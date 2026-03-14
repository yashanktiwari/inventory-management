package com.inventory.ui.controller;

import com.inventory.dao.IndentDAO;
import com.inventory.model.Indent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class IndentListController {

    @FXML
    private TableView<Indent> table;

    @FXML
    private TableColumn<Indent, Void> rowNumberCol;

    @FXML
    private TableColumn<Indent, String> itemCodeCol;

    @FXML
    private TableColumn<Indent, String> itemNameCol;

    @FXML
    private TableColumn<Indent, Double> quantityCol;

    private final IndentDAO dao = new IndentDAO();


    public void initialize() {

        itemCodeCol.setCellValueFactory(
                new PropertyValueFactory<>("itemCode"));

        itemNameCol.setCellValueFactory(
                new PropertyValueFactory<>("itemName"));

        quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("quantity"));

        rowNumberCol.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        loadData();

        ContextMenu menu = new ContextMenu();

        MenuItem removeItem = new MenuItem("Remove Indent");

        menu.getItems().add(removeItem);

        table.setRowFactory(tv -> {

            TableRow<Indent> row = new TableRow<>();

            row.setOnContextMenuRequested(event -> {

                if (!row.isEmpty()) {

                    table.getSelectionModel().select(row.getIndex());

                    menu.show(
                            row,
                            event.getScreenX(),
                            event.getScreenY()
                    );
                }
            });

            return row;
        });

        removeItem.setOnAction(e -> {

            Indent selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            dao.deleteIndent(selected.getId());

            table.getItems().remove(selected);

            loadData();   // refresh table
        });
    }


    private void loadData() {

        List<Indent> list = dao.getAllIndents();

        table.setItems(
                FXCollections.observableArrayList(list)
        );
    }
}