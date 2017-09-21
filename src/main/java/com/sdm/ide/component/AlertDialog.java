package com.sdm.ide.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class AlertDialog {

    private static final String TITLE = "Master IDE";

    public static Optional<ButtonType> showException(Throwable e) {
        String content = e.getClass().getName();
        if (e.getMessage() != null && e.getMessage().length() > 0) {
            content = e.getLocalizedMessage();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        Label label = new Label("Exception stacktrace is: ");
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        Alert alert = createAlert(AlertType.ERROR, content);
        alert.getDialogPane().setExpandableContent(expContent);
        return alert.showAndWait();
    }

    public static Alert createAlert(AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(TITLE);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert;
    }

    public static Optional<String> showInput(String description, Image image) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setGraphic(new ImageView(image));
        dialog.setTitle(TITLE);
        dialog.setHeaderText(description);
        dialog.setContentText(null);

        return dialog.showAndWait();
    }

    public static Optional<ButtonType> showInfo(String content) {
        Alert alert = createAlert(AlertType.INFORMATION, content);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showWarning(String content) {
        Alert alert = createAlert(AlertType.WARNING, content);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showError(String content) {
        Alert alert = createAlert(AlertType.ERROR, content);
        return alert.showAndWait();
    }

    public static Optional<ButtonType> showQuestion(String content) {
        Alert alert = createAlert(AlertType.CONFIRMATION, content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait();
    }
}
