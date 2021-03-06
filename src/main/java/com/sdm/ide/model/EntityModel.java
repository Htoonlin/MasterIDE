package com.sdm.ide.model;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.sdm.core.Globalizer;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class EntityModel implements Serializable {

    private static final long serialVersionUID = -7804109111579775365L;

    //Entity Info
    private final StringProperty name;
    private final StringProperty entityName;
    private final StringProperty tableName;
    private final StringProperty moduleName;
    private final StringProperty resourcePath;
    private final BooleanProperty auditable;
    private final BooleanProperty dynamicUpdate;
    private final BooleanProperty mappedWithDB;
    private final StringProperty description;

    //Properties Info
    private Set<String> searchFields;
    private Set<PropertyModel> properties;
    private PropertyModel primaryProperty;
    private Map<String, String> namedQueries;

    //Java Info
    private File file;
    private CompilationUnit compiledObject;
    private ClassOrInterfaceDeclaration entityObject;
    private Set<String> importedObjects;

    public EntityModel() {
        this.name = new SimpleStringProperty("");
        this.entityName = new SimpleStringProperty("");
        this.tableName = new SimpleStringProperty("");
        this.moduleName = new SimpleStringProperty("");
        this.resourcePath = new SimpleStringProperty("");
        this.auditable = new SimpleBooleanProperty(true);
        this.dynamicUpdate = new SimpleBooleanProperty(true);
        this.mappedWithDB = new SimpleBooleanProperty(true);
        this.description = new SimpleStringProperty("");
        this.searchFields = new HashSet<>();
        this.properties = new HashSet<>();
        this.namedQueries = new HashMap<>();
        this.importedObjects = new HashSet<>(Arrays.asList(
                "java.util.*",
                "java.math.*",
                "javax.persistence.*",
                "javax.validation.constraints.*",
                "com.fasterxml.jackson.annotation.*",
                "org.hibernate.envers.*",
                "com.sdm.core.util.MyanmarFontManager",
                "java.io.Serializable",
                "javax.ws.rs.core.UriBuilder",
                "org.hibernate.annotations.Formula",
                "org.hibernate.annotations.DynamicUpdate",
                "com.sdm.core.hibernate.entity.DefaultEntity",
                "com.sdm.core.response.model.LinkModel",
                "com.sdm.core.ui.UIInputType",
                "com.sdm.core.ui.UIStructure"
        ));
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

    public void removeProperty(PropertyModel property) {
        this.removeFieldCode(property.getName());

        //Remove Property
        this.properties.remove(property);
    }

    public void removeFieldCode(String name) {
        if(this.entityObject == null){
            return;
        }
        this.entityObject.getFieldByName(name).ifPresent(field -> {
            this.entityObject.remove(field);
        });

        //Remove Getter/Settern and MMFontGetter/Setter
        String[] methodPrefixs = {"get", "set", "getMM", "setMM"};
        for (String prefix : methodPrefixs) {
            String methodName = prefix + Globalizer.capitalize(name);
            this.entityObject.getMethodsByName(methodName).forEach((method) -> {
                this.entityObject.remove(method);
            });
        }
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

    public CompilationUnit getCompiledObject() {
        return compiledObject;
    }

    public void setCompiledObject(CompilationUnit compiledObject) {
        this.compiledObject = compiledObject;
    }

    public ClassOrInterfaceDeclaration getEntityObject() {
        return entityObject;
    }

    public void setEntityObject(ClassOrInterfaceDeclaration entityObject) {
        this.entityObject = entityObject;
    }

    public Set<String> getImportedObjects() {
        return importedObjects;
    }

    public void setImportedObjects(Set<String> importedObjects) {
        this.importedObjects = importedObjects;
    }

    public void addImport(String importedObject) {
        for (String imp : this.importedObjects) {
            if (imp.endsWith("*") && importedObject.startsWith(imp.replaceAll("\\.\\*", ""))) {
                return;
            }
        }
        this.importedObjects.add(importedObject);
    }

    public Map<String, String> getNamedQueries() {
        return namedQueries;
    }

    public void removeNamedQuery(String name) {
        this.namedQueries.remove(name);
    }

    public String getNamedQuery(String name) {
        return this.namedQueries.getOrDefault(name, "");
    }

    public void setNamedQueries(Map<String, String> namedQueries) {
        this.namedQueries = namedQueries;
    }

    public void addNamedQuery(String name, String query) {
            this.namedQueries.put(name, query);
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.moduleName);
        return hash;
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
        final EntityModel other = (EntityModel) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.moduleName, other.moduleName)) {
            return false;
        }
        return true;
    }

}
