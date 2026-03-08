package com.inventory.util;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.HashSet;
import java.util.Set;

public class TableColumnFreezer<T> {

    private final TableView<T> table;
    private ScrollBar horizontalBar;

    private final Set<Integer> frozenColumns = new HashSet<>();

    public TableColumnFreezer(TableView<T> table) {
        this.table = table;

        // Wait until TableView skin is ready
        Platform.runLater(this::findScrollbar);
    }

    private void findScrollbar() {

        for (Node node : table.lookupAll(".scroll-bar")) {

            if (node instanceof ScrollBar sb &&
                    sb.getOrientation() == Orientation.HORIZONTAL) {

                horizontalBar = sb;
                break;
            }
        }

        if (horizontalBar == null) {
            // try again next pulse
            Platform.runLater(this::findScrollbar);
        }
    }

    public void freezeColumns(int count) {

        if (horizontalBar == null) {
            System.out.println("Horizontal scrollbar not ready yet.");
            return;
        }

        frozenColumns.clear();

        for (int i = 0; i < count; i++) {
            frozenColumns.add(i);
        }

        horizontalBar.valueProperty().addListener((obs, oldVal, newVal) -> update());

        update();
    }

    public void unfreezeColumns() {

        frozenColumns.clear();

        table.lookupAll(".table-cell").forEach(n -> n.setTranslateX(0));
        table.lookupAll(".column-header").forEach(n -> n.setTranslateX(0));
    }

    private void update() {

        if (horizontalBar == null) return;

        double offset = horizontalBar.getValue() * getScrollableWidth();

        table.lookupAll(".table-cell").forEach(node -> {

            Integer columnIndex =
                    (Integer) node.getProperties().get("TableColumnIndex");

            if (columnIndex != null && frozenColumns.contains(columnIndex)) {
                node.setTranslateX(offset);
            }
        });

        table.lookupAll(".column-header").forEach(node -> {

            Integer columnIndex =
                    (Integer) node.getProperties().get("TableColumnIndex");

            if (columnIndex != null && frozenColumns.contains(columnIndex)) {
                node.setTranslateX(offset);
            }
        });
    }

    private double getScrollableWidth() {

        double totalWidth = table.getColumns().stream()
                .mapToDouble(TableColumn::getWidth)
                .sum();

        return Math.max(0, totalWidth - table.getWidth());
    }
}