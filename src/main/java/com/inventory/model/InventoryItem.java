package com.inventory.model;

public class InventoryItem {

    private String itemName;
    private double stock;
    private String unit;
    private double minimumStock;

    public InventoryItem(String itemName, double stock, String unit, double minimumStock) {
        this.itemName = itemName;
        this.stock = stock;
        this.unit = unit;
        this.minimumStock = minimumStock;
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

    public double getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(double minimumStock) {
        this.minimumStock = minimumStock;
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