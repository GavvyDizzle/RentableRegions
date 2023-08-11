package com.github.gavvydizzle.rentableregions.gui;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.configs.MenuConfig;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.mittenmc.serverutils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * The main menu of a shop
 */
public class ShopMenu implements ClickableMenu {
    private static String inventoryName;
    private static int inventorySize;
    private static ItemStack filler;
    private static InventoryItem ownerItem, noOwnerItem, memberItem, rentItem, extraInfoItem, lotteryItem, teleportItem;
    private static String memberListLine, noMembersLine;

    private final Shop shop;
    private Inventory inventory;
    private int numViewers;

    public ShopMenu(Shop shop) {
        this.shop = shop;
        numViewers = 0;
    }

    // To be called by the InventoryManager
    protected static void reload() {
        FileConfiguration config = MenuConfig.get();
        config.options().copyDefaults(true);

        config.addDefault("shopmenu.name", "Shop {id}");
        config.addDefault("shopmenu.rows", 4);
        config.addDefault("shopmenu.filler", ColoredItems.GRAY.name());

        config.addDefault("shopmenu.items.owner.slot", 10);
        config.addDefault("shopmenu.items.owner.name", "&eOwner: {name}");
        config.addDefault("shopmenu.items.owner.lore", new ArrayList<>());
        config.addDefault("shopmenu.items.noOwner.name", "&eThis shop has no owner");
        config.addDefault("shopmenu.items.noOwner.lore", new ArrayList<>());

        config.addDefault("shopmenu.items.member.slot", 12);
        config.addDefault("shopmenu.items.member.material", Material.ARMOR_STAND.name());
        config.addDefault("shopmenu.items.member.name", "&eMembers:");
        config.addDefault("shopmenu.items.member.lore", Collections.singletonList("[list]"));
        config.addDefault("shopmenu.items.member.listLine", "&7- {name}");
        config.addDefault("shopmenu.items.member.noMembersLine", "&7This shop has no members");

        config.addDefault("shopmenu.items.rent.slot", 14);
        config.addDefault("shopmenu.items.rent.material", Material.SUNFLOWER.name());
        config.addDefault("shopmenu.items.rent.name", "&eRent");
        config.addDefault("shopmenu.items.rent.lore", Arrays.asList(
                "&7Click here to rent this shop",
                "",
                "&7Time Remaining: <SOLID:FFFF8F>{time_remaining}",
                "&7Adding <SOLID:FFFF8F>{time_increase} &7will cost &a${rent_charge}",
                "",
                "&7Price: &a${rent_price} &7per <SOLID:FFFF8F>{rent_time}",
                "&7Maximum Time: <SOLID:FFFF8F>{max_time}"
        ));

        config.addDefault("shopmenu.items.extra.slot", 16);
        config.addDefault("shopmenu.items.extra.material", Material.BOOK.name());
        config.addDefault("shopmenu.items.extra.name", "&eExtra Information");
        config.addDefault("shopmenu.items.extra.lore", Arrays.asList(
                "&7Minimum Level: {level_required}",
                "&7Member Capacity: {member_cap}"
        ));

        config.addDefault("shopmenu.items.lottery.slot", 20);
        config.addDefault("shopmenu.items.lottery.material", Material.PAPER.name());
        config.addDefault("shopmenu.items.lottery.name", "&eLottery");
        config.addDefault("shopmenu.items.lottery.lore", Arrays.asList(
                "<SOLID:5CFF5C>Left-click to join the lottery",
                "<SOLID:FF5C5C>Right-click to leave the lottery",
                "",
                "<SOLID:FFFF8F>{entries} &7players are currently entered"
        ));

        config.addDefault("shopmenu.items.teleport.slot", 22);
        config.addDefault("shopmenu.items.teleport.material", Material.END_PORTAL_FRAME.name());
        config.addDefault("shopmenu.items.teleport.name", "&eTeleport");
        config.addDefault("shopmenu.items.teleport.lore", Collections.singletonList("&7Click here to teleport to this shop"));

        MenuConfig.save();

        // Keeps track of the used slots to warn the console about
        ArrayList<Integer> usedSlots = new ArrayList<>();

        inventoryName = Colors.conv(config.getString("shopmenu.name"));
        inventorySize = Numbers.constrain(config.getInt("shopmenu.rows"), 1, 6) * 9;
        filler = ColoredItems.getGlassByName(config.getString("shopmenu.filler"));

        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.owner.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.owner.lore")));
        itemStack.setItemMeta(meta);
        ownerItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.owner.slot"), 0, inventorySize-1));
        usedSlots.add(ownerItem.getSlot());

        itemStack = new ItemStack(Material.PLAYER_HEAD);
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.noOwner.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.noOwner.lore")));
        itemStack.setItemMeta(meta);
        noOwnerItem = new InventoryItem(itemStack, ownerItem.getSlot());

        memberListLine = Colors.conv(config.getString("shopmenu.items.member.listLine"));
        noMembersLine = Colors.conv(config.getString("shopmenu.items.member.noMembersLine"));
        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString("shopmenu.items.member.material"), Material.ARMOR_STAND));
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.member.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.member.lore")));
        itemStack.setItemMeta(meta);
        memberItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.member.slot"), 0, inventorySize-1));
        if (usedSlots.contains(memberItem.getSlot())) {
            RentableRegions.getInstance().getLogger().warning("The member item's slot has already been defined in menus.yml. Please change it to avoid undesired behavior");
        }
        else {
            usedSlots.add(memberItem.getSlot());
        }

        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString("shopmenu.items.rent.material"), Material.SUNFLOWER));
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.rent.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.rent.lore")));
        itemStack.setItemMeta(meta);
        rentItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.rent.slot"), 0, inventorySize-1));
        if (usedSlots.contains(rentItem.getSlot())) {
            RentableRegions.getInstance().getLogger().warning("The rent item's slot has already been defined in menus.yml. Please change it to avoid undesired behavior");
        }
        else {
            usedSlots.add(rentItem.getSlot());
        }

        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString("shopmenu.items.extra.material"), Material.BOOK));
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.extra.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.extra.lore")));
        itemStack.setItemMeta(meta);
        extraInfoItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.extra.slot"), 0, inventorySize-1));
        if (usedSlots.contains(extraInfoItem.getSlot())) {
            RentableRegions.getInstance().getLogger().warning("The extra item's slot has already been defined in menus.yml. Please change it to avoid undesired behavior");
        }
        else {
            usedSlots.add(extraInfoItem.getSlot());
        }

        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString("shopmenu.items.lottery.material"), Material.PAPER));
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.lottery.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.lottery.lore")));
        itemStack.setItemMeta(meta);
        lotteryItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.lottery.slot"), 0, inventorySize-1));
        if (usedSlots.contains(lotteryItem.getSlot())) {
            RentableRegions.getInstance().getLogger().warning("The lottery item's slot has already been defined in menus.yml. Please change it to avoid undesired behavior");
        }
        else {
            usedSlots.add(lotteryItem.getSlot());
        }

        itemStack = new ItemStack(ConfigUtils.getMaterial(config.getString("shopmenu.items.teleport.material"), Material.END_PORTAL_FRAME));
        meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Colors.conv(config.getString("shopmenu.items.teleport.name")));
        meta.setLore(Colors.conv(config.getStringList("shopmenu.items.teleport.lore")));
        itemStack.setItemMeta(meta);
        teleportItem = new InventoryItem(itemStack, Numbers.constrain(config.getInt("shopmenu.items.teleport.slot"), 0, inventorySize-1));
        if (usedSlots.contains(teleportItem.getSlot())) {
            RentableRegions.getInstance().getLogger().warning("The teleport item's slot has already been defined in menus.yml. Please change it to avoid undesired behavior");
        }
        else {
            usedSlots.add(teleportItem.getSlot());
        }
    }

    /**
     * This should be called when the menu is reloaded.
     * This MUST be called after all players have been removed from this menu
     */
    public void setInventoryNull() {
        numViewers = 0;
        inventory = null;
    }

    @Override
    public void openInventory(Player player) {
        if (inventory == null) {
            // Shop id in the inventory name will not update if it changed, oh well
            inventory = Bukkit.createInventory(null, inventorySize, inventoryName.replace("{id}", shop.getId()));

            for (int i = 0; i < inventorySize; i++) {
                inventory.setItem(i, filler);
            }

            updateAllItems();
            inventory.setItem(teleportItem.getSlot(), teleportItem.getItemStack());
        }

        // Update the inventory if a player opens it and nobody else if viewing it
        if (numViewers == 0) {
            updateAllItems();
        }

        player.openInventory(inventory);
        numViewers++;
    }

    @Override
    public void closeInventory(Player player) {
        numViewers = Math.max(0, numViewers - 1);
    }

    @Override
    public void handleClick(InventoryClickEvent e) {
        int slot = e.getSlot();
        Player player = (Player) e.getWhoClicked();

        if (slot == rentItem.getSlot()) {
            shop.getRentManager().rent(player);
        }
        else if (slot == lotteryItem.getSlot()) {
            if (e.getClick() == ClickType.LEFT) {
                shop.getLotteryManager().onLotteryClick(player, true);
            }
            else if (e.getClick() == ClickType.RIGHT) {
                shop.getLotteryManager().onLotteryClick(player, false);
            }
        }
        else if (slot == teleportItem.getSlot()) {
            shop.teleportPlayer(player);
        }
    }

    /**
     * Updates all applicable inventory items for when a time decrease occurs.
     * This should only be called at the end of the shop's decrease time method
     */
    public void updateOnTimeDecrease() {
        if (numViewers > 0) {
            updateRentItem();
            updateLotteryItem();
        }
    }

    public void updateAllItems() {
        updateOwnerItem();
        updateMemberItem();
        updateRentItem();
        updateExtraInfoItem();
        updateLotteryItem();
    }

    public void updateOwnerItem() {
        if (inventory == null) return;

        UUID ownerUUID = shop.getOwnerUUID();

        if (ownerUUID == null) {
            ItemMeta placeholderMeta = noOwnerItem.getItemStack().getItemMeta();

            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;
            assert placeholderMeta != null;
            if (placeholderMeta.hasDisplayName()) meta.setDisplayName(placeholderMeta.getDisplayName());
            if (placeholderMeta.hasLore()) meta.setLore(placeholderMeta.getLore());

            itemStack.setItemMeta(meta);
            inventory.setItem(ownerItem.getSlot(), itemStack);
        }
        else {
            ItemMeta placeholderMeta = ownerItem.getItemStack().getItemMeta();

            if (PlayerHeads.isCached(ownerUUID)) {
                ItemStack itemStack = PlayerHeads.getHead(ownerUUID);
                ItemMeta meta = itemStack.getItemMeta();
                assert meta != null;
                assert placeholderMeta != null;
                if (placeholderMeta.hasDisplayName()) meta.setDisplayName(placeholderMeta.getDisplayName().replace("{name}", PlayerNameCache.get(ownerUUID)));
                if (placeholderMeta.hasLore()) {
                    ArrayList<String> lore = new ArrayList<>();
                    for (String s : Objects.requireNonNull(placeholderMeta.getLore())) {
                        lore.add(s.replace("{name}", PlayerNameCache.get(ownerUUID)));
                    }
                    meta.setLore(lore);
                }

                itemStack.setItemMeta(meta);
                inventory.setItem(ownerItem.getSlot(), itemStack);
            }
            else {
                Bukkit.getScheduler().runTaskAsynchronously(RentableRegions.getInstance(), () -> {
                    ItemStack itemStack = PlayerHeads.getHead(ownerUUID);
                    ItemMeta meta = itemStack.getItemMeta();
                    assert meta != null;
                    assert placeholderMeta != null;
                    if (placeholderMeta.hasDisplayName()) meta.setDisplayName(placeholderMeta.getDisplayName().replace("{name}", PlayerNameCache.get(ownerUUID)));
                    if (placeholderMeta.hasLore()) {
                        ArrayList<String> lore = new ArrayList<>();
                        for (String s : Objects.requireNonNull(placeholderMeta.getLore())) {
                            lore.add(s.replace("{name}", PlayerNameCache.get(ownerUUID)));
                        }
                        meta.setLore(lore);
                    }

                    itemStack.setItemMeta(meta);
                    inventory.setItem(ownerItem.getSlot(), itemStack);
                });
            }
        }
    }

    public void updateMemberItem() {
        if (inventory == null) return;

        ArrayList<String> memberNames = new ArrayList<>();
        for (UUID uuid : shop.getMemberUUIDs()) {
            memberNames.add(PlayerNameCache.get(uuid));
        }
        Collections.sort(memberNames);

        ArrayList<String> formattedNames = new ArrayList<>();
        for (String s : memberNames) {
            formattedNames.add(memberListLine.replace("{name}", s));
        }

        ItemStack itemStack = memberItem.getItemStack().clone();
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        ArrayList<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (String s : Objects.requireNonNull(meta.getLore())) {
                if (s.contains("[list]")) { // Text in this line will be lost
                    if (formattedNames.isEmpty()) {
                        lore.add(noMembersLine);
                    }
                    else {
                        lore.addAll(formattedNames);
                    }
                }
                else {
                    lore.add(s);
                }
            }
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        inventory.setItem(memberItem.getSlot(), itemStack);
    }

    public void updateRentItem() {
        if (inventory == null) return;

        ItemStack itemStack = rentItem.getItemStack().clone();
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        String timeFormatted = Numbers.getTimeFormatted(shop.getRentManager().secondsGainedForNextRent());
        String priceFormatted = Numbers.withSuffix(Math.max(0, shop.getRentManager().priceOfNextRent()));
        String timeRemaining = Numbers.getTimeFormatted(shop.getRentManager().getSecondsRemaining(), "0s");
        String rentPrice = Numbers.withSuffix(shop.getRentManager().getRentPrice());
        String rentTime = Numbers.getTimeFormatted(shop.getRentManager().getSecondsPerRent());
        String maxTime = Numbers.getTimeFormatted(shop.getRentManager().getMaxRentSeconds());

        meta.setDisplayName(meta.getDisplayName()
                .replace("{time_increase}", timeFormatted)
                .replace("{rent_charge}", priceFormatted)
                .replace("{time_remaining}", timeRemaining)
                .replace("{rent_price}", rentPrice)
                .replace("{rent_time}", rentTime)
                .replace("{max_time}", maxTime)
        );

        if (meta.hasLore()) {
            ArrayList<String> lore = new ArrayList<>();
            for (String s : Objects.requireNonNull(meta.getLore())) {
                lore.add(s
                        .replace("{time_increase}", timeFormatted)
                        .replace("{rent_charge}", priceFormatted)
                        .replace("{time_remaining}", timeRemaining)
                        .replace("{rent_price}", rentPrice)
                        .replace("{rent_time}", rentTime)
                        .replace("{max_time}", maxTime)
                );
            }
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        inventory.setItem(rentItem.getSlot(), itemStack);
    }

    public void updateExtraInfoItem() {
        if (inventory == null) return;

        ItemStack itemStack = extraInfoItem.getItemStack().clone();
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;

        String levelRequired = String.valueOf(shop.getRentManager().getLevelRequired());
        String memberCapacity = String.valueOf(shop.getMemberCapacity());

        meta.setDisplayName(meta.getDisplayName()
                .replace("{level_required}", levelRequired)
                .replace("{member_cap}", memberCapacity)
        );

        if (meta.hasLore()) {
            ArrayList<String> lore = new ArrayList<>();
            for (String s : Objects.requireNonNull(meta.getLore())) {
                lore.add(s
                        .replace("{level_required}", levelRequired)
                        .replace("{member_cap}", memberCapacity)
                );
            }
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        inventory.setItem(extraInfoItem.getSlot(), itemStack);
    }

    public void updateLotteryItem() {
        if (inventory == null) return;

        // If the lottery is not active, replace it with the filler item
        if (shop.getLotteryManager().isLotteryActive()) {
            ItemStack itemStack = lotteryItem.getItemStack().clone();
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;

            String numEntries = String.valueOf(shop.getLotteryManager().numParticipants());

            meta.setDisplayName(meta.getDisplayName().replace("{entries}", numEntries));

            if (meta.hasLore()) {
                ArrayList<String> lore = new ArrayList<>();
                for (String s : Objects.requireNonNull(meta.getLore())) {
                    lore.add(s.replace("{entries}", numEntries));
                }
                meta.setLore(lore);
            }

            itemStack.setItemMeta(meta);
            inventory.setItem(lotteryItem.getSlot(), itemStack);
        }
        else {
            inventory.setItem(lotteryItem.getSlot(), filler);
        }
    }
}
