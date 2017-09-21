package com.sdm.ide.model;

import java.io.Serializable;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MailModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1583004081964832320L;
    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty user;
    private final StringProperty password;

    public MailModel() {
        this.host = new SimpleStringProperty("smtp.gmail.com");
        this.port = new SimpleIntegerProperty(465);
        this.user = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
    }

    public StringProperty hostProperty() {
        return this.host;
    }

    public String getHost() {
        return this.hostProperty().get();
    }

    public void setHost(final String host) {
        this.hostProperty().set(host);
    }

    public IntegerProperty portProperty() {
        return this.port;
    }

    public int getPort() {
        return this.portProperty().get();
    }

    public void setPort(final int port) {
        this.portProperty().set(port);
    }

    public StringProperty userProperty() {
        return this.user;
    }

    public String getUser() {
        return this.userProperty().get();
    }

    public void setUser(final String user) {
        this.userProperty().set(user);
    }

    public StringProperty passwordProperty() {
        return this.password;
    }

    public String getPassword() {
        return this.passwordProperty().get();
    }

    public void setPassword(final String password) {
        this.passwordProperty().set(password);
    }

}
