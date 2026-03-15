package com.inventory.ui.table;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

public class TableCellTooltipUtil {

    public static void applyTooltip(TableCell<?, ?> cell, String text) {

        if (text == null || text.isBlank()) {
            cell.setTooltip(null);
            return;
        }

        Text helper = new Text(text);
        helper.setFont(cell.getFont());

        double textWidth = helper.getLayoutBounds().getWidth();
        double cellWidth = cell.getWidth() - 10;

        if (textWidth > cellWidth) {

            Tooltip tooltip = cell.getTooltip();

            if (tooltip == null) {
                tooltip = new Tooltip();
                tooltip.setShowDelay(javafx.util.Duration.millis(80));   // faster
                tooltip.setHideDelay(javafx.util.Duration.millis(100));
                cell.setTooltip(tooltip);
            }

            tooltip.setText(text);

        } else {
            cell.setTooltip(null);
        }
    }
}