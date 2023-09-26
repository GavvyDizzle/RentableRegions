package com.github.gavvydizzle.rentableregions.shop;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.events.PlayerExitShopEvent;
import com.github.gavvydizzle.rentableregions.events.ShopUnclaimEvent;
import com.github.gavvydizzle.rentableregions.gui.ShopMenu;
import com.github.gavvydizzle.rentableregions.utils.Messages;
import com.github.gavvydizzle.rentableregions.utils.Sounds;
import com.github.mittenmc.serverutils.Colors;
import com.github.mittenmc.serverutils.Numbers;
import com.github.mittenmc.serverutils.PlayerNameCache;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Shop {

    private static final int DEFAULT_MEMBER_CAPACITY = 1;
    private static final int MAXIMUM_CAPACITY = 10;

    @NotNull private String id;
    @NotNull private final ArrayList<ProtectedRegion> regions;
    @Nullable private UUID ownerUUID;
    @NotNull private final ArrayList<UUID> memberUUIDs;
    private int memberCapacity;
    @Nullable private Location visitLocation, signLocation;

    private final RentManager rentManager;
    private final LotteryManager lotteryManager;

    private final ShopMenu shopMenu;
    private final boolean isLoaded;
    private volatile boolean isDirty = false, isSaving = false;

    /**
     * Create a new shop with default properties
     * @param id The id of this shop
     */
    public Shop(@NotNull String id) {
        this.id = id.toLowerCase();
        regions = new ArrayList<>();
        ownerUUID = null;
        memberUUIDs = new ArrayList<>();
        memberCapacity = DEFAULT_MEMBER_CAPACITY;
        visitLocation = null;
        signLocation = null;

        rentManager = new RentManager(this);
        lotteryManager = new LotteryManager(this, new ArrayList<>());

        shopMenu = new ShopMenu(this);
        isLoaded = true;
    }

    /**
     * Clones a shop from another shop.
     * This requires the new shop to have a region associated with it.
     * The region will copy the priority from the cloned shop.
     * @param id The id of this shop
     * @param region A region belonging to the new shop
     * @param clone The shop to clone time and level information from
     */
    public Shop(@NotNull String id, @NotNull ProtectedRegion region, @NotNull Shop clone) {
        this.id = id.toLowerCase();

        regions = new ArrayList<>();
        if (!clone.regions.isEmpty()) {
            region.setPriority(clone.regions.get(0).getPriority());
        }
        regions.add(region);

        ownerUUID = null;
        memberUUIDs = new ArrayList<>();
        memberCapacity = clone.memberCapacity;
        visitLocation = null;
        signLocation = null;

        rentManager = new RentManager(this, clone.getRentManager().getSecondsPerRent(),
                clone.getRentManager().getMaxRentSeconds(), clone.getRentManager().getRentPrice(), clone.getRentManager().getLevelRequired());
        lotteryManager = new LotteryManager(this, new ArrayList<>());

        shopMenu = new ShopMenu(this);
        isLoaded = true;
        isDirty = true; // Save when created
    }

    /**
     * Creates a shop from its configuration section
     * @param section The ConfigurationSection this shop is defined in
     */
    public Shop(@NotNull ConfigurationSection section, ShopManager shopManager) {
        this.id = Objects.requireNonNull(section.getString("id"));

        List<String> regionStrings = section.getStringList("regionNames");
        regions = shopManager.getRegionsByName(regionStrings);

        ownerUUID = Objects.requireNonNull(section.getString("owner")).isEmpty() ? null : UUID.fromString(Objects.requireNonNull(section.getString("owner")));

        List<String> memberUUIDStrings = section.getStringList("members");
        memberUUIDs = new ArrayList<>();
        for (String str : memberUUIDStrings) {
            memberUUIDs.add(UUID.fromString(str));
        }

        this.memberCapacity = section.getInt("memberCapacity");
        this.visitLocation = section.getLocation("visitLocation");
        this.signLocation = section.getLocation("signLocation");

        rentManager = new RentManager(this, section.getInt("secondsRemaining"), section.getInt("secondsPerRent"),
                section.getInt("maxRentSeconds"), section.getInt("rentPrice"), section.getInt("levelRequired"));

        List<String> lotteryUUIDStrings = section.getStringList("lottery");
        ArrayList<UUID> lotteryUUIDs = new ArrayList<>();
        for (String str : lotteryUUIDStrings) {
            lotteryUUIDs.add(UUID.fromString(str));
        }
        lotteryManager = new LotteryManager(this, lotteryUUIDs);

        if (!isOccupied()) {
            rentManager.setNoTimeRemaining();
        }

        shopMenu = new ShopMenu(this);
        isLoaded = true;
        isDirty = true; // Save when created
    }

    /**
     * Saves the entire shop
     * @param config The config file to save to
     */
    protected void saveToConfig(@NotNull FileConfiguration config) {
        String path = "shops." + id + ".";
        config.set(path + "id", id);
        config.set(path + "regionNames", getRegionNames());
        config.set(path + "owner", ownerUUID == null ? "" : ownerUUID.toString());
        config.set(path + "members", memberUUIDStrings());
        config.set(path + "memberCapacity", memberCapacity);
        config.set(path + "visitLocation", visitLocation);
        config.set(path + "signLocation", signLocation);
        config.set(path + "secondsRemaining", rentManager.getSecondsRemaining());
        config.set(path + "secondsPerRent", rentManager.getSecondsPerRent());
        config.set(path + "maxRentSeconds", rentManager.getMaxRentSeconds());
        config.set(path + "rentPrice", rentManager.getRentPrice());
        config.set(path + "levelRequired", rentManager.getLevelRequired());
        config.set(path + "lottery", lotteryManager.getLotteryUUIDStrings());
    }

    private ArrayList<String> memberUUIDStrings() {
        ArrayList<String> arr = new ArrayList<>();
        for (UUID uuid : memberUUIDs) {
            arr.add(uuid.toString());
        }
        return arr;
    }

    public ArrayList<String> getRegionNames() {
        ArrayList<String> arr = new ArrayList<>(regions.size());

        for (ProtectedRegion region : regions) {
            arr.add(region.getId());
        }
        return arr;
    }

    /**
     * Copies a shop's time, price, level, and member properties
     * @param other The shop to copy from
     */
    public void copyProperties(Shop other) {
        rentManager.setSecondsPerRent(other.rentManager.getSecondsPerRent());
        rentManager.setMaxRentSeconds(other.rentManager.getMaxRentSeconds());
        rentManager.setRentPrice(other.rentManager.getRentPrice());
        rentManager.setLevelRequired(other.rentManager.getLevelRequired());
        setMemberCapacity(other.memberCapacity);
    }

    /**
     * Attempts to message the owner and all members of this shop.
     * The message will only send if the player is online.
     * @param messages The message(s) to send.
     */
    public void messageAllMembers(String... messages) {
        Messages.sendMessage(ownerUUID, messages);

        for (UUID uuid : memberUUIDs) {
            Messages.sendMessage(uuid, messages);
        }
    }

    protected void onTimeExpire() {
        RentableRegions.getInstance().getShopLogger().writeToLog("Shop " + id + " ran out of time");

        createUnclaimEvent();

        removeOwner();
        removeAllMembers();

        lotteryManager.runLottery();
        shopMenu.updateAllItems();

        removeEntryFlag();

        isDirty = true;
    }

    /**
     * Creates an unclaim event for this shop.
     * This will NOT edit the members of the shop, instead it just fires
     * the event and sets all blocks to air if told to.
     */
    private void createUnclaimEvent() {
        ShopUnclaimEvent unclaimEvent = new ShopUnclaimEvent(this);
        Bukkit.getPluginManager().callEvent(unclaimEvent);

        if (unclaimEvent.isSetToAir()) {
            if (numRegions() == 0) return;

            com.sk89q.worldedit.world.World w = BukkitAdapter.adapt(RentableRegions.getInstance().getShopManager().getWorld());

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(w)) {
                RandomPattern pat = new RandomPattern();
                pat.add(BukkitAdapter.adapt(Material.AIR.createBlockData()), 1);

                // Create analogous WorldEdit regions and set them to air
                for (ProtectedRegion region : regions) {
                    if (region instanceof ProtectedCuboidRegion) {
                        CuboidRegion cr = new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
                        editSession.replaceBlocks(cr, Masks.alwaysTrue(), pat);
                    }
                    else if (region instanceof ProtectedPolygonalRegion) {
                        Polygonal2DRegion pr = new Polygonal2DRegion(w, region.getPoints(), region.getMinimumPoint().getY(), region.getMaximumPoint().getY());
                        editSession.replaceBlocks(pr, Masks.alwaysTrue(), pat);
                    }
                }

            } catch (MaxChangedBlocksException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Creates an event for when a player stops being a member of the shop.
     * This will NOT edit the members of the shop, instead it just fires event.
     * @param playerWhoExit The player who left/exit
     */
    public void createExitEvent(OfflinePlayer playerWhoExit) {
        PlayerExitShopEvent exitShopEvent = new PlayerExitShopEvent(playerWhoExit, this);
        Bukkit.getPluginManager().callEvent(exitShopEvent);
    }

    /**
     * Change the owner of the shop.
     * This method does not change anything except the owner!
     * @param uuid The uuid of the new owner
     */
    public void setOwner(@NotNull UUID uuid) {
        if (ownerUUID != null) {
            for (ProtectedRegion region : regions) {
                region.getOwners().removePlayer(ownerUUID);
            }
        }

        ownerUUID = uuid;
        shopMenu.updateOwnerItem();

        for (ProtectedRegion region : regions) {
            region.getOwners().addPlayer(ownerUUID);
        }

        isDirty = true;
    }

    /**
     * Remove the owner from the shop
     */
    public void removeOwner() {
        if (ownerUUID != null) {
            for (ProtectedRegion region : regions) {
                region.getOwners().removePlayer(ownerUUID);
            }
        }

        ownerUUID = null;
        shopMenu.updateOwnerItem();

        isDirty = true;
    }

    /**
     * Adds a member to this shop.
     * If set to ignore capacity, the player will be added even if they would go over the member cap
     * @param uuid The member's uuid
     * @param ignoreCapacity If this should ignore the member cap
     * @param sendMessage If the added member should be messaged
     */
    public void addMember(@NotNull UUID uuid, boolean ignoreCapacity, boolean sendMessage) {
        if (!ignoreCapacity && isAtCapacity()) { // Shop is full
            return;
        }

        memberUUIDs.add(uuid);
        shopMenu.updateMemberItem();
        for (ProtectedRegion region : regions) {
            region.getMembers().addPlayer(uuid);
        }

        if (sendMessage) Messages.sendMessage(uuid, Messages.addedAsMember.replace("{name}", PlayerNameCache.get(ownerUUID)));

        isDirty = true;
    }

    /**
     * Removes this member from the shop
     * @param uuid The player's uuid
     * @param sendMessage If the removed member should be messaged
     */
    public void removeMember(@NotNull UUID uuid, boolean sendMessage) {
        if (memberUUIDs.remove(uuid)) {
            shopMenu.updateMemberItem();
            for (ProtectedRegion region : regions) {
                region.getMembers().removePlayer(uuid);
            }
            createExitEvent(Bukkit.getOfflinePlayer(uuid));
            if (sendMessage) Messages.sendMessage(uuid, Messages.removedAsMember.replace("{name}", PlayerNameCache.get(ownerUUID)));
        }

        isDirty = true;
    }

    /**
     * Removes all members from the shop.
     * Events are not fired for these players as they are removed from the shop
     */
    public void removeAllMembers() {
        for (UUID uuid : memberUUIDs) {
            for (ProtectedRegion region : regions) {
                region.getMembers().removePlayer(uuid);
            }
        }
        memberUUIDs.clear();
        shopMenu.updateMemberItem();

        isDirty = true;
    }

    /**
     * Removes all members and gives the money left in rent time back to the owner.
     * This should not be called when the time expires naturally
     * @throws RuntimeException if the shop has no owner
     */
    public void unclaim(boolean runLottery) {
        if (ownerUUID == null) {
            throw new RuntimeException("Tried to unclaim a shop with no owner");
        }

        rentManager.onShopUnclaim(ownerUUID);
        createUnclaimEvent();
        removeAllMembers();
        removeOwner();
        if (runLottery) {
            lotteryManager.runLottery();
        } else {
            lotteryManager.cancelLottery();
        }
        shopMenu.updateAllItems();

        removeEntryFlag();

        isDirty = true;
    }

    /**
     * Transfers ownership to a new player and removes all members
     * @param sender Who ran this action
     * @param newOwner The player to give ownership to
     * @param seconds The seconds to give to the shop initially
     */
    public void transferOwnership(CommandSender sender, OfflinePlayer newOwner, int seconds) {
        if (ownerUUID != null && ownerUUID.equals(newOwner.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Transfer cancelled because this player is already the owner");
            return;
        }

        messageAllMembers(Messages.shopTransferred);

        unclaim(false);

        setOwner(newOwner.getUniqueId());
        rentManager.setTimeRemaining(seconds);

        Messages.sendMessage(newOwner.getUniqueId(), Messages.givenShopByTransfer.replace("{id}", id));
        sender.sendMessage(ChatColor.GREEN + "Successfully transferred shop " + id + " to " + newOwner.getName() + " with " + Numbers.getTimeFormatted(seconds));

        isDirty = true;
    }

    /**
     * Transfers ownership from the current owner to the current co-owner.
     * @param oldMember The member to make owner
     */
    public void swapOwnership(OfflinePlayer oldMember) {
        if (ownerUUID == null) {
            throw new RuntimeException("Tried to swap ownership with no owner");
        }

        if (!memberUUIDs.contains(oldMember.getUniqueId())) {
            Messages.sendMessage(ownerUUID, Messages.invalidMemberOnSwap);
            return;
        }

        UUID oldOwner = ownerUUID;

        removeMember(oldMember.getUniqueId(), false);
        addMember(ownerUUID, true, false);
        setOwner(oldMember.getUniqueId());

        shopMenu.updateOwnerItem();
        shopMenu.updateMemberItem();

        Messages.sendMessage(ownerUUID, Messages.newOwnerOnSwap.replace("{name}", PlayerNameCache.get(oldOwner)));
        Messages.sendMessage(oldOwner, Messages.oldOwnerOnSwap.replace("{name}", PlayerNameCache.get(ownerUUID)));

        isDirty = true;
    }

    /**
     * Teleports the player to this shop.
     * Sends an error message if the location is null.
     * @param player The player to teleport.
     */
    public void teleportPlayer(Player player) {
        if (visitLocation == null) {
            player.sendMessage(Messages.invalidVisitLocation.replace("{id}", id));
            Sounds.generalFailSound.playSound(player);
            return;
        }
        player.teleport(visitLocation);
        Sounds.teleportClickSound.playSound(player);
    }

    /**
     * Determines if this player is allowed to join this shop.
     * Checks if their level is equal to or higher and this shop's required level.
     * If the player has prestiged, then the level requirement will be ignored.
     *
     * @param player the player to check
     * @return If the player meets the join requirements
     */
    public boolean doesNotMeetJoinRequirements(Player player) {
        return RentableRegions.getInstance().getPlayerLevelsAPI().getPlayerLevel(player) < rentManager.getLevelRequired() &&
                RentableRegions.getInstance().getRankupsAPI().getPrestigePosition(player) <= 0;
    }

    /**
     * Removes all non-members from within the shop region(s)
     * Does not remove players with the permission "rentableregions.bypass"
     *
     * @return The number of players removed or -1 if the no visit location exists
     */
    public int removeVisitors() {
        if (visitLocation == null) {
            return -1;
        }

        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isOwnerOrMember(player.getUniqueId()) || player.hasPermission("rentableregions.bypass")) continue;

            if (isPlayerInRegions(player))  {
                player.teleport(visitLocation);
                count++;
            }
        }
        return count;
    }

    /**
     * Toggles the island privacy by updating the regions
     * @return If the shop is now private
     */
    public boolean togglePrivacy() {
        if (regions.isEmpty()) return false;

        if (regions.get(0).getFlag(Flags.ENTRY) == StateFlag.State.DENY) {
            for (ProtectedRegion region : regions) {
                region.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
            }
            return false;
        }
        else {
            for (ProtectedRegion region : regions) {
                region.setFlag(Flags.ENTRY, StateFlag.State.DENY);
            }
            return true;
        }
    }

    /**
     * Removes the owner and members form this region and unsets the ENTRY flag
     */
    protected void cleanRegionsOnDelete() {
        for (ProtectedRegion region : regions) {
            region.getOwners().clear();
            region.getMembers().clear();
            region.setFlag(Flags.ENTRY, null);
        }
    }

    /**
     * Removes the ENTRY flag from all regions
     */
    public void removeEntryFlag() {
        for (ProtectedRegion region : regions) {
            region.setFlag(Flags.ENTRY, null);
        }
    }

    /**
     * Adds a region while copying properties from the existing region(s).
     * This will copy the existing owner, members, flags, and priority.
     * If no regions belong to this shop, only the owner and members will be edited.
     * @param region The region to add
     */
    protected void addRegion(ProtectedRegion region) {
        if (!regions.isEmpty()) {
            ProtectedRegion template = regions.get(0);
            region.setMembers(template.getMembers());
            region.setOwners(template.getOwners());
            region.setFlags(template.getFlags());
            region.setPriority(template.getPriority());
        }
        else { // Add members manually if no regions exist
            if (ownerUUID != null) region.getOwners().addPlayer(ownerUUID);
            for (UUID uuid : memberUUIDs) {
                region.getMembers().addPlayer(uuid);
            }
        }

        regions.add(region);
        isDirty = true;
    }

    /**
     * Removes a region from this shop.
     * This will only remove the owner and members.
     * The region's ENTRY flag will be unset.
     * All other flags and priority will remain untouched.
     * @param region The region
     */
    protected void removeRegion(ProtectedRegion region) {
        if (regions.remove(region)) {
            region.getOwners().clear();
            region.getMembers().clear();
            region.setFlag(Flags.ENTRY, null);
        }
        isDirty = true;
    }

    /**
     * @param region The region
     * @return If the region belongs to this shop
     */
    public boolean isRegion(ProtectedRegion region) {
        return regions.contains(region);
    }

    /**
     * @param player The player to check
     * @return If the player is in one of the shop regions
     */
    private boolean isPlayerInRegions(Player player) {
        if (visitLocation == null || !player.getWorld().equals(visitLocation.getWorld())) return false;

        for (ProtectedRegion region : regions) {
            if (region.contains(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()))) return true;
        }
        return false;
    }

    /**
     * Checks if the location is contained in one of the shop's regions.
     * The caller should first validate that the worlds are the same.
     * @param location The location to check
     * @return True if this location is within any of this shop's regions
     */
    public boolean isInShopRegion(@NotNull Location location) {
        for (ProtectedRegion region : regions) {
            if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) return true;
        }
        return false;
    }

    /**
     * Checks if the block is contained in one of the shop's regions.
     * The caller should first validate that the worlds are the same.
     * @param block The block to check
     * @return True if this block is within any of this shop's regions
     */
    public boolean isInShopRegion(@NotNull Block block) {
        return isInShopRegion(block.getLocation());
    }

    /**
     * @param uuid The uuid
     * @return If this uuid matches the owner
     */
    public boolean isOwner(UUID uuid) {
        if (ownerUUID == null) return false;
        return ownerUUID.equals(uuid);
    }

    /**
     * @param uuid The uuid
     * @return If this uuid matches one of the members
     */
    public boolean isMember(UUID uuid) {
        return memberUUIDs.contains(uuid);
    }

    /**
     * @param uuid The uuid
     * @return If this uuid matches the owner or one of the members
     */
    public boolean isOwnerOrMember(UUID uuid) {
        return isOwner(uuid) || isMember(uuid);
    }

    /**
     * @return If the number of members is greater than or equal to the capacity
     */
    public boolean isAtCapacity() {
        return memberUUIDs.size() >= memberCapacity;
    }

    /**
     * @return If the number of members is greater than the capacity
     */
    public boolean isOverfilled() {
        return memberUUIDs.size() > memberCapacity;
    }

    /**
     * Determines if this shop is occupied by checking if it has an owner.
     * If the shop has an owner, then it should not have any members.
     * @return If the shop is occupied
     */
    public boolean isOccupied() {
        return ownerUUID != null;
    }

    /**
     * Checks the in-memory members with the members of all regions.
     * If the members are mismatched in any region, this will return false
     * @return True if the members and regions agree, false if mismatched
     */
    public boolean validateMembers() {
        int ownerSize = ownerUUID == null ? 0 : 1;

        for (ProtectedRegion region : regions) {
            if (ownerSize != region.getOwners().size()) return false; // Owner number mismatch
            else if (ownerUUID != null && !region.getOwners().getUniqueIds().contains(ownerUUID)) return false; // Owner not the same

            if (memberUUIDs.size() != region.getMembers().size()) return false; // Member number mismatch
            else if (!memberUUIDs.containsAll(region.getMembers().getUniqueIds())) return false; // Member list mismatch
        }
        return true;
    }

    /**
     * Refreshes the owner/members of all regions with the current owner/members.
     * This should only be called after {@link #validateMembers()} returns false.
     */
    public void updateMembers() {
        for (ProtectedRegion region : regions) {
            if (ownerUUID != null) region.getOwners().addPlayer(ownerUUID);
            for (UUID uuid : memberUUIDs) {
                region.getMembers().addPlayer(uuid);
            }
        }
    }

    public ArrayList<String> getInfo() {
        ArrayList<String> arr = new ArrayList<>();

        String c1 = Colors.conv("&a");
        String c2 = Colors.conv("<SOLID:00D100>");

        arr.add(Colors.conv("&6---(Shop " + id + " Info)---"));

        // Owner
        if (ownerUUID == null) {
           arr.add(c1 + "Owner: none");
        }
        else {
            arr.add(c1 + "Owner: " + PlayerNameCache.get(ownerUUID) + " (" + ownerUUID.toString() + ")");
        }

        // Members
        if (memberUUIDs.isEmpty()) {
            arr.add(c2 + "Members: none");
        }
        else {
            arr.add(c2 + "Members:");
            for (UUID uuid : memberUUIDs) {
                arr.add(ChatColor.GRAY + "- " + PlayerNameCache.get(uuid) + " (" + uuid.toString() + ")");
            }
        }

        arr.add(c1 + "Max members: " + memberCapacity);
        arr.add(c2+ "Regions (" + regions.size() + ") - " + getRegionNames().toString());
        if (visitLocation != null) {
            arr.add(c1 + "Visit Location: " + ChatColor.BLUE + "SET");
        }
        else {
            arr.add(c1 + "Visit Location: " + ChatColor.RED + "NOT SET");
        }
        if (signLocation != null) {
            arr.add(c2 + "Sign: " + ChatColor.BLUE + "ACTIVE");
        }
        else {
            arr.add(c2 + "Sign: " + ChatColor.RED + "NOT ACTIVE");
        }

        // Renting
        arr.add(c1 + "Time remaining: " + Numbers.getTimeFormatted(rentManager.getSecondsRemaining()));
        arr.add(c2 + "Time per rent: " + Numbers.getTimeFormatted(rentManager.getSecondsPerRent()));
        arr.add(c1 + "Max rent time: " + Numbers.getTimeFormatted(rentManager.getMaxRentSeconds()));
        arr.add(c2 + "Rent price: $" + Numbers.withSuffix(rentManager.getRentPrice()));
        arr.add(c1 + "Level required: " + rentManager.getLevelRequired());

        arr.add(Colors.conv("&6----------------"));

        return arr;
    }

    //*** GETTERS & SETTERS ***//

    public RentManager getRentManager() {
        return rentManager;
    }

    public LotteryManager getLotteryManager() {
        return lotteryManager;
    }

    @NotNull
    public String getId() {
        return id;
    }

    protected void setId(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public ArrayList<ProtectedRegion> getRegions() {
        return regions;
    }

    public int numRegions() {
        return regions.size();
    }

    @Nullable
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    /**
     * @return A copy of the members list
     */
    @NotNull
    public ArrayList<UUID> getMemberUUIDs() {
        return new ArrayList<>(memberUUIDs);
    }

    public Collection<String> getMemberNames() {
        return PlayerNameCache.get(memberUUIDs);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public int getMemberCapacity() {
        return memberCapacity;
    }

    /**
     * Update the shop's member capacity.
     * This value must be between 0 and {@value MAXIMUM_CAPACITY}
     * @param memberCapacity The new capacity
     */
    public void setMemberCapacity(int memberCapacity) {
        this.memberCapacity = Numbers.constrain(memberCapacity, 0, MAXIMUM_CAPACITY);
    }

    public void setVisitLocation(@Nullable Location visitLocation) {
        this.visitLocation = visitLocation;
    }

    @Nullable
    public Location getSignLocation() {
        return signLocation;
    }

    public void setSignLocation(@Nullable Location signLocation) {
        this.signLocation = signLocation;
    }

    public ShopMenu getShopMenu() {
        return shopMenu;
    }

    protected boolean isDirty() {
        return isDirty;
    }

    protected void setNotDirty() {
        isDirty = false;
    }

    protected boolean isSaving() {
        return isSaving;
    }

    protected void setSaving(boolean saving) {
        isSaving = saving;
    }
}
