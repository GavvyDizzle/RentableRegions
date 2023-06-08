package com.github.gavvydizzle.rentableregions.api;

import com.github.gavvydizzle.rentableregions.RentableRegions;
import com.github.gavvydizzle.rentableregions.shop.Shop;
import com.github.gavvydizzle.rentableregions.shop.ShopManager;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nullable;

public class RentableRegionsAPI {

    private static RentableRegionsAPI instance;
    private final ShopManager shopManager;

    private RentableRegionsAPI(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    /**
     * Accesses the api instance.
     * Might be null if this method is called when {@link com.github.gavvydizzle.rentableregions.RentableRegions}'s startup method is still being executed.
     *
     * @return The instance of this api
     * @since 1.0.0
     */
    @Nullable
    public static RentableRegionsAPI getInstance() {
        if (instance == null) {
            try {
                instance = new RentableRegionsAPI(RentableRegions.getInstance().getShopManager());
            } catch (Exception e) {
                RentableRegions.getInstance().getLogger().severe("Failed to create the API. You must wait until this plugin is done loading to get an instance.");
                instance = null;
            }
        }
        return instance;
    }


    /**
     * Gets the shop at this location.
     * If multiple shops overlap this location, the first one found will be returned.
     * It is assumed that no shops will have overlapping regions.
     * @param location The location
     * @return The shop at this location or null if none exists
     */
    @Nullable
    public Shop getShop(Location location) {
        return shopManager.getShopFromLocation(location);
    }

    /**
     * Gets the shop containing this block.
     * If multiple shops overlap this block, the first one found will be returned.
     * It is assumed that no shops will have overlapping regions.
     * @param block The block
     * @return The shop at this block or null if none exists
     */
    @Nullable
    public Shop getShop(Block block) {
        return getShop(block.getLocation());
    }

}
