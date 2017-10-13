/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.github.javaparser.ast.expr.AnnotationExpr;
import com.sdm.ide.component.Callback;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.model.PropertyModel;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Htoonlin
 */
public class EntityRelationController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private VBox itemBox;
    @FXML
    private ComboBox<String> cboRelations;
    @FXML
    private ComboBox<String> cboSource;

    private PropertyModel property;

    private Callback<PropertyModel> doneHandler;

    private Callback<PropertyModel> cancelHandler;

    public void onDone(Callback<PropertyModel> handler) {
        this.doneHandler = handler;
    }

    public void onCancel(Callback<PropertyModel> handler) {
        this.cancelHandler = handler;
    }

    public void setProperty(PropertyModel property) throws Exception {
        if (property == null) {
            throw new Exception("Invalid property.");
        }

        //Check Relations
        this.cboRelations.valueProperty().bindBidirectional(property.relationTypeProperty());
        this.cboSource.valueProperty().bindBidirectional(property.relationSourceProperty());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cboRelations.setItems(FXCollections.observableArrayList(PropertyModel.RELATIONS));
        Set<String> entities = HibernateManager.getInstance().getEntities();
        cboSource.setItems(FXCollections.observableArrayList(entities));
    }

    @FXML
    private void changeRelation(ActionEvent event) {
        cboSource.setDisable(cboRelations.getValue().equalsIgnoreCase("None"));
    }

    @FXML
    private void cancelClick(ActionEvent event) {
    }

    @FXML
    private void doneClick(ActionEvent event) {
    }

}
