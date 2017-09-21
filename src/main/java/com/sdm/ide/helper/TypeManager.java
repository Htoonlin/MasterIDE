package com.sdm.ide.helper;

import com.sun.media.jfxmedia.track.Track;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;

import org.json.JSONObject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.IOUtils;

public class TypeManager {

    private JSONObject dbTypes;
    private JSONObject javaTypes;
    private final String[] INPUT_TYPES = {"text", "textarea", "password", "number", "email", "url", "tel", "color",
        "file", "image", "range", "month", "week", "date", "datetime", "time", "hidden", "checkbox", "radio",
        "combo", "list", "map", "autocomplete", "object", "objectlist"};

    private static TypeManager instance;

    public TypeManager() throws IOException {
        // Load JavaTypes
        try (InputStream jsonStream = getClass().getResourceAsStream("/json/JavaTypes.json")) {
            this.javaTypes = new JSONObject(IOUtils.toString(jsonStream, Charset.defaultCharset()));
        } catch (IOException ex) {
            throw ex;
        }

        // Load DBTypes
        try (InputStream jsonStream = getClass().getResourceAsStream("/json/DBTypes.json")) {
            this.dbTypes = new JSONObject(IOUtils.toString(jsonStream, Charset.defaultCharset()));
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static TypeManager getInstance() throws IOException {
        if (instance == null) {
            instance = new TypeManager();
        }
        return instance;
    }

    public ObservableList<String> getInputTypes() {
        return FXCollections.observableArrayList(this.INPUT_TYPES);
    }

    public ObservableList<String> getDBTypes() {
        Map<String, Object> typeMap = this.dbTypes.toMap();
        return FXCollections.observableArrayList(typeMap.keySet());
    }

    public ObservableList<String> getJavaTypes() {
        Map<String, Object> typeMap = this.javaTypes.toMap();
        return FXCollections.observableArrayList(typeMap.keySet());
    }

    public JSONObject getLinkType(String javaType) {
        if (this.javaTypes.has(javaType)) {
            return this.javaTypes.getJSONObject(javaType);
        }

        return (new JSONObject().put("db", "VARCHAR").put("input", "text"));
    }

    public String getJavaType(String dbType) throws IOException {
        if (this.dbTypes.has(dbType)) {
            return this.dbTypes.getString(dbType);
        }

        return "String";
    }
}
