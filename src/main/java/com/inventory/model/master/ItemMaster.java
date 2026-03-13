package com.inventory.model.master;

public class ItemMaster {

    private String itemCode;
    private String itemName;
    private String itemCategory;

    public ItemMaster() {}

    public ItemMaster(String itemCode, String itemName, String itemCategory) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemCategory = itemCategory;
    }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getItemCategory() { return itemCategory; }

    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
