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
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author htoonlin
 */
public class Log4JSettingController implements Initializable {

    @FXML
    private TextField txtLogFile;

    private static final String FILE_PATH = "log4j.appender.fileAppender.File";
    private File log4jFile;
    private Properties properties;

    public void setFile(File log4jFile) {
        this.log4jFile = log4jFile;
        properties = new Properties();
        try (InputStream in = new FileInputStream(log4jFile)) {
            properties.load(in);
            txtLogFile.setText(properties.getProperty(FILE_PATH));
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void saveLog4JSetting(ActionEvent event) {
        try (OutputStream output = new FileOutputStream(log4jFile)) {
            properties.put(FILE_PATH, txtLogFile.getText());
            properties.store(output, "It is system generated config file.");
            AlertDialog.showInfo("Save successful.");
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

}
