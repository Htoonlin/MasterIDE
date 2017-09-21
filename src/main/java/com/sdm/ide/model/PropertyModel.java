package com.sdm.ide.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.sdm.ide.component.annotation.FXColumn;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertyModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1027044065751863361L;

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

    @FXColumn(visible = false)
    private BooleanProperty readOnly;

    private Set<ValidateModel> validations;

    public PropertyModel(int index) {
        this();
        this.name = new SimpleStringProperty("prop" + index);
        this.label = new SimpleStringProperty("Property " + index);
        this.columnName = new SimpleStringProperty("col" + index);
        this.index = new SimpleIntegerProperty(index);
    }

    public PropertyModel() {
        this.name = new SimpleStringProperty("");
        this.label = new SimpleStringProperty("");
        this.type = new SimpleStringProperty("String");
        this.columnName = new SimpleStringProperty("");
        this.columnDef = new SimpleStringProperty("VARCHAR(255)");
        this.inputType = new SimpleStringProperty("text");
        this.index = new SimpleIntegerProperty();
        this.primary = new SimpleBooleanProperty(false);
        this.required = new SimpleBooleanProperty(false);
        this.hideInGrid = new SimpleBooleanProperty(true);
        this.readOnly = new SimpleBooleanProperty(false);
        this.auditable = new SimpleBooleanProperty(true);
        this.searchable = new SimpleBooleanProperty(false);
        this.jsonIgnore = new SimpleBooleanProperty(false);
        this.validations = new HashSet<>();
    }

    public Set<ValidateModel> getValidations() {
        return validations;
    }

    public void setValidations(Set<ValidateModel> validations) {
        this.validations = validations;
    }

    public void addValidation(ValidateModel validate) {
        this.validations.add(validate);
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
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
