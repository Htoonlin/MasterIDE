package com.sdm.ide.task;

import com.sdm.ide.helper.TypeManager;
import com.sdm.ide.helper.ValidationManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;
import com.sdm.ide.model.ValidateModel;

import javafx.concurrent.Task;

public class LoadEntityTask extends Task<EntityModel> {

    private final File javaFile;
    private final EntityModel entity;
    private boolean isComment;
    private boolean hasAnnotation;
    private Set<String> annotationList;

    public LoadEntityTask(File javaFile) {
        super();
        this.javaFile = javaFile;
        updateMessage("Init entity.");
        this.entity = new EntityModel(javaFile);
    }

    private void showMessage(String message, int milliSeconds) {
        try {
            updateMessage(message);
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            updateMessage(message);
        }
    }

    private void loadResourcePath() throws IOException {
        showMessage("Reading resource file.", 100);
        String moduleDir = this.javaFile.getParent().replaceAll("entity", "resource");
        File resourceFile = new File(moduleDir + "/" + this.javaFile.getName().replaceAll("Entity", "Resource"));
        if (resourceFile.exists() && resourceFile.isFile()) {
            List<String> codeLines = Files.readAllLines(resourceFile.toPath());
            for (String code : codeLines) {
                if (code.startsWith("@Path")) {
                    Map<String, String> values = this.valuesOfAnnotaion(code);
                    if (values.containsKey("value")) {
                        this.entity.setResourcePath(values.get("value"));
                    }
                    break;
                }
            }
        } else {
            showMessage("There is no resource file.", 100);
        }
    }

    @Override
    protected EntityModel call() throws Exception {
        showMessage("Reading entity file.", 0);
        try {
            List<String> codeLines = Files.readAllLines(this.javaFile.toPath());
            int i = 3;
            int max = codeLines.size() + 5;
            this.updateProgress(i, max);
            for (String code : codeLines) {
                updateProgress(i++, max);
                String cleanCode = code.trim();
                if (cleanCode.length() > 0) {
                    this.analysisCode(cleanCode);
                    showMessage("Processing line : " + i + " of " + max, 5);
                }
            }
            this.loadResourcePath();
            updateProgress(max, max);
            showMessage("Finished loaded entity.", 100);
        } catch (IOException ex) {
            throw ex;
        }
        return entity;
    }

    private void analysisCode(String code) {
        /* Skip Statement */
        if (code.startsWith("//")) {
            return;
        }

        /* Check Multiline Comment */
        if (code.startsWith("/*")) {
            isComment = true;
        }
        isComment = (isComment && !code.contains("*/"));
        if (code.endsWith("*/") || isComment) {
            return;
        }

        /* Check Annotation */
        if (code.startsWith("@")) {
            hasAnnotation = true;
        }
        if (hasAnnotation) {
            if (this.annotationList == null) {
                this.annotationList = new HashSet<>();
            }
            this.annotationList.add(code);
        }
        hasAnnotation = !(code.endsWith(";") || code.endsWith("{"));

        if (!hasAnnotation && this.annotationList != null) {
            this.analysisAnnotation(code);
            this.annotationList = null;
            return;
        }

        /* Check Import */
        if (code.startsWith("import")) {
            String[] importSplitter = code.split(" ");
            if (importSplitter.length > 1) {
                this.entity.addImport(importSplitter[1].replaceAll(";", "").trim());
            }
        }
    }

    private void analysisAnnotation(String code) {
        /* Check Annotation Type */
        if (code.contains("class")) {
            this.classAnalysis(code);
        } else if (code.endsWith(";")) {
            this.propertyAnalysis(code);
        } else if (code.matches("public\\s+\\w+\\sget.+")) {
            this.getterAnalysis(code);
        }
    }

    private void classAnalysis(String code) {
        for (String annotation : this.annotationList) {
            annotation = annotation.trim();
            if (annotation.startsWith("@Audited")) {
                this.entity.setAuditable(true);
            } else if (annotation.startsWith("@DynamicUpdate")) {
                int start = annotation.indexOf("@DynamicUpdate");
                Map<String, String> values = this.valuesOfAnnotaion(annotation.substring(start, annotation.length()));
                if (values.containsKey("value")) {
                    this.entity.setDynamicUpdate(Boolean.parseBoolean(values.get("value")));
                }
            } else if (annotation.startsWith("@Entity")) {
                int start = annotation.indexOf("@Entity");
                Map<String, String> values = this.valuesOfAnnotaion(annotation.substring(start, annotation.length()));
                if (values.containsKey("name")) {
                    this.entity.setEntityName(values.get("name"));
                } else {
                    this.entity.setEntityName(entity.getModuleName() + "." + entity.getName());
                }
            } else if (annotation.startsWith("@Table")) {
                int start = annotation.indexOf("@Table");
                Map<String, String> values = this.valuesOfAnnotaion(annotation.substring(start, annotation.length()));
                if (values.containsKey("name")) {
                    this.entity.setTableName(values.get("name"));
                }
            }
        }
    }

