package com.sdm.ide.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.json.JSONObject;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.helper.TypeManager;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;
import com.sdm.ide.model.ValidateModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;

public class PropertyDetailController implements Initializable {

    private EntityModel currentEntity;

    private PropertyModel currentProperty;

    public void setProperty(EntityModel entity, PropertyModel property) {
        if (property == null) {
            AlertDialog.showWarning("Invalid property.");
            return;
        }

        if (entity == null) {
            AlertDialog.showError("There is no entity for " + property.getName());
            return;
        }

        this.currentEntity = entity;

        this.currentProperty = property;

        this.lblPropertyName.setText(property.getName());

        try {
            this.txtPropertyName.textProperty().bindBidirectional(property.nameProperty());
            this.cboPropertyType.valueProperty().bindBidirectional(property.typeProperty());
            this.chkPropertyAuditable.selectedProperty().bindBidirectional(property.auditableProperty());
            this.chkPropertySearchable.selectedProperty().bindBidirectional(property.searchableProperty());
            this.chkPropertyJsonIgnore.selectedProperty().bindBidirectional(property.jsonIgnoreProperty());

            this.txtColumnName.textProperty().bindBidirectional(property.columnNameProperty());
            this.txtColumnDef.textProperty().bindBidirectional(property.columnDefProperty());
            this.chkColumnPrimary.selectedProperty().bindBidirectional(property.primaryProperty());
            this.chkColumnRequired.selectedProperty().bindBidirectional(property.requiredProperty());

            this.txtUILabel.textProperty().bindBidirectional(property.labelProperty());
            this.cboUIInputType.valueProperty().bindBidirectional(property.inputTypeProperty());
            this.txtUIIndex.textProperty().bindBidirectional(property.indexProperty(), new NumberStringConverter());
            this.chkUIHideInGrid.selectedProperty().bindBidirectional(property.hideInGridProperty());
            this.chkReadOnly.selectedProperty().bindBidirectional(property.readOnlyProperty());

            if (property.getValidations() != null) {
                ObservableList<ValidateModel> validations = FXCollections
                        .observableArrayList(property.getValidations());
                this.lstValidations.setItems(validations);
            }

        } catch (Exception ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    private AnchorPane rootPane;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private TextField txtPropertyName;

    @FXML
    private ComboBox<String> cboPropertyType;

    @FXML
    private CheckBox chkPropertyAuditable;

    @FXML
    private CheckBox chkPropertySearchable;

    @FXML
    private CheckBox chkPropertyJsonIgnore;

    @FXML
    private TextField txtColumnName;

    @FXML
    private TextField txtColumnDef;

    @FXML
    private CheckBox chkColumnPrimary;

    @FXML
    private CheckBox chkColumnRequired;

    @FXML
    private TextField txtUILabel;

    @FXML
    private ComboBox<String> cboUIInputType;

    @FXML
    private TextField txtUIIndex;

    @FXML
    private CheckBox chkUIHideInGrid;

    @FXML
    private CheckBox chkReadOnly;

    @FXML
    private Label lblPropertyName;

    @FXML
    private ListView<ValidateModel> lstValidations;

    private void showValidation(ValidateModel model, final boolean isUpdate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ValidationDetail.fxml"));
            AnchorPane root = (AnchorPane) loader.load();

            Scene dialogScene = new Scene(root);
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(dialogScene);
            dialogStage.show();

            ValidationDetailController controller = loader.getController();
            controller.setModel(model);
            controller.onDone(result -> {
                if (!isUpdate) {
                    this.currentProperty.addValidation(result);
                    lstValidations.getItems().add(result);
                    lstValidations.refresh();
                }
            });

        } catch (IOException e) {
            AlertDialog.showException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainScrollPane.setFitToWidth(true);
        this.txtPropertyName.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                txtColumnName.setText(newValue);
                txtUILabel.setText(newValue);
            }
        });

        try {
            cboUIInputType.setItems(TypeManager.getInstance().getInputTypes());
            cboPropertyType.setItems(TypeManager.getInstance().getJavaTypes());
        } catch (IOException e) {
            AlertDialog.showException(e);
        }

        this.txtColumnName.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if (!newValue) {
                    if (!ProjectManager.validColumnName(txtColumnName.getText())) {
                        AlertDialog.showWarning("Invalid column name <" + txtColumnName.getText() + ">.");
                        txtColumnName.setText("");
                    }
                }
            }
        });

        this.txtPropertyName.focusedProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue) {
                if (!newValue) {
                    if (!ProjectManager.validJavaProperty(txtPropertyName.getText())) {
                        AlertDialog.showWarning("Invalid property name <" + txtPropertyName.getText() + ">.");
                        txtPropertyName.setText("");
                    }
                }
            }
        });
    }

    @FXML
    void deleteValidationRule(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE || event.getCode() == KeyCode.BACK_SPACE) {
            final ValidateModel model = lstValidations.getSelectionModel().getSelectedItem();
            if (model != null) {
                Optional<ButtonType> result = AlertDialog
                        .showQuestion("Are you sure to remove " + model.getName() + "?");
                result.ifPresent(buttonType -> {
                    if (buttonType.equals(ButtonType.YES)) {
                        this.currentProperty.getValidations().remove(model);
                        lstValidations.getItems().remove(model);
                        lstValidations.refresh();
                    }
                });
            }
        }
    }

    @FXML
    void selectedItem(MouseEvent event) {
        ValidateModel model = lstValidations.getSelectionModel().getSelectedItem();
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && model != null) {
            this.showValidation(model, true);
        }
    }

    @FXML
    void changePropertyType(ActionEvent event) {
        String javaType = cboPropertyType.getValue();
        try {
            JSONObject linkTypes = TypeManager.getInstance().getLinkType(javaType);
            txtColumnDef.setText(linkTypes.getString("db"));
            cboUIInputType.setValue(linkTypes.getString("input"));
        } catch (IOException e) {
            AlertDialog.showException(e);
        }
    }

    @FXML
    public void addRule(ActionEvent event) {
        this.showValidation(null, false);
    }

    public void showDetailInputType(ActionEvent event) {
        AlertDialog.showWarning("Under constructions!");
    }

    @FXML
    private void changePrimary(ActionEvent event) {
        if (!chkColumnPrimary.isSelected()) {
            this.currentEntity.setPrimaryProperty(null);
            return;
        }

        Optional<ButtonType> result = AlertDialog.showQuestion("It will remove other primary field. Do you want to continue?");
        if (result.isPresent() && result.get().equals(ButtonType.YES)) {
            PropertyModel previousModel = this.currentEntity.getPrimaryProperty();
            if (previousModel != null) {
                previousModel.setPrimary(false);
            }
            this.currentEntity.setPrimaryProperty(this.currentProperty);
        }
    }
}
