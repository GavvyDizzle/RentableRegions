package com.github.gavvydizzle.rentableregions.gui;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.UUID;

public class InventoryManager implements Listener {

    private final HashMap<UUID, ClickableMenu> playersInInventory;

    public InventoryManager() {
        playersInInventory = new HashMap<>();
        reload(false);
    }

    public void reload(boolean resetMenus) {
        ShopMenu.reload();
        if (resetMenus) {
            closeAllMenus();
            RentableRegions.getInstance().getShopManager().setAllShopMenusNull();
        }
    }

    /**
     * Saves the menu the player opened so clicks can be passed to it correctly
     * @param player The player
     * @param clickableMenu The menu they opened
     */
    protected void onMenuOpen(Player player, ClickableMenu clickableMenu) {
        playersInInventory.put(player.getUniqueId(), clickableMenu);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e) {
       ClickableMenu clickableMenu = playersInInventory.remove(e.getPlayer().getUniqueId());
        if (clickableMenu != null) {
            clickableMenu.closeInventory((Player) e.getPlayer());
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (playersInInventory.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);

            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                playersInInventory.get(e.getWhoClicked().getUniqueId()).handleClick(e);
            }
        }
    }

    /**
     * Closes all menus belonging to this plugin
     */
    public void closeAllMenus() {
        for (UUID uuid : playersInInventory.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.closeInventory();
        }
        playersInInventory.clear();
    }

    /**
     * Removes all players from the shop's GUI.
     * This should be called when a shop gets deleted
     * @param shop The shop
     */
    public void closeShopMenu(Shop shop) {
        ShopMenu shopMenu = shop.getShopMenu();
        HashMap<UUID, ClickableMenu> map = new HashMap<>(playersInInventory);
        for (UUID uuid : map.keySet()) {
            if (playersInInventory.get(uuid) == shopMenu) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) player.closeInventory();
            }
        }
    }

    /**
     * Opens the given menu and adds the player to the list of players with opened menus
     * @param player The player
     * @param clickableMenu The menu to open
     */
    public void openMenu(Player player, ClickableMenu clickableMenu) {
        onMenuOpen(player, clickableMenu);
        clickableMenu.openInventory(player);
    }

}
