/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author htoonlin
 */
public class SystemSettingController implements Initializable {

    @FXML
    private VBox itemBox;

    private File settingFile;
    private Properties properties;

    public void setFile(File settingFile) {
        this.settingFile = settingFile;
        this.properties = new Properties();
        try (InputStream in = new FileInputStream(settingFile)) {
            this.properties.load(in);
            this.loadUI();
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    private void loadUI() {
        List<String> keys = new ArrayList<>(properties.stringPropertyNames());
        Collections.sort(keys);

        keys.forEach((key) -> {
            String value = this.properties.getProperty(key, "");
            String inputName = key.replaceAll("com\\.sdm", "").replaceAll("\\.", " ") + " : ";
            HBox inputBox = new HBox(10);
            inputBox.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(inputName);
            lbl.setPrefWidth(250);
            lbl.setAlignment(Pos.CENTER_RIGHT);
            inputBox.getChildren().add(lbl);

            TextField input = new TextField(value);
            input.focusedProperty().addListener((ob, oldValue, newValue) -> {
                if (!newValue) {
                    this.properties.put(key, input.getText());
                }
            });
            inputBox.getChildren().add(input);
            input.setPrefWidth(400);
            this.itemBox.getChildren().add(inputBox);
        });
    }

    private void loadProperties() {

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void saveSystemSetting(ActionEvent event) {
        try (OutputStream output = new FileOutputStream(this.settingFile)) {
            properties.store(output, "It is system generated config file.");
            AlertDialog.showInfo("Save successful.");
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

}
