package com.inventory.ui.dialog;

import com.inventory.MainApp;
import com.inventory.dao.WorkLogDAO;
import com.inventory.model.WorkLog;
import com.inventory.model.WorkType;
import com.inventory.ui.component.DateHeader;
import com.inventory.ui.component.TimelineRow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.List;

public class WorkLogDialog {

    private VBox cardContainer;

    private ComboBox<String> typeFilter;
    private DatePicker fromDate;
    private DatePicker toDate;

    private WorkLogDAO dao = new WorkLogDAO();

    public static void show() {
        new WorkLogDialog().createUI();
    }

    private void createUI() {

        Stage stage = new Stage();
        stage.setTitle("Work Log");

        BorderPane root = new BorderPane();

        root.setTop(createHeader());
        root.setCenter(createFeed());

        Scene scene = new Scene(root, 900, 600);

        // attach CSS only here
        scene.getStylesheets().add(MainApp.GLOBAL_CSS);
        scene.getStylesheets().add(
                getClass().getResource("/css/worklog.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();

        loadData();
    }

    private Node createHeader() {

        Label title = new Label("Work Activity Feed");
        title.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        Label subtitle = new Label("Track important work and achievements");
        subtitle.setStyle("-fx-text-fill:#6b7280;");

        VBox titleBox = new VBox(4, title, subtitle);

        Button addButton = new Button("+ Add Work");

        addButton.setOnAction(e ->
                AddWorkDialog.show(() -> loadData())
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, titleBox, spacer, addButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All", "Regular", "Achievement");
        typeFilter.setValue("All");
        typeFilter.setPromptText("Filter type");
        typeFilter.setPrefWidth(160);

        fromDate = new DatePicker();
        fromDate.setPromptText("From date");
        fromDate.setPrefWidth(150);

        toDate = new DatePicker();
        toDate.setPromptText("To date");
        toDate.setPrefWidth(150);

        Button apply = new Button("Apply");
        apply.getStyleClass().add("primary-button");
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary-button");
        clear.disableProperty().bind(
                typeFilter.valueProperty().isEqualTo("All")
                        .and(fromDate.valueProperty().isNull())
                        .and(toDate.valueProperty().isNull())
        );

        FontIcon filterIcon = new FontIcon("fas-filter");
        FontIcon calendarIcon1 = new FontIcon("fas-calendar");
        FontIcon calendarIcon2 = new FontIcon("fas-calendar");

        apply.setOnAction(e -> loadData());

        clear.setOnAction(e -> {

            typeFilter.setValue("All");

            fromDate.setValue(null);
            toDate.setValue(null);

            loadData();
        });

        typeFilter.setOnAction(e -> loadData());
        fromDate.setOnAction(e -> loadData());
        toDate.setOnAction(e -> loadData());

        HBox typeBox = new HBox(6, filterIcon, typeFilter);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        HBox fromBox = new HBox(6, calendarIcon1, fromDate);
        fromBox.setAlignment(Pos.CENTER_LEFT);

        HBox toBox = new HBox(6, calendarIcon2, toDate);
        toBox.setAlignment(Pos.CENTER_LEFT);

//        HBox filters = new HBox(24, typeBox, fromBox, toBox, apply, clear);
        Region space1 = new Region();
        space1.setMinWidth(16);

        Region space2 = new Region();
        space2.setMinWidth(8);

        Region pushRight = new Region();
        HBox.setHgrow(pushRight, Priority.ALWAYS);

        HBox filters = new HBox(
                10,
                typeBox,
                space1,
                fromBox,
                toBox,
                apply,
                clear
        );

        filters.setPadding(new Insets(12,20,12,20));
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("filter-toolbar");

        VBox header = new VBox(topBar, filters);

        return header;
    }

    private Node createFeed() {

        cardContainer = new VBox(12);
        cardContainer.setFillWidth(true);
        cardContainer.setPadding(new Insets(10, 0, 20, 0));

        VBox feedWrapper = new VBox(cardContainer);
        feedWrapper.getStyleClass().add("feed-container");
        feedWrapper.setAlignment(Pos.TOP_LEFT);
        feedWrapper.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(feedWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background:#f3f4f6;");

        return scrollPane;
    }

    private void loadData() {

        cardContainer.getChildren().clear();

        WorkType filterType = null;

        String value = typeFilter.getValue();

        if (value != null) {
            switch (value.trim().toUpperCase()) {
                case "REGULAR":
                    filterType = WorkType.REGULAR;
                    break;
                case "ACHIEVEMENT":
                    filterType = WorkType.ACHIEVEMENT;
                    break;
            }
        }

        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        List<WorkLog> logs = dao.getWorkLogs(filterType, from, to);

        LocalDate currentGroupDate = null;

        for (WorkLog log : logs) {

            LocalDate logDate = log.getCreatedAt().toLocalDate();

            // create date header when date changes
            if (currentGroupDate == null || !currentGroupDate.equals(logDate)) {

                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                String headerText;

                if (logDate.equals(today)) {
                    headerText = "Today";
                } else if (logDate.equals(yesterday)) {
                    headerText = "Yesterday";
                } else {
                    headerText = logDate.format(
                            java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
                    );
                }

                DateHeader header = new DateHeader(headerText);

                cardContainer.getChildren().add(header);

                currentGroupDate = logDate;
            }

            TimelineRow row = new TimelineRow(log);

            VBox.setMargin(row, new Insets(4,10,4,10));

            cardContainer.getChildren().add(row);
        }
    }
}