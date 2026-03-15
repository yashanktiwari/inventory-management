package com.inventory.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class DateHeader extends HBox {

    public DateHeader(String dateText) {

        Label label = new Label(dateText);

        label.getStyleClass().add("timeline-date");

        setPadding(new Insets(20,10,5,10));

        getChildren().add(label);
    }
}