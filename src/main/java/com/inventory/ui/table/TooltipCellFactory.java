package com.inventory.ui.table;

import com.inventory.model.TransactionHistory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class TooltipCellFactory {

    public static Callback<TableColumn<TransactionHistory, String>, TableCell<TransactionHistory, String>> create() {

        return column -> new TableCell<>() {

            private final Tooltip tooltip = new Tooltip();
            private final Text textNode = new Text();

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                    return;
                }

                setText(item);

                textNode.setText(item);
                textNode.setFont(getFont());

                double textWidth = textNode.getLayoutBounds().getWidth();
                double cellWidth = getTableColumn().getWidth() - 20;

                if (textWidth > cellWidth) {
                    tooltip.setText(item);
                    setTooltip(tooltip);
                } else {
                    setTooltip(null);
                }
            }
        };
    }
}