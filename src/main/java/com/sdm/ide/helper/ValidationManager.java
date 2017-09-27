package com.sdm.ide.helper;

import com.sdm.Constants;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class ValidationManager {

    private JSONObject rulesMapping;

    private static ValidationManager instance;

    private final Map<String, String> patterns;

    public synchronized static ValidationManager getInstance() throws Exception {
        if (instance == null) {
            instance = new ValidationManager();
        }

        return instance;
    }

    public ValidationManager() throws Exception {
        // Load Validation Rules
        File rules = new File(getClass().getResource("/json/ValidationRules.json").getFile());
        try (InputStream jsonStream = getClass().getResourceAsStream("/json/ValidationRules.json")) {
            rulesMapping = new JSONObject(IOUtils.toString(jsonStream, Charset.defaultCharset()));
        } catch (IOException ex) {
            throw ex;
        }

        // Load Regex Patterns
        patterns = new HashMap<>();
        for (Field pattern : Constants.Pattern.class.getFields()) {
            patterns.put(pattern.getName(), pattern.get(Constants.Pattern.class).toString());
        }
    }

    public boolean checkConstraint(String constraint) {
        if (!constraint.startsWith("@")) {
            return false;
        }

        for (String rule : rulesMapping.keySet()) {
            if (constraint.contains("@" + rule)) {
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getPatterns() {
        return patterns;
    }

    public String getRegex(String key) {
        return this.patterns.get(key);
    }

    public JSONObject getValidations() {
        return this.rulesMapping;
    }

    public JSONObject getValidation(String key) {
        if (rulesMapping.has(key)) {
            return rulesMapping.getJSONObject(key);
        }
        return null;
    }
}
