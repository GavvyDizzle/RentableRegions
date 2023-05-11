package com.github.gavvydizzle.rentableregions.gui;

import org.bukkit.inventory.ItemStack;

public class InventoryItem {

    private final ItemStack itemStack;
    private final int slot;

    public InventoryItem(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getSlot() {
        return slot;
    }
}
