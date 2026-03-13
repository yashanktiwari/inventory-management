package com.inventory.model.master;

public class ItemMaster {

    private String itemCode;
    private String itemName;
    private String itemMake;
    private String itemModel;
    private String itemCategory;

    public ItemMaster() {}

    public ItemMaster(String itemCode, String itemName,
                      String itemMake, String itemModel, String itemCategory) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.itemMake = itemMake;
        this.itemModel = itemModel;
        this.itemCategory = itemCategory;
    }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getItemMake() { return itemMake; }
    public String getItemModel() { return itemModel; }
    public String getItemCategory() { return itemCategory; }

    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemMake(String itemMake) { this.itemMake = itemMake; }
    public void setItemModel(String itemModel) { this.itemModel = itemModel; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
}
