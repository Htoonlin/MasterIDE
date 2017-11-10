package com.sdm.ide.component;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgressDialog<T> extends Stage {

    private final ProgressBar mainProgressBar;

    private Callback<T> succeedHandler;
    
    private final Task<T> task;

    public void onSucceedHandler(Callback<T> handler) {
        this.succeedHandler = handler;
    }

    public ProgressDialog(Task<T> task, final boolean showLog) {
        super(StageStyle.UTILITY);
        
        this.task = task;
        
        mainProgressBar = new ProgressBar();
        mainProgressBar.setPrefWidth(showLog ? 400 : 250);
        this.mainProgressBar.progressProperty().bind(task.progressProperty());

        VBox rootPane = new VBox();
        rootPane.setPadding(new Insets(10));
        rootPane.setFillWidth(true);
        rootPane.setAlignment(Pos.TOP_CENTER);
        rootPane.setSpacing(10);
        rootPane.getChildren().addAll(mainProgressBar);

        Scene dialogScene = new Scene(rootPane);

        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        setScene(dialogScene);

        task.setOnCancelled(event -> close());

        task.setOnFailed((event) -> {
            close();
            if (task.getException() != null) {
                AlertDialog.showException(task.getException());
            } else {
                AlertDialog.showError("Task failed!");
            }
        });

        if (showLog) {
            TextArea logViewer = new TextArea();
            logViewer.setPrefSize(400, 250);
            logViewer.setEditable(false);
            task.messageProperty().addListener(listener -> {
                String message = task.getMessage();
                if (!message.endsWith("\n")) {
                    message += "\n";
                }
                logViewer.appendText(message);
            });
            rootPane.getChildren().add(logViewer);

            Button btnClose = new Button("Close");
            btnClose.setPrefWidth(400);
            btnClose.setDisable(true);
            btnClose.setOnAction(event -> close());
            rootPane.getChildren().add(btnClose);
            task.setOnSucceeded(event -> { 
                mainProgressBar.setVisible(false);
                btnClose.setDisable(false);
                if (succeedHandler != null) {
                    succeedHandler.call(task.getValue());
                }
            });
        } else {
            Label lblMessage = new Label();
            lblMessage.setPrefWidth(250);
            lblMessage.textProperty().bind(task.messageProperty());
            rootPane.getChildren().add(lblMessage);
            task.setOnSucceeded(event -> {
                close();
                if (succeedHandler != null) {
                    succeedHandler.call(task.getValue());
                }
            });
        }
    }

    public ProgressBar getProgressBar() {
        return mainProgressBar;
    }
    
    public void start() {
        show();
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
