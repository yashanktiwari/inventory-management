package com.inventory.ui.component;

import com.inventory.model.WorkLog;
import com.inventory.model.WorkType;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

public class WorkCard extends VBox {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a");

    public WorkCard(WorkLog log) {
        setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(this, Priority.ALWAYS);

        getStyleClass().add("work-card");

        if (log.getWorkType() == WorkType.ACHIEVEMENT) {
            getStyleClass().add("work-achievement");
        } else {
            getStyleClass().add("work-regular");
        }

        createUI(log);

        playAnimation();
    }

    private void createUI(WorkLog log) {

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon;

        if (log.getWorkType() == WorkType.ACHIEVEMENT) {
            icon = new FontIcon("fas-star");
            icon.setIconSize(16);
            icon.setStyle("-fx-icon-color:#f59e0b;");
        } else {
            icon = new FontIcon("fas-tasks");
            icon.setIconSize(16);
            icon.setStyle("-fx-icon-color:#3b82f6;");
        }

        Label typeLabel = new Label(log.getWorkType().name());

        header.getChildren().addAll(icon, typeLabel);

        Label details = new Label(log.getDetails());
        details.getStyleClass().add("work-details");
        details.setWrapText(true);

        FontIcon userIcon = new FontIcon("fas-user");
        userIcon.setIconSize(12);

        FontIcon timeIcon = new FontIcon("fas-clock");
        timeIcon.setIconSize(12);

        Label meta = new Label(
                log.getUsername() + " • " +
                        log.getCreatedAt().format(FORMATTER)
        );

        meta.getStyleClass().add("work-meta");

        HBox footer = new HBox(6, userIcon, meta, timeIcon);
        footer.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, details, footer);
    }

    private void playAnimation() {

        FadeTransition fade = new FadeTransition(Duration.millis(300), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}