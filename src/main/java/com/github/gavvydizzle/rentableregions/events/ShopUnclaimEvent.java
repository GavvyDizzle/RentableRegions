package com.github.gavvydizzle.rentableregions.events;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event fires before any modifications to the shop have been made.
 */
public class ShopUnclaimEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Shop shop;
    private final World world;
    private boolean setToAir;

    public ShopUnclaimEvent(Shop shop) {
        this.shop = shop;
        world = RentableRegions.getInstance().getShopManager().getWorld();
        setToAir = false;
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

    /**
     * @return The world all shops are in
     */
    public World getWorld() {
        return world;
    }

    /**
     * Change if this shop should set all of its blocks to air.
     * The default value is FALSE
     * @param setToAir The new value
     */
    public void setSetToAir(boolean setToAir) {
        this.setToAir = setToAir;
    }

    public boolean isSetToAir() {
        return setToAir;
    }
}
