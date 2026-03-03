package com.inventory.model;

public class Item {

    private String itemId;
    private String itemName;
    private String description;

    public Item() {}

    public Item(String itemName, String description) {
        this.itemName = itemName;
        this.description = description;
    }

    public Item(String itemId, String itemName, String description) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
    }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return itemName;
    }
}