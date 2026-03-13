package com.inventory.util;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.function.BooleanSupplier;
import java.util.prefs.Preferences;

public class TableColumnPreferenceManager<T> {

    private final TableView<T> table;
    private final Preferences prefs;
    private final String key;
    private final BooleanSupplier saveCondition;

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

        prefs.put(key, order.toString());
    }

    private void restoreColumnOrder() {

        String order = prefs.get(key, null);

        if (order == null) return;

        String[] ids = order.split(",");

        var columns = table.getColumns();

        for (String id : ids) {

            columns.stream()
                    .filter(c -> id.equals(c.getId()))
                    .findFirst()
                    .ifPresent(col -> {
                        columns.remove(col);
                        columns.add(col);
                    });
        }
    }
}