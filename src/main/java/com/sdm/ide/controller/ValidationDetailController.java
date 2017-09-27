package com.sdm.ide.controller;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.component.Callback;
import com.sdm.ide.helper.ValidationManager;
import com.sdm.ide.model.AnnotationModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ValidationDetailController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private VBox itemBox;

    @FXML
    private ComboBox<String> cboRules;

    private AnnotationModel model;

    public void setModel(AnnotationModel model) {
        if (model == null) {
            return;
        }

        this.model = model;
        this.cboRules.getSelectionModel().select(model.getName());
    }

    private Callback<AnnotationModel> doneHandler;

    private Callback<AnnotationModel> cancelHandler;

    public void onDone(Callback<AnnotationModel> handler) {
        this.doneHandler = handler;
    }

    public void onCancel(Callback<AnnotationModel> handler) {
        this.cancelHandler = handler;
    }

    private void close() {
        Stage stage = (Stage) this.rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelClick(ActionEvent event) {
        if (this.cancelHandler != null) {
            this.cancelHandler.call(model);
        }

        this.close();
    }

    @FXML
    void doneClick(ActionEvent event) {
        if (this.doneHandler != null) {
            if (this.model == null) {
                this.model = new AnnotationModel();
            }

            this.model.setName(cboRules.getSelectionModel().getSelectedItem());
            this.setInputValues();
            this.doneHandler.call(this.model);
        }
        this.close();
    }

    private void setInputValues() {
        for (Node node : this.itemBox.getChildren()) {
            if (node instanceof TextField) {
                TextField input = (TextField) node;
                if (input.getText().trim().length() > 0) {
                    this.model.addValue(input.getId(), input.getText());
                }
            }
        }
    }

    private void generatePatternParam(final TextField txtRegexPattern) {
        try {
            final Map<String, String> patterns = ValidationManager.getInstance().getPatterns();
            ObservableList<String> patternNames = FXCollections.observableArrayList(patterns.keySet());
            Collections.sort(patternNames);
            patternNames.add("Custom");

            String currentPattern = "Custom";
            //Select pattern type by regex
            if (txtRegexPattern.getText().length() > 0) {
                for (Map.Entry<String, String> entry : patterns.entrySet()) {
                    if (entry.getValue().equals(txtRegexPattern.getText())) {
                        currentPattern = entry.getKey();
                        break;
                    }
                }
            }

            //Create UI
            ComboBox<String> cboPatterns = new ComboBox<>(patternNames);
            cboPatterns.setPrefWidth(280);
            cboPatterns.setPromptText("Choose pattern");
            cboPatterns.setValue(currentPattern);
            cboPatterns.valueProperty().addListener((ob, oldValue, newValue) -> {
                if (newValue.equalsIgnoreCase("Custom")) {
                    txtRegexPattern.setText("");
                } else {
                    txtRegexPattern.setText(patterns.get(newValue));
                }
            });

            itemBox.getChildren().add(cboPatterns);
            itemBox.getChildren().add(txtRegexPattern);

        } catch (Exception e) {
            AlertDialog.showException(e);
        }
    }

    private String getValueByParam(String param) {
        if (this.model == null) {
            return "";
        }

        return this.model.getValues().getOrDefault(param, "");
    }

    private void generateParams(String rule) {
        int count = itemBox.getChildren().size();
        if (count > 1) {
            itemBox.getChildren().remove(1, count);
        }

        try {
            JSONObject ruleObject = ValidationManager.getInstance().getValidation(rule);
            JSONArray params = ruleObject.getJSONArray("params");
            for (int i = 0; i < params.length(); i++) {
                String param = params.getString(i);
                TextField txtParam = new TextField();
                txtParam.setPromptText("Enter " + param + ".");
                txtParam.setId(param);
                txtParam.setText(this.getValueByParam(param));
                if (param.equalsIgnoreCase("regexp")) {
                    this.generatePatternParam(txtParam);
                } else {
                    itemBox.getChildren().add(txtParam);
                }
            }
        } catch (Exception ex) {
            AlertDialog.showException(ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            ObservableList<String> rules = FXCollections
                    .observableArrayList(ValidationManager.getInstance().getValidations().keySet());
            Collections.sort(rules);
            cboRules.setItems(rules);
            cboRules.valueProperty().addListener((ob, oldValue, newValue) -> {
                generateParams(newValue);
                rootPane.getScene().getWindow().sizeToScene();
            });
        } catch (Exception ex) {
            AlertDialog.showException(ex);
        }
    }
}
