/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.model.DatabaseModel;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author htoonlin
 */
public class HibernateSettingController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField txtHost;
    @FXML
    private TextField txtSchema;
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPassword;

    private DatabaseModel dbModel;

    @FXML
    private ListView<String> lstEntities;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.dbModel = HibernateManager.getInstance().getDBInfo();

            txtHost.textProperty().bindBidirectional(this.dbModel.hostProperty());
            txtSchema.textProperty().bindBidirectional(this.dbModel.schemaProperty());
            txtUser.textProperty().bindBidirectional(this.dbModel.userProperty());
            txtPassword.textProperty().bindBidirectional(this.dbModel.passwordProperty());

            ObservableList<String> entities = FXCollections.observableArrayList(HibernateManager.getInstance().getEntities());
            lstEntities.setItems(entities);

        } catch (URISyntaxException ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    private void saveDatabaseSetting(ActionEvent event) {
        try {
            HibernateManager.getInstance().setDBInfo(this.dbModel);
            HibernateManager.getInstance().writeConfig();
            HibernateManager.getInstance().reload();
            AlertDialog.showInfo("Save successful.");
        } catch (Exception e) {
            AlertDialog.showException(e);
        }
    }

    @FXML
    private void removeSelectedEntity(KeyEvent event) {
        String name = lstEntities.getSelectionModel().getSelectedItem();

        if (!name.isEmpty()
                && (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE)) {
            Optional<ButtonType> result = AlertDialog.showQuestion("Are you sure to remove " + name + "?");
            if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                HibernateManager.getInstance().removeMapping(name);
            }
        }
    }

}
