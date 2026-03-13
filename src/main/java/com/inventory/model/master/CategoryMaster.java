package com.inventory.model.master;

public class CategoryMaster {

    private String categoryName;

    public CategoryMaster() {}

    public CategoryMaster(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
