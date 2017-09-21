package com.sdm.ide.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.component.ProgressDialog;
import com.sdm.ide.component.TableHelper;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.task.LoadEntityTask;
import com.sdm.ide.helper.TemplateManager;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

public class EntityManagerController implements Initializable {

    private EntityModel currentEntity;

    private Node currentDetail;

    private String moduleDir;

    @FXML
    private SplitPane rootPane;

    @FXML
    private TableView<PropertyModel> propertyTable;

    @FXML
    private Button btnMapping;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void clearDetail() {
        if (this.currentDetail != null) {
            rootPane.getItems().remove(this.currentDetail);
            this.currentDetail = null;
        }
    }

    private <T> T loadDetail(String fxml) throws IOException {
        this.clearDetail();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        this.currentDetail = loader.load();
        rootPane.getItems().add(currentDetail);

        return loader.getController();
    }

    private void showPropertyDetail(PropertyModel property) {
        /* Load Property Detail */
        try {
            PropertyDetailController controller = this.loadDetail("/fxml/PropertyDetail.fxml");
            controller.setProperty(property);
        } catch (IOException e) {
            AlertDialog.showException(e);
        }
    }

    public void loadEntity(File entity) {
        if (entity != null && entity.isFile() && entity.getName().endsWith(".java")) {
            this.moduleDir = entity.getParent().replaceAll("entity", "");
            ProgressDialog dialog = new ProgressDialog();
            LoadEntityTask task = new LoadEntityTask(entity);
            dialog.start(task);
            task.setOnSucceeded((event) -> {
                currentEntity = task.getValue();
                TableHelper.generateColumns(PropertyModel.class, propertyTable);
                propertyTable.setRowFactory(tv -> {
                    TableRow<PropertyModel> row = new TableRow<>();
                    row.setOnMouseClicked(mouse -> {
                        if (mouse.getButton() == MouseButton.PRIMARY && mouse.getClickCount() == 2 && !row.isEmpty()) {
                            PropertyModel property = row.getItem();
                            if (property != null) {
                                showPropertyDetail(property);
                            }
                        }
                    });
                    return row;
                });
                propertyTable.setItems(FXCollections.observableArrayList(currentEntity.getProperties()));
                propertyTable.refresh();
                this.showDetail(null);
                dialog.close();
            });

            new Thread(task).start();
        } else {
            AlertDialog.showWarning("It is not java file. <" + entity.getAbsolutePath() + ">.");
        }
    }

    @FXML
    public void showDetail(ActionEvent event) {
        try {
            EntityInfoController controller = this.loadDetail("/fxml/EntityInfo.fxml");
            controller.setEntity(currentEntity);
        } catch (IOException e) {
            AlertDialog.showException(e);
        }
    }

    @FXML
    public void addProperty(ActionEvent event) {
        PropertyModel property = new PropertyModel(this.currentEntity.getProperties().size());
        this.currentEntity.addProperty(property);
        propertyTable.getItems().add(property);
        propertyTable.refresh();
        propertyTable.getSelectionModel().select(property);
        this.showPropertyDetail(property);
    }

    private void deleteProperty() {
        PropertyModel property = this.propertyTable.getSelectionModel().getSelectedItem();
        if (property != null) {
            Optional<ButtonType> confirm = AlertDialog
                    .showQuestion("Are you sure to remove " + property.getName() + "?");
            if (confirm.get() == ButtonType.YES) {
                this.currentEntity.getProperties().remove(property);
                this.propertyTable.getItems().remove(property);
                this.propertyTable.refresh();
            }
        }
    }

    @FXML
    public void deleteProperty(ActionEvent event) {
        this.deleteProperty();
    }

    @FXML
    void deleteKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            this.deleteProperty();
        }
    }

    @FXML
    public void writeEntity(ActionEvent event) {
        TemplateManager manager = new TemplateManager();
        try {
            manager.writeEntity(this.currentEntity, this.moduleDir);

            // Generate DAO or not?
            String daoName = this.currentEntity.getName().replaceAll("Entity", "DAO.java");
            Optional<ButtonType> result = AlertDialog.showQuestion(
                    "Do you want to generate DAO? It will override if " + daoName + " file already existed.");
            if (result.get() == ButtonType.YES) {
                manager.writeDAO(this.currentEntity, this.moduleDir);

                // Generate Resource or not?
                String resourceName = this.currentEntity.getName().replaceAll("Entity", "Resource.java");
                result = AlertDialog.showQuestion("Do you want to generate Resource? It will override if "
                        + resourceName + " file already existed.");
                if (result.get() == ButtonType.YES) {
                    manager.writeResource(this.currentEntity, this.moduleDir);
                }
            }

            // Write mapping in hibernate config file.
            String entityClass = this.currentEntity.getModuleName() + ".entity." + this.currentEntity.getName();
            if (this.currentEntity.isMappedWithDB()) {
                HibernateManager.getInstance().addEntity(entityClass);
            } else {
                HibernateManager.getInstance().removeMapping(entityClass);
            }
            HibernateManager.getInstance().writeConfig();

            AlertDialog.showInfo("Save successful.");
        } catch (Exception e) {
            AlertDialog.showException(e);
        }

    }
}
