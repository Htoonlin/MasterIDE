package com.sdm.ide.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ValidateModel {

    private StringProperty name;
    private Map<String, String> values;

    public ValidateModel() {
        this.name = new SimpleStringProperty();
        this.values = new HashMap<>();
    }

    public ValidateModel(String name) {
        this();
        this.setName(name);
    }

    @Override
    public String toString() {
        return this.getAnnotation();
    }

    public String getAnnotation() {
        String annotation = "@" + this.getName();
        List<String> data = new ArrayList<>();
        for (String key : this.values.keySet()) {
            String value = this.values.get(key);
            if (!value.matches("[0-9]+")) {
                value = "\"" + StringEscapeUtils.escapeJava(value) + "\"";
            }

            data.add(key + "=" + value);
        }

        if (data.size() > 0) {
            annotation += "(" + String.join(", ", data) + ")";
        }

        return annotation;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public void addValue(String key, String value) {
        this.values.put(key, value);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ValidateModel other = (ValidateModel) obj;
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
