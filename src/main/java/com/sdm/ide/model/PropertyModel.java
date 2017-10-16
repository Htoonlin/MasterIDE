package com.sdm.ide.model;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.sdm.ide.component.annotation.FXColumn;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertyModel implements Serializable {

    private static final long serialVersionUID = 1027044065751863361L;

    public enum Relation {
        None,
        OneToOne,
        OneToMany,
        ManyToOne,
        ManyToMany;
    }


    /* Java Properties */
    @FXColumn(width = 120)
    private StringProperty name;

    @FXColumn(width = 100)
    private StringProperty type;

    @FXColumn(visible = false)
    private BooleanProperty auditable;

    @FXColumn(visible = false)
    private BooleanProperty searchable;

    @FXColumn(visible = false)
    private BooleanProperty jsonIgnore;

    private final StringProperty description;

    /* Database */
    @FXColumn(label = "DB Column", width = 120)
    private StringProperty columnName;

    @FXColumn(label = "DB Type", width = 120)
    private StringProperty columnDef;

    @FXColumn(width = 50)
    private BooleanProperty primary;

    @FXColumn(width = 50)
    private BooleanProperty required;

    /* UI Properties */
    @FXColumn(width = 120)
    private StringProperty label;

    @FXColumn(width = 100)
    private StringProperty inputType;

    @FXColumn(width = 75)
    private IntegerProperty index;

    @FXColumn(width = 50)
    private BooleanProperty hideInGrid;

    @FXColumn(width = 50)
    private BooleanProperty allowMMFont;

    @FXColumn(visible = false)
    private BooleanProperty readOnly;

    private FieldDeclaration fieldObject;

    private MethodDeclaration getter;

    private MethodDeclaration setter;

    /* Relational Info */
    private final StringProperty relationSource = new SimpleStringProperty();
    private AnnotationExpr relationAnnotation;
    private NormalAnnotationExpr joinAnnotaion;

    /**
     * Field Validations
     */
    private Set<AnnotationExpr> validations;

    private boolean systemGenerated;

    public PropertyModel(int index) {
        this();
        this.name = new SimpleStringProperty("prop" + index);
        this.label = new SimpleStringProperty("Property " + index);
        this.columnName = new SimpleStringProperty("col" + index);
        this.index = new SimpleIntegerProperty(index);
        this.systemGenerated = true;
    }

    public PropertyModel() {
        this.name = new SimpleStringProperty("");
        this.label = new SimpleStringProperty("");
        this.type = new SimpleStringProperty("String");
        this.description = new SimpleStringProperty("");
        this.columnName = new SimpleStringProperty("");
        this.columnDef = new SimpleStringProperty("VARCHAR(255)");
        this.inputType = new SimpleStringProperty("text");
        this.index = new SimpleIntegerProperty();
        this.primary = new SimpleBooleanProperty(false);
        this.required = new SimpleBooleanProperty(false);
        this.hideInGrid = new SimpleBooleanProperty(false);
        this.allowMMFont = new SimpleBooleanProperty(false);
        this.readOnly = new SimpleBooleanProperty(false);
        this.auditable = new SimpleBooleanProperty(true);
        this.searchable = new SimpleBooleanProperty(false);
        this.jsonIgnore = new SimpleBooleanProperty(false);
        this.validations = new HashSet<>();
    }

    public FieldDeclaration getFieldObject() {
        return fieldObject;
    }

    public void setFieldObject(FieldDeclaration fieldObject) {
        this.fieldObject = fieldObject;
    }

    public MethodDeclaration getGetter() {
        return getter;
    }

    public void setGetter(MethodDeclaration getter) {
        this.getter = getter;
    }

    public MethodDeclaration getSetter() {
        return setter;
    }

    public void setSetter(MethodDeclaration setter) {
        this.setter = setter;
    }

    public Set<AnnotationExpr> getValidations() {
        return validations;
    }

    public void setValidations(Set<AnnotationExpr> validations) {
        this.validations = validations;
    }

    public void addValidation(AnnotationExpr annotation) {
        if (this.validations.contains(annotation)) {
            if (this.fieldObject != null) {
                this.fieldObject.addAnnotation(annotation);
            }
            this.validations.add(annotation);
        }
    }

    public StringProperty nameProperty() {
        return this.name;
    }

    public String getName() {
        return this.nameProperty().get();
    }

    public void setName(final String name) {
        this.nameProperty().set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty labelProperty() {
        return this.label;
    }

    public String getLabel() {
        return this.labelProperty().get();
    }

    public void setLabel(final String label) {
        this.labelProperty().set(label);
    }

    public StringProperty typeProperty() {
        return this.type;
    }

    public String getType() {
        return this.typeProperty().get();
    }

    public void setType(final String type) {
        this.typeProperty().set(type);
    }

    public BooleanProperty primaryProperty() {
        return this.primary;
    }

    public boolean isPrimary() {
        return this.primaryProperty().get();
    }

    public void setPrimary(final boolean primary) {
        this.primaryProperty().set(primary);
    }

    public BooleanProperty requiredProperty() {
        return this.required;
    }

    public boolean isRequired() {
        return this.requiredProperty().get();
    }

    public void setRequired(final boolean required) {
        this.requiredProperty().set(required);
    }

    public BooleanProperty hideInGridProperty() {
        return this.hideInGrid;
    }

    public boolean isHideInGrid() {
        return this.hideInGridProperty().get();
    }

    public void setHideInGrid(final boolean hideInGrid) {
        this.hideInGridProperty().set(hideInGrid);
    }

    public boolean isAllowMMFont() {
        return allowMMFont.get();
    }

    public void setAllowMMFont(boolean value) {
        allowMMFont.set(value);
    }

    public BooleanProperty allowMMFontProperty() {
        return allowMMFont;
    }

    public BooleanProperty readOnlyProperty() {
        return this.readOnly;
    }

    public boolean isReadOnly() {
        return this.readOnlyProperty().get();
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnlyProperty().set(readOnly);
    }

    public BooleanProperty auditableProperty() {
        return this.auditable;
    }

    public boolean isAuditable() {
        return this.auditableProperty().get();
    }

    public void setAuditable(final boolean auditable) {
        this.auditableProperty().set(auditable);
    }

    public BooleanProperty searchableProperty() {
        return this.searchable;
    }

    public boolean isSearchable() {
        return this.searchableProperty().get();
    }

    public void setSearchable(final boolean searchable) {
        this.searchableProperty().set(searchable);
    }

    public BooleanProperty jsonIgnoreProperty() {
        return this.jsonIgnore;
    }

    public boolean isJsonIgnore() {
        return this.jsonIgnoreProperty().get();
    }

    public void setJsonIgnore(final boolean jsonIgnore) {
        this.jsonIgnoreProperty().set(jsonIgnore);
    }

    public StringProperty inputTypeProperty() {
        return this.inputType;
    }

    public String getInputType() {
        return this.inputTypeProperty().get();
    }

    public void setInputType(final String inputType) {
        this.inputTypeProperty().set(inputType);
    }

    public final StringProperty columnNameProperty() {
        return this.columnName;
    }

    public final String getColumnName() {
        return this.columnNameProperty().get();
    }

    public final void setColumnName(final String columnName) {
        this.columnNameProperty().set(columnName);
    }

    public final IntegerProperty indexProperty() {
        return this.index;
    }

    public final int getIndex() {
        return this.indexProperty().get();
    }

    public final void setIndex(final int index) {
        this.indexProperty().set(index);
    }

    public final StringProperty columnDefProperty() {
        return this.columnDef;
    }

    public final String getColumnDef() {
        return this.columnDefProperty().get();
    }

    public final void setColumnDef(final String columnDef) {
        this.columnDefProperty().set(columnDef);
    }

    public boolean isSystemGenerated() {
        return systemGenerated;
    }

    public void setSystemGenerated(boolean systemGenerated) {
        this.systemGenerated = systemGenerated;
    }

    public String getRelationSource() {
        return relationSource.get();
    }

    public void setRelationSource(String value) {
        relationSource.set(value);
    }

    public StringProperty relationSourceProperty() {
        return relationSource;
    }

    public AnnotationExpr getRelationAnnotation() {
        return relationAnnotation;
    }

    public void setRelationAnnotation(AnnotationExpr relationAnnotation) {
        this.relationAnnotation = relationAnnotation;
    }

    public NormalAnnotationExpr getJoinAnnotaion() {
        return joinAnnotaion;
    }

    public void setJoinAnnotaion(NormalAnnotationExpr joinAnnotaion) {
        this.joinAnnotaion = joinAnnotaion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PropertyModel other = (PropertyModel) obj;
        if (columnName == null) {
            if (other.columnName != null) {
                return false;
            }
        } else if (!columnName.equals(other.columnName)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.get().equals(other.name.get())) {
            return false;
        }
        return true;
    }

}
