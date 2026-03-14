package com.inventory.ui.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.*;
import java.util.prefs.Preferences;

public class ColumnVisibilityDialog {

    public static <T> void show(
            TableView<T> table,
            Class<?> prefsClass,
            String currentTab,
            List<String> tabs
    ) {

        ComboBox<String> tabSelector = new ComboBox<>();
        tabSelector.getItems().addAll(tabs);
        tabSelector.setValue(currentTab);
        tabSelector.setPrefWidth(180);

        Label tabLabel = new Label("Tab:");

        HBox tabHeader = new HBox(10, tabLabel, tabSelector);
        tabHeader.setAlignment(Pos.CENTER_LEFT);

        Preferences prefs = Preferences.userNodeForPackage(prefsClass);

        ObservableList<String> hiddenMaster = FXCollections.observableArrayList();
        ObservableList<String> visibleMaster = FXCollections.observableArrayList();

        ListView<String> hiddenList = new ListView<>();
        ListView<String> visibleList = new ListView<>();

        hiddenList.setCursor(Cursor.HAND);
        visibleList.setCursor(Cursor.HAND);

        hiddenList.setOnDragDetected(event -> {

            String selected = hiddenList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Dragboard db = hiddenList.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(selected);

            db.setContent(content);

            event.consume();
        });

        visibleList.setOnDragDetected(event -> {

            String selected = visibleList.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Dragboard db = visibleList.startDragAndDrop(TransferMode.MOVE);

            ClipboardContent content = new ClipboardContent();
            content.putString(selected);

            db.setContent(content);

            event.consume();
        });

        hiddenList.setOnDragOver(event -> {

            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        hiddenList.setOnDragDropped(event -> {

            String dragged = event.getDragboard().getString();

            if (dragged != null) {

                visibleMaster.remove(dragged);

                if (!hiddenMaster.contains(dragged)) {
                    hiddenMaster.add(dragged);
                }
            }

            event.setDropCompleted(true);
            event.consume();
        });

        visibleList.setOnDragOver(event -> {

            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        visibleList.setOnDragDropped(event -> {

            String dragged = event.getDragboard().getString();

            if (dragged != null) {

                hiddenMaster.remove(dragged);

                if (!visibleMaster.contains(dragged)) {
                    visibleMaster.add(dragged);
                }
            }

            event.setDropCompleted(true);
            event.consume();
        });

        CustomTextField hiddenSearch = new CustomTextField();
        hiddenSearch.setPromptText("Search...");

        CustomTextField visibleSearch = new CustomTextField();
        visibleSearch.setPromptText("Search...");

        Button visibleClear = new Button("✖");
        visibleClear.setCursor(Cursor.HAND);
        visibleClear.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 11;"
        );
        visibleClear.setOnAction(e -> visibleSearch.clear());

        visibleClear.visibleProperty().bind(visibleSearch.textProperty().isNotEmpty());
        visibleSearch.setRight(visibleClear);

        Button hiddenClear = new Button("✖");
        hiddenClear.setCursor(Cursor.HAND);
        hiddenClear.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-font-size: 11;"
        );
        hiddenClear.setOnAction(e -> hiddenSearch.clear());

        hiddenClear.visibleProperty().bind(hiddenSearch.textProperty().isNotEmpty());
        hiddenSearch.setRight(hiddenClear);

        hiddenList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        visibleList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Map<String, TableColumn<T, ?>> columnMap = new HashMap<>();

        Runnable loadColumns = () -> {

            hiddenMaster.clear();
            visibleMaster.clear();
            columnMap.clear();

            String selectedTab = tabSelector.getValue();

            for (TableColumn<T, ?> col : table.getColumns()) {

                if (col.getId() == null) continue;

                columnMap.put(col.getText(), col);

                boolean visible =
                        prefs.getBoolean(selectedTab + "_column_visible_" + col.getId(), true);

                if (visible) {
                    visibleMaster.add(col.getText());
                } else {
                    hiddenMaster.add(col.getText());
                }
            }
        };

        loadColumns.run();

        tabSelector.valueProperty().addListener((obs, oldTab, newTab) -> {
            loadColumns.run();
        });

        FilteredList<String> hiddenFiltered =
                new FilteredList<>(hiddenMaster, s -> true);

        FilteredList<String> visibleFiltered =
                new FilteredList<>(visibleMaster, s -> true);

        hiddenSearch.textProperty().addListener((obs, oldVal, newVal) -> {

            hiddenFiltered.setPredicate(item -> {

                if (newVal == null || newVal.isBlank()) {
                    return true;
                }

                return item.toLowerCase().contains(newVal.toLowerCase());
            });

        });

        visibleSearch.textProperty().addListener((obs, oldVal, newVal) -> {

            visibleFiltered.setPredicate(item -> {

                if (newVal == null || newVal.isBlank()) {
                    return true;
                }

                return item.toLowerCase().contains(newVal.toLowerCase());
            });

        });

        hiddenList.setItems(hiddenFiltered);
        visibleList.setItems(visibleFiltered);

        Button addBtn = new Button(">>");
        Button removeBtn = new Button("<<");

        addBtn.setOnAction(e -> {

            List<String> selected =
                    new ArrayList<>(hiddenList.getSelectionModel().getSelectedItems());

            hiddenMaster.removeAll(selected);
            visibleMaster.addAll(selected);
        });

        removeBtn.setOnAction(e -> {

            List<String> selected =
                    new ArrayList<>(visibleList.getSelectionModel().getSelectedItems());

            visibleMaster.removeAll(selected);
            hiddenMaster.addAll(selected);
        });

        visibleList.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2) {
                removeBtn.fire();
            }
        });

        hiddenList.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2) {
                addBtn.fire();
            }
        });

        VBox buttonBox = new VBox(10, addBtn, removeBtn);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        VBox hiddenBox = new VBox(
                5,
                new Label("Hidden Columns"),
                hiddenSearch,
                hiddenList
        );

        VBox visibleBox = new VBox(
                5,
                new Label("Visible Columns"),
                visibleSearch,
                visibleList
        );

        hiddenBox.setPrefWidth(200);
        visibleBox.setPrefWidth(200);

        HBox lists = new HBox(15, hiddenBox, buttonBox, visibleBox);

        Button cancelBtn = new Button("Cancel");
        Button restoreBtn = new Button("Restore Default");
        Button saveBtn = new Button("Save");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(10, restoreBtn, spacer, cancelBtn, saveBtn);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(15, tabHeader, lists, bottom);
        root.setPadding(new Insets(15));

        Stage stage = new Stage();
        stage.setTitle("Column Visibility Manager");

        restoreBtn.setOnAction(e -> {

            String selectedTab = tabSelector.getValue();

            for (TableColumn<T, ?> col : table.getColumns()) {

                if (col.getId() == null) continue;

                prefs.remove(selectedTab + "_column_visible_" + col.getId());

                if (selectedTab.equals(currentTab)) {
                    col.setVisible(true);
                }
            }

            loadColumns.run();
        });

        cancelBtn.setOnAction(e -> stage.close());

        saveBtn.setOnAction(e -> {

            String selectedTab = tabSelector.getValue();

            List<String> visible = new ArrayList<>(visibleMaster);

            for (String name : columnMap.keySet()) {

                TableColumn<T, ?> col = columnMap.get(name);

                boolean isVisible = visible.contains(name);

                if (col.getId() != null) {
                    prefs.putBoolean(selectedTab + "_column_visible_" + col.getId(), isVisible);
                }

                if (selectedTab.equals(currentTab)) {
                    col.setVisible(isVisible);
                }
            }

            stage.close();
        });

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}