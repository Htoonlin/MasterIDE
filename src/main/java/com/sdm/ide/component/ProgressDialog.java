package com.sdm.ide.component;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class ProgressDialog {

    private final ProgressBar mainProgressBar;

    private final Label progressLabel;

    private final Stage dialogStage;

    public ProgressDialog() {
        super();
        mainProgressBar = new ProgressBar();
        mainProgressBar.setPrefWidth(250);
        progressLabel = new Label("Please wait...");
        progressLabel.setTextAlignment(TextAlignment.CENTER);

        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(20));
        rootPane.setFillWidth(true);
        rootPane.setAlignment(Pos.TOP_CENTER);
        rootPane.setSpacing(20);
        rootPane.getChildren().add(mainProgressBar);
        rootPane.getChildren().add(progressLabel);

        Scene dialogScene = new Scene(rootPane, 320, 100, Color.WHITE);

        this.dialogStage = new Stage();
        this.dialogStage.initStyle(StageStyle.UTILITY);
        this.dialogStage.setResizable(false);
        this.dialogStage.initModality(Modality.APPLICATION_MODAL);
        this.dialogStage.setScene(dialogScene);
    }

    public void start(final Task<?> task) {
        task.setOnCancelled((event) -> close());
        task.setOnFailed((event) -> {
            close();
            if (task.getException() != null) {
                AlertDialog.showException(task.getException());
            } else {
                AlertDialog.showError("Task failed!");
            }
        });
        this.mainProgressBar.progressProperty().bind(task.progressProperty());
        this.progressLabel.textProperty().bind(task.messageProperty());
        this.dialogStage.show();
    }

    public ProgressBar getProgressBar() {
        return mainProgressBar;
    }

    public Label getLabel() {
        return progressLabel;
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setTitle(String title) {
        this.dialogStage.setTitle(title);
    }

    public void close() {
        this.dialogStage.close();
    }
}
