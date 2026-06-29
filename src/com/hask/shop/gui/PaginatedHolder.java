package com.hask.shop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PaginatedHolder implements InventoryHolder {

    private final PaginatedGUI gui;
    private final int page;

    public PaginatedHolder(PaginatedGUI gui, int page) {
        this.gui = gui;
        this.page = page;
    }

    public PaginatedGUI getGui() { return gui; }
    public int getPage() { return page; }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
