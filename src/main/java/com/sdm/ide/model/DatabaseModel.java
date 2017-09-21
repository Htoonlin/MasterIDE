package com.sdm.ide.model;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DatabaseModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6171162661380658706L;
    private final SimpleStringProperty host;
    private final StringProperty schema;

    private final SimpleStringProperty user;
    private final SimpleStringProperty password;

    public DatabaseModel() {
        this.host = new SimpleStringProperty("localhost:3306");
        this.schema = new SimpleStringProperty("sundwe_api");
        this.user = new SimpleStringProperty("root");
        this.password = new SimpleStringProperty("");
    }

    public SimpleStringProperty hostProperty() {
        return this.host;
    }

    public String getHost() {
        return this.hostProperty().get();
    }

    public void setHost(final String host) {
        this.hostProperty().set(host);
    }

    public String getSchema() {
        return schema.get();
    }

    public void setSchema(String value) {
        schema.set(value);
    }

    public StringProperty schemaProperty() {
        return schema;
    }

    public SimpleStringProperty userProperty() {
        return this.user;
    }

    public String getUser() {
        return this.userProperty().get();
    }

    public void setUser(final String user) {
        this.userProperty().set(user);
    }

    public SimpleStringProperty passwordProperty() {
        return this.password;
    }

    public String getPassword() {
        return this.passwordProperty().get();
    }

    public void setPassword(final String password) {
        this.passwordProperty().set(password);
    }

}
