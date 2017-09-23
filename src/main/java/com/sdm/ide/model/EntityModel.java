package com.sdm.ide.model;

import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class EntityModel implements Serializable {

    private static final long serialVersionUID = -7804109111579775365L;

    private final StringProperty name;
    private final StringProperty entityName;
    private final StringProperty tableName;
    private final StringProperty moduleName;
    private final StringProperty resourcePath;
    private final BooleanProperty auditable;
    private final BooleanProperty dynamicUpdate;
    private final BooleanProperty mappedWithDB;

    private File file;
    private Set<String> imports;
    private Set<String> searchFields;
    private Set<PropertyModel> properties;
    private PropertyModel primaryProperty;

    public EntityModel() {
        this.name = new SimpleStringProperty("");
        this.entityName = new SimpleStringProperty("");
        this.tableName = new SimpleStringProperty("");
        this.moduleName = new SimpleStringProperty("");
        this.resourcePath = new SimpleStringProperty("");
        this.auditable = new SimpleBooleanProperty(false);
        this.dynamicUpdate = new SimpleBooleanProperty(false);
        this.mappedWithDB = new SimpleBooleanProperty(true);
        this.imports = this.initImports();
        this.searchFields = new HashSet<>();
        this.properties = new HashSet<>();
    }

    public EntityModel(File file) {
        this();
        this.file = file;
        this.setName(file.getName().replaceAll("\\.java", ""));
        String[] pathSplit = ProjectManager.getFilePath(file).split("/java/");
        if (pathSplit.length == 2) {
            String basePackage = pathSplit[1].replaceAll("/", ".").replaceAll("\\.java", "");
            boolean isMapped = HibernateManager.getInstance().getEntities().contains(basePackage);
            this.setMappedWithDB(isMapped);
            int end = basePackage.indexOf(".entity");
            this.setModuleName(basePackage.substring(0, end));
            String dbModule = basePackage.toLowerCase().replaceAll("com\\.sdm\\.", "").replaceAll("(\\.)*entity", "");
            this.setTableName("tbl_" + dbModule.replaceAll("\\.", "_"));
            this.setEntityName(basePackage);
            String resourceImport = this.getModuleName() + ".resource." + this.getName().replaceAll("Entity", "Resource");
            this.addImport(resourceImport);
        }
    }

    private Set<String> initImports() {
        return new HashSet<>(Arrays.asList("java.io.Serializable", "java.util.*", "java.math.*", "javax.persistence.*",
                "javax.validation.constraints.*", "com.fasterxml.jackson.annotation.*", "org.hibernate.envers.*",
                "javax.ws.rs.core.UriBuilder", "org.hibernate.annotations.Formula", "org.hibernate.annotations.DynamicUpdate",
                "com.sdm.core.hibernate.entity.DefaultEntity", "com.sdm.core.response.LinkModel",
                "com.sdm.core.ui.UIInputType", "com.sdm.core.ui.UIStructure"));
    }

    public String getPrimaryType() {
        if (this.primaryProperty != null) {
            switch (this.primaryProperty.getType().toLowerCase()) {
                case "byte":
                    return "Byte";
                case "short":
                    return "Short";
                case "int":
                case "integer":
                    return "Integer";
                case "long":
                    return "Long";
                case "float":
                    return "Float";
                case "double":
                    return "Double";
                case "char":
                    return "Character";
                case "boolean":
                    return "Boolean";
                default:
                    return this.primaryProperty.getType();
            }
        }
        return "";
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getBaseName() {
        return this.getName().replaceAll("Entity", "");
    }

    public Set<String> getSearchFields() {
        return searchFields;
    }

    public void setSearchFields(Set<String> searchFields) {
        this.searchFields = searchFields;
    }

    public void addSearchField(String field) {
        this.searchFields.add(field);
    }

    public Set<PropertyModel> getProperties() {
        return properties;
    }

    public void setProperties(Set<PropertyModel> properties) {
        this.properties = properties;
    }

    public void addProperty(PropertyModel property) {
        this.properties.add(property);
    }

    public PropertyModel findProperty(String name) {
        if (this.properties != null && this.properties.size() > 0) {
            for (PropertyModel prop : this.properties) {
                if (prop.getName().equalsIgnoreCase(name)) {
                    return prop;
                }
            }
        }

        return null;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void setImports(Set<String> imports) {
        this.imports = imports;
    }

    public void addImport(String value) {
        for (String imp : this.imports) {
            if (imp.endsWith("*") && value.startsWith(imp.replaceAll("\\.\\*", ""))) {
                return;
            }
        }
        this.imports.add(value);
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

    public StringProperty tableNameProperty() {
        return this.tableName;
    }

    public String getTableName() {
        return this.tableNameProperty().get();
    }

    public void setTableName(final String tableName) {
        this.tableNameProperty().set(tableName);
    }

    public StringProperty moduleNameProperty() {
        return this.moduleName;
    }

    public String getModuleName() {
        return this.moduleNameProperty().get();
    }

    public void setModuleName(final String moduleName) {
        this.moduleNameProperty().set(moduleName);
    }

    public BooleanProperty auditableProperty() {
        return this.auditable;
    }

    public boolean isAuditable() {
        return this.auditableProperty().get();
    }

    public void setAuditable(final boolean auditable) {
        if (!auditable) {
            this.addImport("org.hibernate.envers.NotAudited");
        }
        this.auditableProperty().set(auditable);
    }

    public final StringProperty entityNameProperty() {
        return this.entityName;
    }

    public final String getEntityName() {
        return this.entityNameProperty().get();
    }

    public final void setEntityName(final String entityName) {
        this.entityNameProperty().set(entityName);
    }

    public final BooleanProperty dynamicUpdateProperty() {
        return this.dynamicUpdate;
    }

    public final boolean isDynamicUpdate() {
        return this.dynamicUpdateProperty().get();
    }

    public final void setDynamicUpdate(final boolean dynamicUpdate) {
        if (dynamicUpdate) {
            this.addImport("org.hibernate.annotations.DynamicUpdate");
        }
        this.dynamicUpdateProperty().set(dynamicUpdate);
    }

    public StringProperty resourcePathProperty() {
        return this.resourcePath;
    }

    public String getResourcePath() {
        return this.resourcePathProperty().get();
    }

    public void setResourcePath(final String resourcePath) {
        this.resourcePathProperty().set(resourcePath);
    }

    public PropertyModel getPrimaryProperty() {
        return primaryProperty;
    }

    public void setPrimaryProperty(PropertyModel primaryProperty) {
        this.primaryProperty = primaryProperty;
    }

    public BooleanProperty mappedWithDBProperty() {
        return this.mappedWithDB;
    }

    public boolean isMappedWithDB() {
        return this.mappedWithDBProperty().get();
    }

    public void setMappedWithDB(final boolean mappedWithDB) {
        this.mappedWithDBProperty().set(mappedWithDB);
    }

}
