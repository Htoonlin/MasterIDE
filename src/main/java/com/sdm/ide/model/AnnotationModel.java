package com.sdm.ide.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.lang.StringEscapeUtils;

public class AnnotationModel {

    private StringProperty name;
    private Map<String, String> values;

    public AnnotationModel() {
        this.name = new SimpleStringProperty();
        this.values = new HashMap<>();
    }

    public AnnotationModel(String name) {
        this();
        this.setName(name);
    }

    @Override
    public String toString() {
        return this.getAnnotation();
    }

    public String getAnnotation() {
        try {
            String annotation = "@" + this.getName();
            if (this.values != null) {
                List<String> data = new ArrayList<>();
                for (String key : this.values.keySet()) {
                    String value = this.values.get(key);
                    if (!value.matches("[0-9]+|true|false")) {
                        value = "\"" + StringEscapeUtils.escapeJava(value) + "\"";
                    }

                    data.add(key + "=" + value);
                }

                if (data.size() > 0) {
                    annotation += "(" + String.join(", ", data) + ")";
                }
            }
            return annotation;
        } catch (Exception ex) {
            return "";
        }
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
        AnnotationModel other = (AnnotationModel) obj;
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
