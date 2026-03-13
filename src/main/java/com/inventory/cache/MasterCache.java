package com.inventory.cache;

import com.inventory.dao.MasterDAO;
import com.inventory.model.master.CategoryMaster;
import com.inventory.model.master.EmployeeMaster;
import com.inventory.model.master.ItemMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterCache {

    private static final MasterDAO masterDAO = new MasterDAO();

    public static Map<String, ItemMaster> itemCache = new HashMap<>();
    public static Map<String, EmployeeMaster> employeeCache = new HashMap<>();
    public static Map<String, CategoryMaster> categoryCache = new HashMap<>();


    public static void loadCache() {

        itemCache.clear();
        employeeCache.clear();
        categoryCache.clear();

        List<ItemMaster> items = masterDAO.getAllItems();
        for(ItemMaster item : items) {
            itemCache.put(item.getItemCode().toLowerCase(), item);
        }

        List<EmployeeMaster> employees = masterDAO.getAllEmployees();
        for(EmployeeMaster emp : employees) {
            employeeCache.put(emp.getEmployeeCode().toLowerCase(), emp);
        }

        List<CategoryMaster> categories = masterDAO.getAllCategories();
        for(CategoryMaster c : categories) {
            categoryCache.put(c.getCategoryName().toLowerCase(), c);
        }


        System.out.println("Master cache loaded");
    }
}
