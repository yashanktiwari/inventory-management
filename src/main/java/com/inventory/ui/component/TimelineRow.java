package com.inventory.ui.component;

import com.inventory.model.WorkLog;
import com.inventory.model.WorkType;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class TimelineRow extends HBox {

    public TimelineRow(WorkLog log) {

        setSpacing(10);
        setAlignment(Pos.TOP_LEFT);

        // vertical timeline container
        StackPane timeline = new StackPane();
        timeline.setMinWidth(40);
        timeline.setMaxWidth(40);

        Region line = new Region();
        line.getStyleClass().add("timeline-line");

        Region dot = new Region();

        if (log.getWorkType() == WorkType.ACHIEVEMENT) {
            dot.getStyleClass().add("timeline-dot-achievement");
        } else {
            dot.getStyleClass().add("timeline-dot-regular");
        }

        timeline.getChildren().addAll(line, dot);

        WorkCard card = new WorkCard(log);

        getChildren().addAll(timeline, card);
    }
}