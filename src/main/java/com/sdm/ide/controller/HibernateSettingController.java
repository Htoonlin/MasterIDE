/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.model.DatabaseModel;
import com.sdm.ide.model.ProjectTreeModel;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

    @FXML
    private ListView<String> lstEntities;

    private DatabaseModel dbModel;

    private TreeView<ProjectTreeModel> projectTree;

    public void setProjectTree(TreeView<ProjectTreeModel> projectTree) {
        this.projectTree = projectTree;
    }

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
                lstEntities.getItems().remove(name);
            }
        }
    }

    @FXML
    private void autoMapped(ActionEvent event) {
        if (this.projectTree != null) {
            HibernateManager.getInstance().clearMappings();
            this.mapAllEntities(this.projectTree.getRoot());
            ObservableList<String> entities = FXCollections.observableArrayList(HibernateManager.getInstance().getEntities());
            lstEntities.setItems(entities);
            AlertDialog.showInfo("Successfully loaded entities.");
        }
    }

    private void mapAllEntities(TreeItem<ProjectTreeModel> item) {
        ProjectTreeModel model = item.getValue();
        if (model.getType() == ProjectTreeModel.Type.ENTITY) {
            String javaClass = ProjectManager.getClassNameWithPackage(model.getFile());
            HibernateManager.getInstance().addEntity(javaClass);
        }

        item.getChildren().forEach(subItem -> {
            mapAllEntities(subItem);
        });
    }

}
