package com.inventory.util;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane;
import javafx.collections.ObservableList;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class TableFreezeManager<T> {

    private final TableView<T> originalTable;

    private TableView<T> frozenTable;
    private TableView<T> scrollTable;

    public TableFreezeManager(TableView<T> table) {
        this.originalTable = table;
    }

    public SplitPane freezeColumns(int count) {

        frozenTable = new TableView<>();
        scrollTable = new TableView<>();

        ObservableList<T> items = originalTable.getItems();

        frozenTable.setItems(items);
        scrollTable.setItems(items);

        frozenTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        scrollTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        List<TableColumn<T, ?>> columns = new ArrayList<>(originalTable.getColumns());

        originalTable.getColumns().clear();

        for (int i = 0; i < count && i < columns.size(); i++) {
            frozenTable.getColumns().add(cloneColumn(columns.get(i)));
        }

        for (int i = count; i < columns.size(); i++) {
            scrollTable.getColumns().add(cloneColumn(columns.get(i)));
        }

        frozenTable.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
            if (scrollTable.getSelectionModel().getSelectedIndex() != n.intValue()) {
                scrollTable.getSelectionModel().select(n.intValue());
            }
        });

        scrollTable.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> {
            if (frozenTable.getSelectionModel().getSelectedIndex() != n.intValue()) {
                frozenTable.getSelectionModel().select(n.intValue());
            }
        });

        syncVerticalScroll();

        VBox leftBox = new VBox(frozenTable);
        VBox rightBox = new VBox(scrollTable);

        VBox.setVgrow(frozenTable, Priority.ALWAYS);
        VBox.setVgrow(scrollTable, Priority.ALWAYS);

        SplitPane pane = new SplitPane(leftBox, rightBox);

        pane.setDividerPositions(0.25);

        return pane;
    }

    public void restoreOriginalTable() {

        if (frozenTable == null || scrollTable == null) return;

        List<TableColumn<T, ?>> columns = new ArrayList<>();

        columns.addAll(frozenTable.getColumns());
        columns.addAll(scrollTable.getColumns());

        frozenTable.getColumns().clear();
        scrollTable.getColumns().clear();

        originalTable.getColumns().addAll(columns);
    }

    private void syncVerticalScroll() {

        frozenTable.skinProperty().addListener((obs, oldSkin, newSkin) -> {

            ScrollBar frozenBar =
                    (ScrollBar) frozenTable.lookup(".scroll-bar:vertical");

            ScrollBar scrollBar =
                    (ScrollBar) scrollTable.lookup(".scroll-bar:vertical");

            if (frozenBar != null && scrollBar != null) {

                frozenBar.valueProperty().bindBidirectional(
                        scrollBar.valueProperty()
                );
            }
        });
    }

    @SuppressWarnings("unchecked")
    private TableColumn<T, ?> cloneColumn(TableColumn<T, ?> col) {

        TableColumn<T, Object> clone = new TableColumn<>(col.getText());

        clone.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<T, Object>, ObservableValue<Object>>)
                        (Callback<?, ?>) col.getCellValueFactory()
        );

        clone.setCellFactory(
                (Callback<TableColumn<T, Object>, TableCell<T, Object>>)
                        (Callback<?, ?>) col.getCellFactory()
        );

        clone.setPrefWidth(col.getPrefWidth());

        return clone;
    }
}