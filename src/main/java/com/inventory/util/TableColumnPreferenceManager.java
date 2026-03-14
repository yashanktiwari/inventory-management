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
    private boolean savingEnabled = true;
    private ListChangeListener<TableColumn<T, ?>> columnListener;

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

        columnListener = (ListChangeListener<TableColumn<T,?>>) change -> {
            if(savingEnabled && (saveCondition == null || saveCondition.getAsBoolean())) {
                saveColumnOrder();
            }
        };

        table.getColumns().addListener(columnListener);
    }

    public void setSavingEnabled(boolean enabled) {
        this.savingEnabled = enabled;
    }

    private void saveColumnOrder() {

        StringBuilder order = new StringBuilder();

        for (TableColumn<?, ?> column : table.getColumns()) {

            if (column.getId() != null) {
                order.append(column.getId()).append(",");
            }
        }

        String activeKey = table.getProperties()
                .getOrDefault("columnOrderKey", key)
                .toString();

        prefs.put(activeKey, order.toString());
    }

    private String getCurrentKey() {
        return table.getProperties().getOrDefault("columnOrderKey", key).toString();
    }

    private void restoreColumnOrder() {

        String currentKey = getCurrentKey();
        String order = prefs.get(currentKey, null);

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

        String order = prefs.get(newKey, null);

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
    }
}