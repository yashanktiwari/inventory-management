package com.inventory.model;

public class InventoryItem {

    private String itemName;
    private double stock;
    private String unit;

    public InventoryItem(String itemName, double stock, String unit) {
        this.itemName = itemName;
        this.stock = stock;
        this.unit = unit;
    }

    public String getItemName() {
        return itemName;
    }

    public double getStock() {
        return stock;
    }

    public String getUnit() {
        return unit;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}