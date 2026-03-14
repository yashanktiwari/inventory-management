package com.inventory.ui.table;

import com.inventory.model.TransactionHistory;

public class DashboardSearchFilter {

    public static boolean matches(TransactionHistory history, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        keyword = keyword.toLowerCase();

        return

                (history.getBuySell() != null &&
                        history.getBuySell().toLowerCase().contains(keyword))

                        || (history.getPlant() != null &&
                        history.getPlant().toLowerCase().contains(keyword))

                        || (history.getDepartment() != null &&
                        history.getDepartment().toLowerCase().contains(keyword))

                        || (history.getLocation() != null &&
                        history.getLocation().toLowerCase().contains(keyword))

                        || (history.getEmployeeCode() != null &&
                        history.getEmployeeCode().toLowerCase().contains(keyword))

                        || (history.getEmployeeName() != null &&
                        history.getEmployeeName().toLowerCase().contains(keyword))

                        || (history.getIpAddress() != null &&
                        history.getIpAddress().toLowerCase().contains(keyword))

                        || (history.getItemCode() != null &&
                        history.getItemCode().toLowerCase().contains(keyword))

                        || (history.getItemName() != null &&
                        history.getItemName().toLowerCase().contains(keyword))

                        || (history.getItemMake() != null &&
                        history.getItemMake().toLowerCase().contains(keyword))

                        || (history.getItemModel() != null &&
                        history.getItemModel().toLowerCase().contains(keyword))

                        || (history.getItemSerial() != null &&
                        history.getItemSerial().toLowerCase().contains(keyword))

                        || (history.getImeiNo() != null &&
                        history.getImeiNo().toLowerCase().contains(keyword))

                        || (history.getSimNo() != null &&
                        history.getSimNo().toLowerCase().contains(keyword))

                        || (history.getPoNo() != null &&
                        history.getPoNo().toLowerCase().contains(keyword))

                        || (history.getPartyName() != null &&
                        history.getPartyName().toLowerCase().contains(keyword))

                        || (history.getStatus() != null &&
                        history.getStatus().toLowerCase().contains(keyword))

                        || (history.getItemCategory() != null &&
                        history.getItemCategory().toLowerCase().contains(keyword))

                        || (history.getItemLocation() != null &&
                        history.getItemLocation().toLowerCase().contains(keyword))

                        || (history.getItemCondition() != null &&
                        history.getItemCondition().toLowerCase().contains(keyword));
    }
}