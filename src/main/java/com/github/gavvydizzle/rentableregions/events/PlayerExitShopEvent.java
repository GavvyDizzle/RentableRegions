package com.github.gavvydizzle.rentableregions.events;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event takes the state of the shop after the player has exited.
 * The shop may have new members by this time.
 */
public class PlayerExitShopEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Shop shop;
    private final World world;
    private final OfflinePlayer whoLeft;

    public PlayerExitShopEvent(OfflinePlayer whoLeft, Shop shop) {
        this.whoLeft = whoLeft;
        this.shop = shop;
        world = RentableRegions.getInstance().getShopManager().getWorld();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Shop getShop() {
        return shop;
    }

    public OfflinePlayer getWhoLeft() {
        return whoLeft;
    }

    /**
     * @return The world all shops are in
     */
    public World getWorld() {
        return world;
    }

}
