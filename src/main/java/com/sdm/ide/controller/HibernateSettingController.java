/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.helper.TemplateManager;
import com.sdm.ide.model.DatabaseModel;
import com.sdm.ide.model.ModuleModel;
import com.sdm.ide.model.ProjectTreeModel;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
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
    private CheckBox chkAutoMapping;

    private DatabaseModel dbModel;

    private TreeItem<ProjectTreeModel> rootTree;

    public void setRootTree(TreeItem<ProjectTreeModel> root) {
        this.rootTree = root;
    }

    private List<ModuleModel> getEntites() {
        List<ModuleModel> modules = new ArrayList<>();

        if (chkAutoMapping.isSelected()) {
            for (TreeItem<ProjectTreeModel> moduleTree : this.rootTree.getChildren()) {
                ProjectTreeModel treeModel = moduleTree.getValue();
                if (treeModel.getType().equals(ProjectTreeModel.Type.MODULE)) {
                    ModuleModel module = new ModuleModel();
                    module.setName(treeModel.getLabel());
                    for (TreeItem<ProjectTreeModel> entityTree : moduleTree.getChildren()) {
                        ProjectTreeModel entityModel = entityTree.getValue();
                        if (entityModel.getType().equals(ProjectTreeModel.Type.ENTITY)) {
                            String filePath = ProjectManager.getFilePath(entityModel.getFile());
                            String entityClass = ProjectManager.getEntityClass(filePath);
                            module.addEntityClass(entityClass);
                        }
                    }
                    modules.add(module);
                }
            }
        } else {
            ModuleModel model = new ModuleModel();
            model.setName("User Defined");
            model.setEntityClasses(HibernateManager.getInstance().getEntities());
        }

        return modules;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.dbModel = new DatabaseModel();
            HibernateManager.getInstance().loadDB(dbModel);

            txtHost.textProperty().bindBidirectional(this.dbModel.hostProperty());
            txtSchema.textProperty().bindBidirectional(this.dbModel.schemaProperty());
            txtUser.textProperty().bindBidirectional(this.dbModel.userProperty());
            txtPassword.textProperty().bindBidirectional(this.dbModel.passwordProperty());
        } catch (URISyntaxException ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    private void changeAutoMapping(ActionEvent event) {
        if (chkAutoMapping.isSelected()) {
            AlertDialog.showWarning("It will map all entites from project.");
        }
    }

    @FXML
    private void saveDatabaseSetting(ActionEvent event) {
        try {
            File hibernateFile = HibernateManager.getInstance().getFile();
            TemplateManager manager = new TemplateManager();
            manager.writeHibernateConfig(this.dbModel, this.getEntites(), hibernateFile);
            HibernateManager.getInstance().load(hibernateFile);
            AlertDialog.showInfo("Save successful.");
        } catch (Exception e) {
            AlertDialog.showException(e);
        }
    }

}
