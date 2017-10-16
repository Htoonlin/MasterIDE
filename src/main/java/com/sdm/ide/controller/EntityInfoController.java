package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.model.EntityModel;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EntityInfoController implements Initializable {

    private EntityModel currentEntity;

    @FXML
    private TextField txtResourcePath;

    @FXML
    private TextField txtModule;

    @FXML
    private TextField txtEntity;

    @FXML
    private TextField txtTable;

    @FXML
    private CheckBox chkMappedWithDatabase;

    @FXML
    private CheckBox chkAuditable;

    @FXML
    private CheckBox chkDynamicUpdate;

    @FXML
    private Label lblEntity;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private TextArea txtDescription;

    public void setEntity(EntityModel entity) {
        if (entity != null) {

            this.currentEntity = entity;

            String title = entity.getModuleName() + ".entity." + entity.getName();
            lblEntity.setText(title);

            txtResourcePath.textProperty().bindBidirectional(entity.resourcePathProperty());
            txtModule.textProperty().bindBidirectional(entity.moduleNameProperty());
            txtEntity.textProperty().bindBidirectional(entity.entityNameProperty());
            txtTable.textProperty().bindBidirectional(entity.tableNameProperty());
            chkMappedWithDatabase.selectedProperty().bindBidirectional(entity.mappedWithDBProperty());
            chkAuditable.selectedProperty().bindBidirectional(entity.auditableProperty());
            chkDynamicUpdate.selectedProperty().bindBidirectional(entity.dynamicUpdateProperty());
            txtDescription.textProperty().bindBidirectional(entity.descriptionProperty());

            Set<String> entityClasses = HibernateManager.getInstance().getEntities();
            boolean isMapped = entityClasses.contains(title);
            chkMappedWithDatabase.setSelected(isMapped);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainScrollPane.setFitToWidth(true);

        // Validate entity name
        this.txtEntity.focusedProperty().addListener((field, oldValue, newValue) -> {
            if (!newValue) {
                txtEntity.setText(txtEntity.getText().replaceAll(" ", "_"));
            }
        });

        // Validate module name
        this.txtModule.focusedProperty().addListener((field, oldValue, newValue) -> {
            if (!newValue) {
                if (!ProjectManager.validJavaPackage(txtModule.getText())) {
                    AlertDialog.showWarning("Invalid module name <" + txtModule.getText() + ">.");
                    txtModule.setText("");
                    txtModule.requestFocus();
                }
            }
        });

        // Validate table name
        this.txtTable.focusedProperty().addListener((field, oldValue, newValue) -> {
            if (!newValue) {
                if (!ProjectManager.validTableName(txtTable.getText())) {
                    AlertDialog.showWarning("Invalid table name <" + txtTable.getText() + ">.");
                    txtTable.setText("");
                    txtTable.requestFocus();
                }
            }
        });
    }
}
