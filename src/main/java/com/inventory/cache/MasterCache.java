package com.inventory.cache;

import com.inventory.dao.MasterDAO;
import com.inventory.model.master.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterCache {

    private static final MasterDAO masterDAO = new MasterDAO();

    public static Map<String, ItemMaster> itemCache = new HashMap<>();
    public static Map<String, EmployeeMaster> employeeCache = new HashMap<>();
    public static Map<String, CategoryMaster> categoryCache = new HashMap<>();
    public static Map<String, PlantMaster> plantCache = new HashMap<>();
    public static Map<String, DepartmentMaster> departmentCache = new HashMap<>();


    public static void loadCache() {

        itemCache.clear();
        employeeCache.clear();
        categoryCache.clear();
        plantCache.clear();
        departmentCache.clear();

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

        for(PlantMaster plant : masterDAO.getAllPlants()) {
            plantCache.put(plant.getPlantName().toLowerCase(), plant);
        }

        for (DepartmentMaster d : masterDAO.getAllDepartments()) {
            departmentCache.put(
                    d.getDepartmentName().toLowerCase(),
                    d
            );
        }
    }
}
