package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.model.EntityModel;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    @FXML
    private ListView<String> lstQueries;

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

            //Bind Queries
            Set<String> namedQueries = entity.getNamedQueries().keySet();
            lstQueries.setItems(FXCollections.observableArrayList(namedQueries));

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

    @FXML
    private void addQuery(ActionEvent event) {
    }

    @FXML
    private void deleteQuery(KeyEvent event) {
    }

    @FXML
    private void selectedQuery(MouseEvent event) {
        String name = lstQueries.getSelectionModel().getSelectedItem();
        if (event.getButton() != MouseButton.PRIMARY && event.getClickCount() < 2
                && name.isEmpty()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QueryEditor.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            QueryEditorController controller = loader.getController();
            controller.setQuery(name, this.currentEntity.getQueryByName(name));
            controller.onDone(pair -> {
                this.currentEntity.addNamedQuery(pair.getKey().toString(), pair.getValue().toString());
            });

            Scene dialogScene = new Scene(root, 720, 500);
            dialogScene.getStylesheets().add(getClass().getResource("/fxml/syntax.css").toExternalForm());
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Query Editor");
            dialogStage.initStyle(StageStyle.DECORATED);
            dialogStage.setResizable(true);
            dialogStage.setScene(dialogScene);
            dialogStage.show();
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }
}
