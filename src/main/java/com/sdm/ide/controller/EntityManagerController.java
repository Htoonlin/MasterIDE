package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.component.ProgressDialog;
import com.sdm.ide.component.TableHelper;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;
import com.sdm.ide.task.ParseEntityTask;
import com.sdm.ide.task.WriteEntityTask;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class EntityManagerController implements Initializable {

    private EntityModel currentEntity;

    private Node currentDetail;

    private String moduleDir;

    @FXML
    private SplitPane rootPane;

    @FXML
    private TableView<PropertyModel> propertyTable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        propertyTable.setRowFactory(tv -> {
            TableRow<PropertyModel> row = new TableRow<>();
            row.setOnMouseClicked(mouse -> {
                if (mouse.getButton() == MouseButton.PRIMARY && mouse.getClickCount() == 2 && !row.isEmpty()) {
                    PropertyModel property = row.getItem();
                    if (property != null) {
                        this.loadPropertyDetail(property);
                    }
                }
            });
            return row;
        });
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

    private void loadPropertyDetail(PropertyModel property) {
        /* Load Property Detail */
        try {
            PropertyDetailController controller = this.loadDetail("/fxml/PropertyDetail.fxml");
            controller.setProperty(this.currentEntity, property);
        } catch (IOException e) {
            AlertDialog.showException(e);
        }
    }

    public void loadEntity(File entityFile) {
        if (entityFile != null && entityFile.isFile() && entityFile.getName().endsWith(".java")) {
            this.moduleDir = entityFile.getParent().replaceAll("entity", "");

            try {
                ParseEntityTask task = new ParseEntityTask(entityFile);
                ProgressDialog<EntityModel> dialog = new ProgressDialog(task, false);
                dialog.onSucceedHandler(value -> {
                    currentEntity = value;
                    TableHelper.generateColumns(PropertyModel.class, propertyTable);
                    propertyTable.setItems(FXCollections.observableArrayList(currentEntity.getProperties()));
                    propertyTable.getColumns().forEach(col -> {
                        if (col.getText().equalsIgnoreCase("index")) {
                            propertyTable.getSortOrder().add(col);
                        }
                    });
                    propertyTable.refresh();
                    this.showDetail(null);                    
                });
                dialog.start();
            } catch (Exception ex) {
                AlertDialog.showException(ex);
            }
        } else {
            AlertDialog.showWarning("It is not java file. <" + entityFile.getName() + ">.");
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
        this.loadPropertyDetail(property);
    }

    @FXML
    private void deleteProperty(ActionEvent event) {
        PropertyModel property = this.propertyTable.getSelectionModel().getSelectedItem();
        if (property != null) {
            Optional<ButtonType> confirm = AlertDialog
                    .showQuestion("Are you sure to remove " + property.getName() + "?");
            if (confirm.get() == ButtonType.YES) {
                if (property.isPrimary()) {
                    this.currentEntity.setPrimaryProperty(null);
                }
                this.currentEntity.removeProperty(property);
                this.propertyTable.getItems().remove(property);
                this.propertyTable.refresh();
            }
        }
    }

    @FXML
    public void writeEntity(ActionEvent event) {
        try {
            if (this.currentEntity.getPrimaryProperty() == null) {
                AlertDialog.showWarning("Sorry! We can't generate entity without primary property.");
                return;
            }

            WriteEntityTask task = new WriteEntityTask(currentEntity);
            ProgressDialog<Boolean> dialog = new ProgressDialog<>(task, false);            
            dialog.onSucceedHandler(value -> {                
                if (value) {
                    this.loadEntity(this.currentEntity.getFile());
                } else {
                    AlertDialog.showWarning("Something wrong in code generation process.");
                }
            });
            dialog.start();
        } catch (Exception ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    private void showCode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CodeEditor.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            CodeEditorController controller = loader.getController();
            controller.setEntity(currentEntity);

            Scene dialogScene = new Scene(root, 720, 500);
            dialogScene.getStylesheets().add(getClass().getResource("/fxml/syntax.css").toExternalForm());
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Code Editor");
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/image/code.png")));
            dialogStage.initStyle(StageStyle.DECORATED);
            dialogStage.setResizable(true);
            dialogStage.setScene(dialogScene);
            dialogStage.show();
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    private void reloadEntity(ActionEvent event) {
        Optional<ButtonType> result = AlertDialog.showQuestion("It will lost unsaved data. Do you want to continue?");
        if (result.isPresent() && result.get().equals(ButtonType.YES)) {
            this.loadEntity(this.currentEntity.getFile());
        }
    }

    @FXML
    private void showPropertyDetail(ActionEvent event) {
        PropertyModel property = this.propertyTable.getSelectionModel().getSelectedItem();
        if (property != null) {
            this.loadPropertyDetail(property);
        }
    }

    @FXML
    private void cloneProperty(ActionEvent event) {
        PropertyModel propSource = this.propertyTable.getSelectionModel().getSelectedItem();
        if (propSource != null) {
            PropertyModel propDest = new PropertyModel(propSource);
            this.currentEntity.addProperty(propDest);
            propertyTable.getItems().add(propDest);
            propertyTable.refresh();
            propertyTable.getSelectionModel().select(propDest);
            this.loadPropertyDetail(propDest);
        }
    }
}
