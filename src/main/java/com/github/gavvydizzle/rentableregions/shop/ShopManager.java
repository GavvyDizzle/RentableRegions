package com.github.gavvydizzle.rentableregions.shop;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.configs.ShopsConfig;
import com.github.gavvydizzle.rentableregions.gui.InventoryManager;
import com.github.gavvydizzle.rentableregions.shop.invite.InviteManager;
import com.github.gavvydizzle.rentableregions.shop.trade.TradeManager;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.RepeatingTask;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShopManager implements Listener {

    private static final int AUTOSAVE_SECONDS = 300;

    private final RentableRegions instance;
    private final InventoryManager inventoryManager;
    private final InviteManager inviteManager;
    private final TradeManager tradeManager;
    private final HashMap<String, Shop> shopMap;
    private final HashMap<Location, Shop> signShopMap;
    private final HashMap<String, Shop> regionShopMap;
    private RegionManager regionManager;
    private World world;
    private boolean areTimersActive;
    private volatile boolean isSaving;

    // Sign stuff
    private final String[] signLines;
    private boolean isSignGlowing;
    private DyeColor signDyeColor;

    public ShopManager(RentableRegions instance, InventoryManager inventoryManager) {
        this.instance = instance;
        this.inventoryManager = inventoryManager;
        this.inviteManager = new InviteManager(this);
        this.tradeManager = new TradeManager();
        shopMap = new HashMap<>();
        signShopMap = new HashMap<>();
        regionShopMap = new HashMap<>();
        signLines = new String[4];

        startTimerClock();
        isSaving = false;
        startAutoSaveClock();
    }

    public synchronized void reload() {
        FileConfiguration config = instance.getConfig();
        config.options().copyDefaults(true);
        config.addDefault("world", "todo");
        config.addDefault("areTimersActive", true);
        config.addDefault("sign.isGlowing", false);
        config.addDefault("sign.dyeColor", "");
        config.addDefault("sign.line.0", "&0&lShop {id}");
        config.addDefault("sign.line.1", "");
        config.addDefault("sign.line.2", "&1&lClick for info");
        config.addDefault("sign.line.3", "");
        instance.saveConfig();

        inventoryManager.closeAllMenus();

        // A lot of work is done here, call async
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            saveAllShops(); // Save all shops before clearing them from memory (or anything really)


            String worldString = config.getString("world");
            if (worldString == null) {
                instance.getLogger().severe("No world defined in config.yml. Please define one for the plugin to work properly");
                return;
            }

            world = Bukkit.getWorld(worldString);
            if (world == null) {
                instance.getLogger().severe("Invalid world `" + worldString + "` defined in config.yml. Make sure this world exists!");
                return;
            }

            signLines[0] = Colors.conv(config.getString("sign.line.0"));
            signLines[1] = Colors.conv(config.getString("sign.line.1"));
            signLines[2] = Colors.conv(config.getString("sign.line.2"));
            signLines[3] = Colors.conv(config.getString("sign.line.3"));
            isSignGlowing = config.getBoolean("sign.isGlowing");
            try {
                signDyeColor = DyeColor.valueOf(Objects.requireNonNull(config.getString("sign.dyeColor")).toUpperCase());
            } catch (Exception ignored) {
                signDyeColor = null;
            }

            shopMap.clear();
            signShopMap.clear();
            inviteManager.clear();

            areTimersActive = config.getBoolean("areTimersActive");

            regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
            if (regionManager == null) {
                instance.getLogger().severe("Failed to load region manager through WorldGuard. Unable to load shops!");
                return;
            }

            loadShops();
        });
    }

    /**
     * Load all shops from the config file
     */
    private void loadShops() {
        FileConfiguration config = ShopsConfig.get();

        if (config.getConfigurationSection("shops") != null) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("shops")).getKeys(false)) {
                String path = "shops." + key;

                try {
                    Shop shop = new Shop(Objects.requireNonNull(config.getConfigurationSection(path)), this);
                    if (!shop.isLoaded()) {
                        instance.getLogger().warning("Failed to load shop from " + path);
                    } else {
                        if (shopMap.containsKey(shop.getId())) {
                            instance.getLogger().warning("Failed to load shop from " + path + ". The id (" + shop.getId() + ") is already in use!");
                            continue;
                        }

                        shopMap.put(shop.getId(), shop);
                        if (shop.getSignLocation() != null) signShopMap.put(shop.getSignLocation(), shop);
                        for (String s : shop.getRegionNames()) {
                            regionShopMap.put(s, shop);
                        }
                    }
                } catch (Exception e) {
                    instance.getLogger().severe("Encountered an error when loading shop from " + path);
                    e.printStackTrace();
                }
            }
        }
        else {
            instance.getLogger().warning("No shops are defined in shops.yml!");
        }
    }

    /**
     * Saves all shops to the config file
     */
    public void saveAllShops() {
        if (shopMap.isEmpty() || regionManager == null) return;

        FileConfiguration config = ShopsConfig.get();

        for (Shop shop : shopMap.values()) {
            try {
                shop.saveToConfig(config);
            }
            catch (Exception e) {
                instance.getLogger().severe("Failed to save shop " + shop.getId());
                e.printStackTrace();
            }
        }

        ShopsConfig.save();
    }

    /**
     * Saves shops to the config file
     * @param list A list of dirty shops
     */
    private void saveDirtyShops(ArrayList<Shop> list) {
        if (shopMap.isEmpty() || regionManager == null) return;

        FileConfiguration config = ShopsConfig.get();

        for (Shop shop : list) {
            try {
                shop.saveToConfig(config);
            }
            catch (Exception e) {
                instance.getLogger().severe("Failed to save shop " + shop.getId());
                e.printStackTrace();
            }
        }

        ShopsConfig.save();
    }

    /**
     * Starts a task that decreases the time of all shops.
     */
    public void startTimerClock() {
        new RepeatingTask(instance, 20, 20) {
            @Override
            public void run() {
                ArrayList<Shop> dirtyShops = new ArrayList<>();

                for (Shop shop : shopMap.values()) {
                    if (areTimersActive) shop.getRentManager().decreaseTime();
                    if (shop.isDirty()) {
                        dirtyShops.add(shop);
                        shop.setSaving(true);
                    }
                }

                if (!dirtyShops.isEmpty()) {
                    Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                        saveDirtyShops(dirtyShops);
                        for (Shop shop : dirtyShops) {
                            shop.setSaving(false);
                            shop.setNotDirty();
                        }
                    });
                }
            }
        };
    }

    /**
     * Starts a task that saves all shops every 5 minutes.
     */
    public void startAutoSaveClock() {
        new RepeatingTask(instance, 0, AUTOSAVE_SECONDS * 20) {
            @Override
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                    isSaving = true;
                    saveAllShops();
                    isSaving = false;
                });
            }
        };
    }

    /**
     * Sets all shop menus to null.
     * This should only be run if no players are viewing any shop menu.
     */
    public void setAllShopMenusNull() {
        for (Shop shop : shopMap.values()) {
            shop.getShopMenu().setInventoryNull();
        }
    }

    /**
     * Adds a player to the shop as a new member
     * @param newMember The player
     * @param shop The shop
     */
    public void onPlayerJoinShop(OfflinePlayer newMember, Shop shop, boolean ignoreCapacity) {
        shop.addMember(newMember.getUniqueId(), ignoreCapacity, true);
    }

    /**
     * Registers a new shop to be used with this plugin
     * @param shop The shop
     */
    public void registerShop(Shop shop) {
        shopMap.put(shop.getId(), shop);
        if (shop.getSignLocation() != null) signShopMap.put(shop.getSignLocation(), shop);
        for (String s : shop.getRegionNames()) {
            regionShopMap.put(s, shop);
        }
    }

    /**
     * Deletes a shop and edit the regions
     * @param shop The shop
     * @param deleteRegions If the regions should be deleted
     */
    public void deleteShop(Shop shop, boolean deleteRegions) {
        shopMap.remove(shop.getId());
        if (shop.getSignLocation() != null) signShopMap.remove(shop.getSignLocation());
        inventoryManager.closeShopMenu(shop);

        // Remove shop sign if set
        if (shop.getSignLocation() != null && shop.getSignLocation().getBlock().getState() instanceof Sign) {
            shop.getSignLocation().getBlock().setType(Material.AIR);
        }

        // Remove regions from region map
        for (String s : shop.getRegionNames()) {
            regionShopMap.remove(s);
        }

        // Delete the regions or set them back to their default state
        if (deleteRegions) {
            for (ProtectedRegion region : shop.getRegions()) {
                regionManager.removeRegion(region.getId());
            }
        }
        else {
            shop.cleanRegionsOnDelete();
        }

        // Remove the config section for this shop
        // If the file is saving, this will wait until saving is complete to delete the shop's data
        // This should only fail if the saving mechanism breaks
        if (isSaving || shop.isSaving()) {
            new RepeatingTask(instance, 5, 4) {
                int count = 0;
                @Override
                public void run() {
                    if (count >= 5) {
                        instance.getLogger().severe("Failed to delete shop " + shop.getId() + " after 5 attempts. This deleted shop will reappear the next time the shops file gets reloaded.");
                        cancel();
                        return;
                    }

                    if (!isSaving && !shop.isSaving())  {
                        FileConfiguration config = ShopsConfig.get();
                        config.set("shops." + shop.getId(), null);
                        ShopsConfig.save();
                        cancel();
                        return;
                    }

                    if (shop.getSignLocation() != null) {
                        signShopMap.remove(shop.getSignLocation());
                        shop.setSignLocation(null);
                    }
                    count++;
                }
            };
        }
        else {
            FileConfiguration config = ShopsConfig.get();
            config.set("shops." + shop.getId(), null);
            ShopsConfig.save();
        }
    }

    @EventHandler
    private void onSignShopClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign)) return;

        Shop shop = signShopMap.get(block.getLocation());
        if (shop != null) {
            e.setCancelled(true);
            if (instance.getPlayerCommandManager().hasMenuOpenPermission(player)) {
                inventoryManager.openMenu(player, shop.getShopMenu());
            }
            else {
                player.sendMessage(Messages.shopSignPermissionDenied);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSignShopBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        Shop shop = signShopMap.get(block.getLocation());
        if (shop != null) {
            if (instance.getAdminCommandManager().hasSignEditingPermission(e.getPlayer())) {
                onSignDelete(shop);
                e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully deleted the sign for shop " + shop.getId());
            }
            else {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You don't have permission to delete shop signs");
            }
        }
    }

    /**
     * Changes the shop's ID
     * @param shop The shop
     * @param newID the new ID
     * @return True if the change was successful, false if the new id is already in use
     */
    public boolean changeShopID(Shop shop, String newID) {
        if (shopMap.get(newID) != null) { // If the new ID is in use
            return false;
        }

        shopMap.remove(shop.getId());
        shopMap.put(newID, shop);
        shop.setId(newID);
        return true;
    }

    /**
     * Gets a shop by its id
     * @param id The id
     * @return The shop or null
     */
    @Nullable
    public Shop getShopByID(String id) {
        return shopMap.get(id);
    }

    /**
     * Gets a shop by its sign location
     * @param location The location
     * @return The shop or null
     */
    @Nullable
    public Shop getShopBySignLocation(@NotNull Location location) {
        return signShopMap.get(location);
    }

    /**
     * Gets the shop this player is the owner of
     * @param offlinePlayer The player
     * @return The shop or null
     */
    @Nullable
    public Shop getShopByOwner(OfflinePlayer offlinePlayer) {
        for (Shop shop : shopMap.values()) {
            if (shop.isOwner(offlinePlayer.getUniqueId())) {
                return shop;
            }
        }
        return null;
    }

    /**
     * Gets the shop this player is a member of
     * @param offlinePlayer The player
     * @return The shop or null
     */
    @Nullable
    public Shop getShopByMember(OfflinePlayer offlinePlayer) {
        for (Shop shop : shopMap.values()) {
            if (shop.isMember(offlinePlayer.getUniqueId())) {
                return shop;
            }
        }
        return null;
    }

    /**
     * Gets the shop this player is the owner or a member of
     * @param offlinePlayer The player
     * @return The shop or null
     */
    @Nullable
    public Shop getShopByOwnerOrMember(OfflinePlayer offlinePlayer) {
        for (Shop shop : shopMap.values()) {
            if (shop.isOwnerOrMember(offlinePlayer.getUniqueId())) {
                return shop;
            }
        }
        return null;
    }

    /**
     * Determines if this player belongs to a shop
     * @param offlinePlayer The player
     * @return If the player belongs to a shop
     */
    public boolean belongToShop(OfflinePlayer offlinePlayer)  {
        return getShopByOwnerOrMember(offlinePlayer) != null;
    }

    /**
     * @param offlinePlayer The player
     * @return If this player is in a lottery
     */
    public boolean isInLottery(OfflinePlayer offlinePlayer) {
        for (Shop shop : shopMap.values()) {
            if (shop.getLotteryManager().isInLottery(offlinePlayer)) {
                return true;
            }
        }
        return false;
    }

    //*** REGION METHODS ***//

    /**
     * Adds a region to the shop and adds it to the region map
     * @param shop The shop
     * @param region The region
     */
    public void addShopRegion(Shop shop, ProtectedRegion region) {
        shop.addRegion(region);
        regionShopMap.put(region.getId(), shop);
    }

    /**
     * Removes a region from the shop and removes it to the region map
     * @param shop The shop
     * @param region The region
     */
    public void removeShopRegion(Shop shop, ProtectedRegion region) {
        shop.removeRegion(region);
        regionShopMap.remove(region.getId());
    }

    /**
     * Gets the shop at this location.
     * If multiple shops overlap this location, the first one found will be returned.
     * It is assumed that no shops will have overlapping regions.
     * @param location The location
     * @return The shop at this location or null if none exists
     */
    @Nullable
    public Shop getShopFromLocation(Location location) {
        BlockVector3 pos = BlockVector3.at(location.getX(), location.getY(), location.getZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(pos);

        // Checks all regions at this location to find a shop
        for (ProtectedRegion protectedRegion : set) {
            if (!(protectedRegion instanceof GlobalProtectedRegion)) {
                Shop shop = regionShopMap.get(protectedRegion.getId());
                if (shop != null) {
                    return shop;
                }
            }
        }

        return null;
    }

    /**
     * If the player is in a region belonging to a shop, that shop will be returned.
     * If the player is not in a shop, a list of all shop ids will be returned.
     * If the parameter is the CONSOLE, the list of all shops will be returned.<p>
     * This may produce inconsistent results if shop regions overlap
     * @param sender The CommandSender
     * @return A list of shop ids
     */
    @NotNull
    public List<String> getCurrentShopOrAllShops(CommandSender sender) {
        if (!(sender instanceof Player))  {
            return getShopIDs();
        }
        Player player = (Player) sender;

        BlockVector3 pos = BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(pos);

        // Tries to link each region to a shop
        for (ProtectedRegion protectedRegion : set) {
            if (!(protectedRegion instanceof GlobalProtectedRegion)) {
                Shop shop = regionShopMap.get(protectedRegion.getId());
                if (shop != null) {
                    return Collections.singletonList(shop.getId());
                }
            }
        }

        // If the player is not in any shop regions, return all shop ids
        return getShopIDs();
    }

    /**
     * Calculates the non-global regions at this player's locations
     * @param player The player
     * @return A list of applicable region names
     */
    public ArrayList<String> getPlayerRegions(Player player) {
        BlockVector3 pos = BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(pos);

        ArrayList<String> names = new ArrayList<>();
        for (ProtectedRegion protectedRegion : set) {
            if (!(protectedRegion instanceof GlobalProtectedRegion)) {
                names.add(protectedRegion.getId());
            }
        }
        return names;
    }

    /**
     * Get regions by their name
     * @param names The names of the regions
     * @return A list of all regions that were successfully found
     */
    public ArrayList<ProtectedRegion> getRegionsByName(List<String> names) {
        ArrayList<ProtectedRegion> arr = new ArrayList<>(names.size());

        for (String name : names) {
            ProtectedRegion region = regionManager.getRegion(name);
            if (region == null) {
                instance.getLogger().warning("Failed to load region with the name: " + name);
            }
            else {
                arr.add(region);
            }
        }
        return arr;
    }

    /**
     * Determines if this region is used by any shop
     * @param region The region
     * @return True if this region is being used by another shop
     */
    public boolean isRegionTaken(ProtectedRegion region) {
        for (Shop shop : shopMap.values()) {
            if (shop.isRegion(region)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method does a lot of work.
     * It should NOT be used for tab completion.
     * It should be called asynchronously.
     * @return A list of all shop region names
     */
    public ArrayList<String> getAllShopRegions() {
        ArrayList<String> arr = new ArrayList<>();
        for (Shop shop : shopMap.values()) {
            arr.addAll(shop.getRegionNames());
        }
        return arr;
    }

    /**
     * @return A list of all shop ids
     */
    public ArrayList<String> getShopIDs() {
        ArrayList<String> arr = new ArrayList<>();
        for (Shop shop : shopMap.values()) {
            arr.add(shop.getId());
        }
        return arr;
    }

    /**
     * Toggles if the shop timer is active
     * @return The new value after inverting
     */
    public boolean toggleTimer() {
        areTimersActive = !areTimersActive;
        instance.getConfig().set("areTimersActive", areTimersActive);
        instance.saveConfig();
        return areTimersActive;
    }

    //*** SIGN METHODS ***//

    /**
     * Links a sign with its sign.
     * The caller should verify that the location contains a sign.
     * @param shop The shop
     * @param signLocation The new sign location
     */
    public void onSignSet(Shop shop, Location signLocation) {
        shop.setSignLocation(signLocation);
        signShopMap.put(signLocation, shop);
        updateShopSign(shop);
    }

    /**
     * Unlinks the sign from this shop
     * @param shop The shop
     */
    public void onSignDelete(Shop shop) {
        if (shop.getSignLocation() != null) {
            signShopMap.remove(shop.getSignLocation());
            shop.setSignLocation(null);
        }
    }

    /**
     * Updates the text on this shop's sign
     * @param shop The shop
     * @return True if the sign was updated
     */
    public boolean updateShopSign(Shop shop) {
        if (shop.getSignLocation() == null) return false;

        Block block = world.getBlockAt(shop.getSignLocation());
        if (!(block.getState() instanceof Sign)) {
            instance.getLogger().warning("The sign for shop " + shop.getId() + " is invalid. The invalid location has been removed\n" +
                    "Location: " + shop.getSignLocation().toString());
            signShopMap.remove(shop.getSignLocation());
            shop.setSignLocation(null);
            return false;
        }

        Sign sign = (Sign) block.getState();

        sign.setLine(0, signLines[0].replace("{id}", shop.getId()));
        sign.setLine(1, signLines[1].replace("{id}", shop.getId()));
        sign.setLine(2, signLines[2].replace("{id}", shop.getId()));
        sign.setLine(3, signLines[3].replace("{id}", shop.getId()));

        if (signDyeColor != null) sign.setColor(signDyeColor);
        sign.setGlowingText(isSignGlowing);
        sign.update();

        return true;
    }

    /**
     * Updates the text on all shop signs
     * @return The number of signs that were edited
     */
    public int updateAllSigns() {
        int count = 0;
        for (Shop shop : shopMap.values()) {
            if (updateShopSign(shop)) count++;
        }

        return count;
    }

    /**
     * @return The number of valid shop signs
     */
    public int getNumActiveSigns() {
        int count = 0;
        for (Shop shop : shopMap.values()) {
            if (shop.getSignLocation() == null) continue;

            Block block = world.getBlockAt(shop.getSignLocation());
            if (block.getState() instanceof Sign) count++;
        }

        return count;
    }

    public void validateShopRegions(CommandSender sender, boolean updateRegions) {
        int invalid = 0;
        for (Shop shop : shopMap.values()) {
            if (!shop.validateMembers()) {
                invalid++;
                if (updateRegions) {
                    shop.updateMembers();
                    sender.sendMessage(ChatColor.YELLOW + "Shop " + shop.getId() + " had its shop regions fixed");
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "Shop " + shop.getId() + " has mismatching region data");
                }
            }
        }

        if (invalid == 0) {
            sender.sendMessage(ChatColor.GREEN + "All shops have the correct region data");
        }
        else {
            if (updateRegions) {
                sender.sendMessage(ChatColor.GOLD + String.valueOf(invalid) + " shop(s) had their region data fixed");
            }
            else {
                sender.sendMessage(ChatColor.RED + String.valueOf(invalid) + " shop(s) have mismatching region data");
            }
        }
    }

    public void validateShopRegionParents(CommandSender sender) {
        int total = 0;
        for (Shop shop : shopMap.values()) {
            for (ProtectedRegion region : shop.getRegions()) {
                if (region.getParent() == null) {
                    total++;
                    sender.sendMessage(ChatColor.YELLOW + "Region '" + region.getId() + "' has no parent region");
                }
            }
        }

        if (total == 0) {
            sender.sendMessage(ChatColor.GREEN + "All shop regions have a parent region");
        }
        else {
            sender.sendMessage(ChatColor.RED + String.valueOf(total) + " shop region(s) have no parent region");
        }
    }

    /**
     * Determines if this player is not in a valid shops world.
     * If CONSOLE is passed, then this method will always return false
     * @param sender The CommandSender
     * @return If this player is not in a valid shops world
     */
    // Currently this only checks one world, but it could be adapted to work for a list of worlds
    public boolean isNotInValidWorld(CommandSender sender) {
        if (!(sender instanceof Player)) return false;

        if (world == null) return true;

        Player player = (Player) sender;
        return !world.getUID().equals(player.getWorld().getUID());
    }

    public World getWorld() {
        return world;
    }

    public boolean isInWorld(Player player) {
        return player.getWorld().getUID().equals(world.getUID());
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }
}
