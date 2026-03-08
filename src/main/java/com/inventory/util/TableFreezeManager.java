package com.inventory.util;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SplitPane;
import javafx.collections.ObservableList;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class TableFreezeManager<T> {

    private final TableView<T> originalTable;

    private TableView<T> frozenTable;
    private TableView<T> scrollTable;
    private List<TableColumn<T, ?>> originalColumns;

    public TableFreezeManager(TableView<T> table) {
        this.originalTable = table;
    }

    public SplitPane freezeColumns(int count) {
        if (originalColumns != null) {
            throw new IllegalStateException("Columns already frozen");
        }

        int totalColumns = originalTable.getColumns().size();

        if (count <= 0 || count >= totalColumns) {
            throw new IllegalArgumentException("Invalid freeze column count");
        }

        frozenTable = new TableView<>();
        scrollTable = new TableView<>();

        ObservableList<T> items = originalTable.getItems();

        frozenTable.setItems(items);
        scrollTable.setItems(items);

        frozenTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        scrollTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        frozenTable.setFixedCellSize(28);
        scrollTable.setFixedCellSize(28);

        originalColumns = new ArrayList<>(originalTable.getColumns());

        originalTable.getColumns().clear();

        for (int i = 0; i < count && i < originalColumns.size(); i++) {
            frozenTable.getColumns().add(cloneColumn(originalColumns.get(i)));
        }

        for (int i = count; i < originalColumns.size(); i++) {
            scrollTable.getColumns().add(cloneColumn(originalColumns.get(i)));
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

        scrollTable.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<T, ?>>) change -> {
            frozenTable.getSortOrder().setAll(scrollTable.getSortOrder());
        });

        frozenTable.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<T, ?>>) change -> {
            scrollTable.getSortOrder().setAll(frozenTable.getSortOrder());
        });

        syncVerticalScroll();

        frozenTable.setStyle("-fx-padding: 0; -fx-border-width: 0 1 0 0;\n" +
                "-fx-border-color: transparent #cccccc transparent transparent;");
        scrollTable.setStyle("-fx-padding: 0;");

        VBox leftBox = new VBox(frozenTable);
        VBox rightBox = new VBox(scrollTable);

        leftBox.setStyle("-fx-spacing: 0; -fx-padding: 0;");
        rightBox.setStyle("-fx-spacing: 0; -fx-padding: 0;");

        VBox.setVgrow(frozenTable, Priority.ALWAYS);
        VBox.setVgrow(scrollTable, Priority.ALWAYS);


        SplitPane pane = new SplitPane(leftBox, rightBox);
        pane.setStyle("-fx-padding: 0; -fx-spacing: 0;");

        double frozenWidth = calculateFrozenWidth(count);
        pane.setDividerPositions(frozenWidth);

        return pane;
    }

    public void restoreOriginalTable() {

        if (originalColumns == null) return;

        originalTable.getColumns().setAll(originalColumns);

        frozenTable = null;
        scrollTable = null;
        originalColumns = null;
    }

    private void syncVerticalScroll() {

        Platform.runLater(() -> {

            ScrollBar frozenBar = null;
            ScrollBar scrollBar = null;

            for (Node node : frozenTable.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                    frozenBar = sb;
                    break;
                }
            }

            for (Node node : scrollTable.lookupAll(".scroll-bar")) {
                if (node instanceof ScrollBar sb && sb.getOrientation() == Orientation.VERTICAL) {
                    scrollBar = sb;
                    break;
                }
            }

            if (frozenBar != null && scrollBar != null) {

                // Scrollbar sync
                frozenBar.valueProperty().bindBidirectional(scrollBar.valueProperty());

                // VirtualFlow sync (prevents row mismatch)
                VirtualFlow<?> frozenFlow =
                        (VirtualFlow<?>) frozenTable.lookup(".virtual-flow");

                VirtualFlow<?> scrollFlow =
                        (VirtualFlow<?>) scrollTable.lookup(".virtual-flow");

                if (frozenFlow != null && scrollFlow != null) {
                    frozenFlow.positionProperty().bindBidirectional(
                            scrollFlow.positionProperty()
                    );
                }
            }
        });
    }

    private double calculateFrozenWidth(int count) {

        double frozenWidth = 0;

        for (int i = 0; i < count && i < frozenTable.getColumns().size(); i++) {
            frozenWidth += frozenTable.getColumns().get(i).getPrefWidth();
        }

        double tableWidth = originalTable.getWidth();
        if (tableWidth <= 0) return 0.25;

        return Math.min(frozenWidth / tableWidth, 0.5);
    }

    private TableColumn<T, ?> cloneColumn(TableColumn<T, ?> col) {

        TableColumn<T, Object> clone = new TableColumn<>(col.getText());

        clone.setId(col.getId());
        clone.setComparator((java.util.Comparator<Object>) col.getComparator());
        clone.setResizable(col.isResizable());

        clone.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<T, Object>, ObservableValue<Object>>)
                        (Callback<?, ?>) col.getCellValueFactory()
        );

        clone.setCellFactory(
                (Callback<TableColumn<T, Object>, TableCell<T, Object>>)
                        (Callback<?, ?>) col.getCellFactory()
        );

        clone.setPrefWidth(col.getPrefWidth());

        clone.setStyle(col.getStyle());
        clone.setSortable(col.isSortable());
        clone.setReorderable(false);

        return clone;
    }

    public TableView<T> getScrollTable() {
        return scrollTable;
    }

    public TableView<T> getFrozenTable() {
        return frozenTable;
    }
}