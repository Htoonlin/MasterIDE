package com.sdm;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.controller.MainController;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class IDE extends Application {

    private static Preferences prefs = Preferences.userNodeForPackage(IDE.class);

    public static Preferences getPrefs() {
        return prefs;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/fxml/main.css").toExternalForm());
            primaryStage.setMaximized(true);
            primaryStage.setScene(scene);
            primaryStage.setTitle("MasterIDE");
            primaryStage.show();
        } catch (Exception ex) {
            AlertDialog.showException(ex);
            Platform.exit();
        }
    }

}
