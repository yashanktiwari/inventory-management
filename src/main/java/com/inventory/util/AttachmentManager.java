package com.inventory.util;

import com.inventory.dao.TransactionDAO;
import com.inventory.database.AppConfig;
import com.inventory.model.TransactionHistory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AttachmentManager {

    public void handleAttachment(
            TransactionHistory history,
            Stage stage,
            TransactionDAO transactionDAO,
            Runnable reloadCallback
    ) {

        String storagePath = AppConfig.getAttachmentPath();

        if (storagePath == null || storagePath.isBlank()) {
            StoragePathDialog.show(stage);
            return;
        }

        try {

            if (history.getAttachmentFile() == null ||
                    history.getAttachmentFile().isBlank()) {

                uploadAttachment(history, storagePath, stage, transactionDAO, reloadCallback);

            } else {

                viewAttachment(history, storagePath);
            }

        } catch (Exception e) {

            e.printStackTrace();
            AlertUtil.showError("Error", "Unable to process attachment");
        }
    }

    private void uploadAttachment(
            TransactionHistory history,
            String storagePath,
            Stage stage,
            TransactionDAO transactionDAO,
            Runnable reloadCallback
    ) throws Exception {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Attachment");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        File file = chooser.showOpenDialog(stage);

        if (file == null) return;

        String extension =
                file.getName().substring(file.getName().lastIndexOf("."));

        String newName =
                history.getTransactionId() + "_" +
                        System.currentTimeMillis() + extension;

        File target = new File(
                storagePath + File.separator +
                        "transactions" + File.separator + newName
        );

        java.nio.file.Files.copy(file.toPath(), target.toPath());

        transactionDAO.updateAttachment(
                history.getTransactionId(),
                newName
        );

        if (reloadCallback != null) {
            reloadCallback.run();
        }
    }

    private void viewAttachment(TransactionHistory history, String storagePath) throws Exception {

        File file = new File(
                storagePath + File.separator +
                        "transactions" + File.separator +
                        history.getAttachmentFile()
        );

        if (!file.exists()) {

            AlertUtil.showError("File Missing", "Attachment not found.");
            return;
        }

        java.awt.Desktop.getDesktop().open(file);
    }
}