    private void propertyAnalysis(String code) {
        // Check properties must be private and not transient.
        if (!code.matches("private.+;") || this.annotationList.contains("@Transient")) {
            return;
        }

        // Check properties must have exists UIStructure || Column annotation.
        boolean isProperty = false;
        for (String annotation : this.annotationList) {
            if (annotation.startsWith("@UIStructure") || annotation.startsWith("@Column")) {
                isProperty = true;
                break;
            }
        }

        if (!isProperty) {
            return;
        }

        PropertyModel property = new PropertyModel();

        if (code.endsWith(";")) {
            String[] codeSplit = code.replaceAll(";", "").replaceAll(", ", ",").split("\\s");
            if (codeSplit.length > 2) {
                property.setType(codeSplit[1].trim());
                property.setName(codeSplit[2].trim());
            }
        }

        property.setAuditable(true);
        property.setJsonIgnore(false);
        property.setSearchable(false);
        property.setRequired(false);

        this.propertyAnnotataion(property, code);

        if (this.entity.getSearchFields() != null && property.getColumnName() != null
                && this.entity.getSearchFields().contains(property.getColumnName().trim())) {
            property.setSearchable(true);
        }

        if (property.isPrimary()) {
            this.entity.setPrimaryProperty(property);
        }
        this.entity.addProperty(property);
    }

    private void getterAnalysis(String code) {
        Matcher matcher = Pattern.compile("\\sget\\w+").matcher(code);

        while (matcher.find()) {
            String getter = matcher.group();
            getter = getter.trim().substring(3, getter.length() - 1);
            PropertyModel property = this.entity.findProperty(getter);
            if (property != null) {
                this.propertyAnnotataion(property, code);
            }
        }
    }

    private void propertyAnnotataion(PropertyModel property, String code) {
        for (String annotation : this.annotationList) {
            try {
                if (annotation.startsWith("@Formula") && code.matches(".*search;")) {
                    this.searchColumns(annotation);
                    return; // If search field, stop property analysis.
                }
                if (annotation.startsWith("@Id") || annotation.startsWith("@GeneratedValue")) {
                    property.setPrimary(true);
                } else if (annotation.startsWith("@JsonIgnore")) {
                    property.setJsonIgnore(true);
                } else if (annotation.startsWith("@NotAudited")) {
                    property.setAuditable(false);
                } else if (annotation.startsWith("@UIStructure")) {
                    this.uiAnalysis(property, annotation);
                } else if (annotation.startsWith("@Column")) {
                    this.columnAnalysis(property, annotation);
                } else if (ValidationManager.getInstance().checkConstraint(annotation)) {
                    this.validationAnalysis(property, annotation);
                }
            } catch (Exception ex) {
                showMessage(ex.getLocalizedMessage(), 100);
            }
        }
    }

    private void validationAnalysis(PropertyModel property, String annotation) {
        int bracketStart = annotation.indexOf("(");
        ValidateModel validation = new ValidateModel(annotation.substring(1, bracketStart));
        validation.setValues(this.valuesOfAnnotaion(annotation.substring(bracketStart, annotation.length())));
        property.addValidation(validation);
    }

    private void uiAnalysis(PropertyModel property, String annotation) {
        int start = annotation.indexOf("@UIStructure");
        Map<String, String> values = this.valuesOfAnnotaion(annotation.substring(start, annotation.length()));
        if (values.containsKey("inputType")) {
            property.setInputType(values.get("inputType").replaceAll(".*\\.", ""));
        } else {
            try {
                JSONObject json = TypeManager.getInstance().getLinkType(property.getType());
                property.setInputType(json.getString("input"));
            } catch (IOException e) {
                updateMessage(e.getLocalizedMessage());
            }
        }

        property.setLabel(values.getOrDefault("label", property.getName()));
        property.setHideInGrid(Boolean.parseBoolean(values.getOrDefault("hideInGrid", "true")));
        property.setReadOnly(Boolean.parseBoolean(values.getOrDefault("readOnly", "false")));
        property.setIndex(Integer.parseInt(values.getOrDefault("order", "0")));
    }

    private void columnAnalysis(PropertyModel property, String annotation) {
        int start = annotation.indexOf("@Column");
        Map<String, String> values = this.valuesOfAnnotaion(annotation.substring(start, annotation.length()));
        property.setColumnName(values.getOrDefault("name", property.getName()));
        boolean required = !Boolean.parseBoolean(values.getOrDefault("nullable", "true"));
        if (required) {
            property.setRequired(true);
            if (!property.isPrimary()) {
                // Set NotNull Validate
                property.addValidation(new ValidateModel("NotNull"));
            }
        }

        if (values.containsKey("columnDefinition")) {
            property.setColumnDef(values.get("columnDefinition"));
        } else {
            try {
                JSONObject json = TypeManager.getInstance().getLinkType(property.getType());
                property.setColumnDef(json.getString("db"));
            } catch (IOException e) {
                showMessage(e.getLocalizedMessage(), 100);
            }
        }
    }

    private void searchColumns(String code) {
        int start = code.indexOf("@Formula");
        Map<String, String> values = this.valuesOfAnnotaion(code.substring(start, code.length()));
        if (values.containsKey("value")) {
            String[] columns = this.innerValue(values.get("value"), "(", ")").split(",");
            for (String col : columns) {
                this.entity.addSearchField(col.trim());
            }
        }
    }

    private String innerValue(String source, String start, String end) {
        Matcher matcher = Pattern.compile("\\" + start + "(.*)\\" + end).matcher(source);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return source;
    }

    private Map<String, String> valuesOfAnnotaion(String code) {
        code = this.innerValue(code, "(", ")").replaceAll("\"", "");
        Matcher matcher = Pattern.compile("([^=^,]+)=([^,]+)").matcher(code);
        Map<String, String> values = new HashMap<>();
        boolean hasMatch = false;
        while (matcher.find()) {
            hasMatch = true;
            String value = matcher.group();
            String[] valueSplit = value.split("=");
            if (valueSplit.length > 1) {
                values.put(valueSplit[0].trim(), valueSplit[1].trim());
            }
        }
        if (!hasMatch) {
            values.put("value", code);
        }

        return values;
    }
}
