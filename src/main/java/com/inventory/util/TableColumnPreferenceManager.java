package com.inventory.util;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.prefs.Preferences;

public class TableColumnPreferenceManager<T> {

    private final TableView<T> table;
    private final Preferences prefs;
    private final String key;
    private final BooleanSupplier saveCondition;
    private final List<TableColumn<T, ?>> originalOrder = new ArrayList<>();

    public TableColumnPreferenceManager(
            TableView<T> table,
            String key,
            Class<?> clazz,
            BooleanSupplier saveCondition
    ) {
        this.table = table;
        this.key = key;
        this.prefs = Preferences.userNodeForPackage(clazz);
        this.saveCondition = saveCondition;
    }

    public void initialize() {

        if (originalOrder.isEmpty()) {
            originalOrder.addAll(table.getColumns());
        }

        restoreColumnOrder();

        table.getColumns().addListener(
                (ListChangeListener<TableColumn<T, ?>>) change -> {

                    if (saveCondition == null || saveCondition.getAsBoolean()) {
                        saveColumnOrder();
                    }
                }
        );
    }

    private void saveColumnOrder() {

        StringBuilder order = new StringBuilder();

        for (TableColumn<?, ?> column : table.getColumns()) {

            if (column.getId() != null) {
                order.append(column.getId()).append(",");
            }
        }
        System.out.println("Saving column order with key: " + key);
        System.out.println("Order: " + order);

        String activeKey = table.getProperties()
                .getOrDefault("columnOrderKey", key)
                .toString();

        prefs.put(activeKey, order.toString());

        System.out.println("Saving column order with key: " + activeKey);
    }

    private String getCurrentKey() {
        return table.getProperties().getOrDefault("columnOrderKey", key).toString();
    }

    private void restoreColumnOrder() {

        String order = prefs.get(key, null);

        var columns = table.getColumns();

        // Reset to original order first
        columns.setAll(originalOrder);

        if (order == null) return;

        String[] ids = order.split(",");

        List<TableColumn<T, ?>> reordered = new ArrayList<>();

        for (String id : ids) {

            columns.stream()
                    .filter(c -> id.equals(c.getId()))
                    .findFirst()
                    .ifPresent(reordered::add);
        }

        for (TableColumn<T, ?> col : columns) {
            if (!reordered.contains(col)) {
                reordered.add(col);
            }
        }

        columns.setAll(reordered);
    }

    public void restoreForKey(String newKey) {
        System.out.println("restoreForKey() called with key: " + newKey);

        String order = prefs.get(newKey, null);

        System.out.println("Stored order: " + order);

        if (order == null) return;

        String[] ids = order.split(",");

        var columns = table.getColumns();

        List<TableColumn<T, ?>> reordered = new ArrayList<>();

        for (String id : ids) {

            columns.stream()
                    .filter(c -> id.equals(c.getId()))
                    .findFirst()
                    .ifPresent(reordered::add);
        }

        for (TableColumn<T, ?> col : columns) {
            if (!reordered.contains(col)) {
                reordered.add(col);
            }
        }

        columns.setAll(reordered);

        System.out.println("Final restored order:");

        table.getColumns().forEach(c ->
                System.out.println("  " + c.getId())
        );
    }
}