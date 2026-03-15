package com.inventory.ui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BaseDialog extends Stage {

    protected BorderPane root = new BorderPane();
    protected HBox footer = new HBox(10);

    protected Button btnPrimary = new Button("Save");
    protected Button btnCancel = new Button("Cancel");

    public BaseDialog(String title, Node content) {

        setTitle(title);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.DECORATED);

        root.setPadding(new Insets(20));

        footer.getChildren().addAll(btnPrimary, btnCancel);
        footer.setPadding(new Insets(15,0,0,0));

        root.setCenter(content);
        root.setBottom(footer);

        btnCancel.setOnAction(e -> close());

        setScene(new javafx.scene.Scene(root));
    }
}