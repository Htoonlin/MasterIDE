/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sun.text.normalizer.Utility;

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
    private ComboBox<PropertyModel.Relation> cboRelations;
    @FXML
    private ComboBox<String> cboSource;

    private TextField txtTable;

    private TextField txtSource;

    private TextField txtDest;

    private PropertyModel property;

    private Callback<PropertyModel> doneHandler;

    private Callback<PropertyModel> cancelHandler;

    public void onDone(Callback<PropertyModel> handler) {
        this.doneHandler = handler;
    }

    public void onCancel(Callback<PropertyModel> handler) {
        this.cancelHandler = handler;
    }

    private void close() {
        Stage stage = (Stage) this.rootPane.getScene().getWindow();
        stage.close();
    }

    public void setProperty(PropertyModel property) throws Exception {
        if (property == null) {
            throw new Exception("Invalid property.");
        }

        this.property = property;

        PropertyModel.Relation relation = PropertyModel.Relation.None;

        //Check Relations
        if (property.getRelationAnnotation() != null) {
            relation = PropertyModel.Relation.valueOf(property.getRelationAnnotation().getNameAsString());
            this.cboRelations.setValue(relation);
            this.realtionAnalysis(relation);
        } else {
            this.cboRelations.setValue(PropertyModel.Relation.None);
        }
        this.cboSource.valueProperty().bindBidirectional(property.relationSourceProperty());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cboRelations.setItems(FXCollections.observableArrayList(PropertyModel.Relation.values()));
        Set<String> entities = HibernateManager.getInstance().getEntities();
        cboSource.setItems(FXCollections.observableArrayList(entities));
    }

    private void typeAnalysis() {
        Name source = JavaParser.parseName(cboSource.getValue());
        switch (cboRelations.getValue()) {
            case ManyToMany:
            case ManyToOne:
                property.setType("Set<" + source.getIdentifier() + ">");
                property.setInputType("objectlist");
                property.setColumnDef("");
                break;
            case OneToOne:
            case OneToMany:
                property.setType(source.getIdentifier());
                property.setInputType("object");
                property.setColumnDef("");
                break;
        }
    }

    private void realtionAnalysis(PropertyModel.Relation value) {
        int count = itemBox.getChildren().size();
        if (count > 2) {
            itemBox.getChildren().remove(2, count);
        }
        if (value.equals(PropertyModel.Relation.ManyToMany)) {
            txtTable = new TextField();
            txtTable.setPromptText("Enter join table name.");
            itemBox.getChildren().add(txtTable);

            txtSource = new TextField();
            txtSource.setPromptText("Enter column of entity.");
            itemBox.getChildren().add(txtSource);

            txtDest = new TextField();
            txtDest.setPromptText("Enter column of source.");
            itemBox.getChildren().add(txtDest);

            if (property.getJoinAnnotaion() != null) {
                this.setManyToManyAnnotation(property.getJoinAnnotaion());
            }
        }

        rootPane.getScene().getWindow().sizeToScene();
    }

    private void createManyToManyAnnotation(NormalAnnotationExpr annotation) {
        annotation.setName("JoinTable");
        if (txtTable != null) {
            String value = "\"" + Utility.escape(txtTable.getText()) + "\"";
            annotation.addPair("name", value);
        }

        if (txtSource != null) {
            String value = "\"" + Utility.escape(txtSource.getText()) + "\"";

            NormalAnnotationExpr join = new NormalAnnotationExpr();
            join.setName("JoinColumn");
            join.addPair("name", value);
            annotation.addPair("joinColumns", join.toString());
        }

        if (txtDest != null) {
            String value = "\"" + Utility.escape(txtDest.getText()) + "\"";

            NormalAnnotationExpr join = new NormalAnnotationExpr();
            join.setName("JoinColumn");
            join.addPair("name", value);
            annotation.addPair("inverseJoinColumns", join.toString());
        }
    }

    private void setManyToManyAnnotation(NormalAnnotationExpr annotation) {
        annotation.getPairs().forEach(pair -> {
            if (pair.getNameAsString().equalsIgnoreCase("name") && txtTable != null) {
                txtTable.setText(pair.getValue().toString().replaceAll("\"", ""));
            } else if (pair.getNameAsString().equalsIgnoreCase("joinColumns") && txtSource != null) {
                NormalAnnotationExpr join = (NormalAnnotationExpr) JavaParser.parseAnnotation(pair.getValue().toString());
                join.getPairs().forEach(joinPair -> {
                    if (joinPair.getNameAsString().equalsIgnoreCase("name")) {
                        txtSource.setText(joinPair.getValue().toString().replaceAll("\"", ""));
                    }
                });
            } else if (pair.getNameAsString().equalsIgnoreCase("inverseJoinColumns") && txtDest != null) {
                NormalAnnotationExpr join = (NormalAnnotationExpr) JavaParser.parseAnnotation(pair.getValue().toString());
                join.getPairs().forEach(joinPair -> {
                    if (joinPair.getNameAsString().equalsIgnoreCase("name")) {
                        txtDest.setText(joinPair.getValue().toString().replaceAll("\"", ""));
                    }
                });
            }
        });
    }

    @FXML
    private void changeRelation(ActionEvent event) {
        PropertyModel.Relation value = cboRelations.getValue();
        cboSource.setDisable(value.equals(PropertyModel.Relation.None));
        this.realtionAnalysis(value);
    }

    @FXML
    void cancelClick(ActionEvent event) {
        if (this.cancelHandler != null) {
            this.cancelHandler.call(property);
        }

        this.close();
    }

    @FXML
    private void doneClick(ActionEvent event) {
        if (this.doneHandler != null) {
            this.typeAnalysis();
            property.setRelationAnnotation(new MarkerAnnotationExpr(cboRelations.getValue().toString()));
            NormalAnnotationExpr annotation = new NormalAnnotationExpr();
            switch (cboRelations.getValue()) {
                case ManyToMany:
                    this.createManyToManyAnnotation(annotation);
                    break;
                case None:
                    property.setRelationAnnotation(null);
                    break;
                default:
                    annotation.setName("JoinColumn");
                    String value = "\"" + Utility.escape(property.getColumnName()) + "\"";
                    annotation.addPair("name", value);

                    value = property.isRequired() ? "false" : "true";
                    annotation.addPair("nullable", value);
                    break;
            }
            property.setJoinAnnotaion(annotation);
            this.doneHandler.call(property);
        }
        this.close();
    }

}
