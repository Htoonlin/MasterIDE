/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.sdm.core.Globalizer;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ValidationManager;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javafx.concurrent.Task;
import javax.xml.transform.TransformerException;

/**
 *
 * @author htoonlin
 */
public class WriteEntityTask extends Task<Boolean> {

    private final EntityModel entity;

    private final String[] processAnnotations = {
        "UIStructure", "Column",
        "Audited", "NotAudited",
        "JsonIgnore",
        "Id", "GeneratedValue"
    };

    public WriteEntityTask(EntityModel entity) {
        this.entity = entity;
    }

    private void showMessage(String message) {
        try {
            updateMessage(message);
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            updateMessage(message);
        }
    }

    private void cleanEntityAnnotations(ClassOrInterfaceDeclaration entityObject) {
        entityObject.getAnnotationByName("Audited").ifPresent(anno -> entityObject.remove(anno));
        entityObject.getAnnotationByName("DynamicUpdate").ifPresent(anno -> entityObject.remove(anno));
        entityObject.getAnnotationByName("Entity").ifPresent(anno -> entityObject.remove(anno));
        entityObject.getAnnotationByName("Table").ifPresent(anno -> entityObject.remove(anno));
    }

    private void initEntity() {
        showMessage("Creating new entity.");
        CompilationUnit cu = this.entity.getCompiledObject();
        if (cu == null) {
            cu = new CompilationUnit(entity.getModuleName());
        } else {
            cu.setPackageDeclaration(entity.getModuleName() + ".entity");
        }
        //Add imports
        cu.getImports().forEach(importedObject -> {
            this.entity.addImport(importedObject.getNameAsString());
        });
        String resourcePackage = this.entity.getModuleName() + ".resource."
                + this.entity.getBaseName() + "Resource";
        this.entity.addImport(resourcePackage);

        for (String pkg : this.entity.getImportedObjects()) {
            cu.addImport(pkg);
        }

        ClassOrInterfaceDeclaration entityObject = entity.getEntityObject();
        if (entityObject == null) {
            entityObject = cu.addClass(entity.getName(), Modifier.PUBLIC);
            entityObject.addExtendedType("DefaultEntity");
            entityObject.addImplementedType("Serializable");
        } else {
            this.cleanEntityAnnotations(entityObject);
        }

        //Add Auditable
        if (this.entity.isAuditable()) {
            this.showMessage("Add => @Audited");
            entityObject.addMarkerAnnotation("Audited");
        }

        //Add Dynamice Update Annotation
        if (this.entity.isDynamicUpdate()) {
            this.showMessage("Add => @DynamicUpdate");
            NormalAnnotationExpr dynamicUpdate = new NormalAnnotationExpr();
            dynamicUpdate.setName("DynamicUpdate");
            dynamicUpdate.addPair("value", "true");
            entityObject.addAnnotation(dynamicUpdate);
        }

        //Add Entity Annotation
        this.showMessage("Add => @Entity");
        NormalAnnotationExpr entityAnnotation = new NormalAnnotationExpr();
        entityAnnotation.setName("Entity");
        entityAnnotation.addPair("name", "\"" + entity.getEntityName() + "\"");
        entityObject.addAnnotation(entityAnnotation);

        //Add Table Annotation
        this.showMessage("Add => @Table");
        NormalAnnotationExpr tableAnnotation = new NormalAnnotationExpr();
        tableAnnotation.setName("Table");
        tableAnnotation.addPair("name", "\"" + entity.getTableName() + "\"");
        entityObject.addAnnotation(tableAnnotation);

        //Add Generated SerialVersionUID
        if (!entityObject.getFieldByName("serialVersionUID").isPresent()) {
            this.generateSerializeField(entityObject);
        }

        this.entity.setCompiledObject(cu);
        this.entity.setEntityObject(entityObject);
        showMessage("Created blank entity");
    }

    private void addSearchFormula(FieldDeclaration searchField) {
        this.showMessage("Creating search property.");

        searchField.getAnnotationByName("Formula").ifPresent(oldFormula -> searchField.remove(oldFormula));

        //Add @Formula
        NormalAnnotationExpr formulaAnnotation = new NormalAnnotationExpr();
        formulaAnnotation.setName("Formula");
        String formula = String.join(", ", entity.getSearchFields());
        formulaAnnotation.addPair("value", "\"concat(" + formula + ")\"");
        searchField.addAnnotation(formulaAnnotation);
    }

    private void createSelfLink(ClassOrInterfaceDeclaration entityObject) {
        MethodDeclaration selfLink = entityObject.addMethod("getSelfLink", Modifier.PUBLIC);
        selfLink.setType("LinkModel");

        String resourceName = this.entity.getBaseName() + "Resource";
        String line1 = "String selfLink = UriBuilder.fromResource(" + resourceName + ".class).build().toString();";
        String line2 = "selfLink += \"/\" + this." + this.entity.getPrimaryProperty().getName() + " + \"/\";";
        String line3 = "return new LinkModel(selfLink);";
        selfLink.createBody().addStatement(line1)
                .addStatement(line2)
                .addStatement(line3);

        selfLink.addSingleMemberAnnotation("JsonGetter", new StringLiteralExpr("&detail_link"));
    }

    private void generateSerializeField(ClassOrInterfaceDeclaration entityObject) {
        this.showMessage("Creating serialVersionUID.");
        long serial = (new Date()).getTime();
        FieldDeclaration serialField = entityObject.addField("long", "serialVersionUID",
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        serialField.getVariable(0).setInitializer(serial + "L");
    }

    private void createMyanmarFontGetterSetter(ClassOrInterfaceDeclaration entityObject, PropertyModel property) {
        String getterName = "getMM" + Globalizer.capitalize(property.getName());
        String setterName = "setMM" + Globalizer.capitalize(property.getName());

        //Remove MMFont Getters
        entityObject.getMethodsByName(getterName).forEach(method -> {
            entityObject.remove(method);
        });

        //Remove MMFont Setters
        entityObject.getMethodsByName(setterName).forEach(method -> {
            entityObject.remove(method);
        });

        if (!property.isJsonIgnore() && property.isAllowMMFont()) {
            //Create getMMProperty
            MethodDeclaration getter = entityObject.addMethod(getterName, Modifier.PUBLIC);
            getter.setType("Object");
            String code = "if (MyanmarFontManager.isMyanmar(this." + property.getName() + ")) {";
            code += "Map<String, String> output = new HashMap<>();";
            code += "output.put(\"zg\", MyanmarFontManager.toZawgyi(this." + property.getName() + "));";
            code += "output.put(\"uni\", this." + property.getName() + ");";
            code += "return output;";
            code += "} else {";
            code += "return this." + property.getName() + ";";
            code += "}";
            getter.createBody().addStatement(code);
            getter.addSingleMemberAnnotation("JsonGetter",
                    new StringLiteralExpr(Globalizer.camelToLowerUnderScore(property.getName())));

            //Create setMMProperty
            MethodDeclaration setter = entityObject.addMethod(setterName, Modifier.PUBLIC);
            setter.addParameter(property.getType(), property.getName());
            code = "if (MyanmarFontManager.isMyanmar(" + property.getName() + ") && MyanmarFontManager.isZawgyi(" + property.getName() + ")) {";
            code += "this." + property.getName() + " = MyanmarFontManager.toUnicode(" + property.getName() + ");";
            code += "} else {";
            code += "this." + property.getName() + " = " + property.getName() + ";";
            code += "}";
            setter.createBody().addStatement(code);
            setter.addSingleMemberAnnotation("JsonSetter",
                    new StringLiteralExpr(Globalizer.camelToLowerUnderScore(property.getName())));
        }
    }

    private void removeAnnotation(ClassOrInterfaceDeclaration entityObject,
            String propertyName, String annotation) {
        //Field clean
        entityObject.getFieldByName(propertyName).ifPresent(field -> {
            field.getAnnotationByName(annotation).ifPresent(anno -> {
                field.remove(anno);
            });
        });

        //Getter clean
        String methodName = "get" + Globalizer.capitalize(propertyName);
        entityObject.getMethodsByName(methodName).forEach(method -> {
            method.getAnnotationByName(annotation).ifPresent(anno -> {
                method.remove(anno);
            });
        });

        //Setter clean
        methodName = "set" + Globalizer.capitalize(propertyName);
        entityObject.getMethodsByName(methodName).forEach(method -> {
            method.getAnnotationByName(annotation).ifPresent(anno -> {
                method.remove(anno);
            });
        });
    }

    private void createProperty(ClassOrInterfaceDeclaration entityObject, PropertyModel property) throws Exception {
        //Clean processable annotations
        this.showMessage("Clean processable annotations.");
        for (String anno : this.processAnnotations) {
            this.removeAnnotation(entityObject, property.getName(), anno);
        }

        //Clean Validate annotations
        this.showMessage("Clean validate annotations.");
        Set<String> validations = ValidationManager.getInstance().getValidations().toMap().keySet();
        for (String anno : validations) {
            this.removeAnnotation(entityObject, property.getName(), anno);
        }

        this.showMessage("Creating property : " + property.getName());

        FieldDeclaration field = property.getFieldObject();
        if (field == null) {
            field = entityObject.addField(property.getType(), property.getName(), Modifier.PRIVATE);
        }

        //Is Primary
        if (property.isPrimary()) {
            field.addMarkerAnnotation("Id");

            if (property.getType().equalsIgnoreCase("int")
                    || property.getType().equalsIgnoreCase("long")) {
                field.addMarkerAnnotation("GeneratedValue");
            }

            if (this.entity.getPrimaryProperty() == null) {
                this.entity.setPrimaryProperty(property);
            }
        }

        //Auditable
        if (!property.isAuditable()) {
            field.addMarkerAnnotation("NotAudited");
        }

        //Create JsonIgnore
        if (property.isJsonIgnore() || property.isAllowMMFont()) {
            field.addMarkerAnnotation("JsonIgnore");
        }

        //Add validations
        for (AnnotationExpr constraint : property.getValidations()) {
            field.addAnnotation(constraint);
        }

        //Create @UIStructure
        this.createUIAnnotation(field, property);

        //Create @Column
        this.createColumnAnnotation(field, property);

        //Check search field
        if (property.isSearchable()) {
            entity.addSearchField(property.getColumnName());
        }

        //Create getter
        String methodName = "get" + Globalizer.capitalize(property.getName());
        if (entityObject.getMethodsByName(methodName).isEmpty()) {
            field.createGetter();
        }

        //Create setter
        methodName = "set" + Globalizer.capitalize(property.getName());
        if (entityObject.getMethodsByName(methodName).isEmpty()) {
            field.createSetter();
        }

        this.createMyanmarFontGetterSetter(entityObject, property);

        this.showMessage("Successfully created : " + property.getName());
    }

    private void createUIAnnotation(FieldDeclaration field, PropertyModel property) {
        NormalAnnotationExpr uiAnnotation = new NormalAnnotationExpr();
        uiAnnotation.setName("UIStructure");
        uiAnnotation.addPair("order", Integer.toString(property.getIndex()));
        uiAnnotation.addPair("label", "\"" + property.getLabel() + "\"");
        uiAnnotation.addPair("inputType", "UIInputType." + property.getInputType());
        uiAnnotation.addPair("hideInGrid", Boolean.toString(property.isHideInGrid()));
        uiAnnotation.addPair("readOnly", Boolean.toString(property.isReadOnly()));

        field.addAnnotation(uiAnnotation);
    }

    private void createColumnAnnotation(FieldDeclaration field, PropertyModel property) {
        NormalAnnotationExpr colAnnotation = new NormalAnnotationExpr();
        colAnnotation.setName("Column");
        colAnnotation.addPair("name", "\"" + property.getColumnName() + "\"");
        colAnnotation.addPair("nullable", Boolean.toString(!property.isRequired()));
        colAnnotation.addPair("columnDefinition", "\"" + property.getColumnDef() + "\"");

        field.addAnnotation(colAnnotation);
    }

    private CompilationUnit createNewResourceFile(String resource) throws IOException {
        this.showMessage("Creating new resource file.");
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(entity.getModuleName() + ".resource");
        cu.addImport("javax.annotation.PostConstruct");
        cu.addImport("javax.ws.rs.Path");
        cu.addImport("org.apache.log4j.Logger");
        cu.addImport("com.sdm.core.resource.RestResource");
        cu.addImport("com.sdm.core.hibernate.dao.RestDAO");
        cu.addImport(entity.getModuleName() + ".dao." + entity.getBaseName() + "DAO");
        cu.addImport(entity.getModuleName() + ".entity." + entity.getName());

        ClassOrInterfaceDeclaration resourceObject = cu.addClass(resource, Modifier.PUBLIC);
        resourceObject.addExtendedType("RestResource<" + entity.getName() + ", " + entity.getPrimaryType() + ">");

        FieldDeclaration loggerField = resourceObject.addField("Logger", "LOG", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        loggerField.getVariable(0).setInitializer("Logger.getLogger(" + entity.getBaseName() + "Resource.class.getName())");

        MethodDeclaration getLogger = resourceObject.addMethod("getLogger", Modifier.PROTECTED);
        getLogger.addMarkerAnnotation("Override");
        getLogger.setType("Logger");
        getLogger.createBody().addStatement("return " + entity.getBaseName() + "Resource.LOG;");

        resourceObject.addField(entity.getBaseName() + "DAO", "mainDAO", Modifier.PRIVATE);
        MethodDeclaration init = resourceObject.addMethod("init", Modifier.PRIVATE);
        init.addMarkerAnnotation("PostConstruct");
        init.createBody().addStatement("mainDAO = new " + entity.getBaseName() + "DAO(getUserId());");

        MethodDeclaration getDAO = resourceObject.addMethod("getDAO", Modifier.PROTECTED);
        getDAO.addMarkerAnnotation("Override");
        getDAO.setType("RestDAO");
        getDAO.createBody().addStatement("return this.mainDAO;");

        this.showMessage("Successfully created " + resource + ".java file.");
        return cu;
    }

    private void writeResource() throws IOException, FileNotFoundException {
        this.showMessage("Checking resource file.");
        String resource = entity.getBaseName() + "Resource";
        String modulePath = entity.getFile().getParentFile().getParent();
        File resourceFile = new File(modulePath + File.separatorChar
                + "resource" + File.separatorChar + resource + ".java");
        CompilationUnit cu;
        if (!resourceFile.exists() && resourceFile.createNewFile()) {
            cu = this.createNewResourceFile(resource);
        } else {
            cu = JavaParser.parse(resourceFile);
        }

        cu.getClassByName(resource).ifPresent(resourceObject -> {
            resourceObject.getAnnotationByName("Path").ifPresent(annotation -> {
                resourceObject.remove(annotation);
            });
            resourceObject.addSingleMemberAnnotation("Path", new StringLiteralExpr(entity.getResourcePath()));
            try {
                Files.write(resourceFile.toPath(), cu.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ex) {
                this.showMessage(ex.getLocalizedMessage());
            }
            this.showMessage("Updated resource path.");
        });

    }

    private void writeDAO() throws IOException {
        this.showMessage("Checking DAO file.");
        String dao = entity.getBaseName() + "DAO";
        String modulePath = entity.getFile().getParentFile().getParent();
        File daoFile = new File(modulePath + File.separatorChar
                + "dao" + File.separatorChar + dao + ".java");
        if (!daoFile.exists() && daoFile.createNewFile()) {
            this.showMessage("Creating new DAO file.");

            CompilationUnit cu = new CompilationUnit();
            cu.setPackageDeclaration(entity.getModuleName() + ".dao");
            cu.addImport("org.hibernate.Session");
            cu.addImport("com.sdm.core.hibernate.dao.RestDAO");
            cu.addImport(entity.getModuleName() + ".entity." + entity.getName());

            ClassOrInterfaceDeclaration daoObject = cu.addClass(dao, Modifier.PUBLIC);
            daoObject.addExtendedType("RestDAO");

            ConstructorDeclaration constUser = daoObject.addConstructor(Modifier.PUBLIC);
            constUser.addParameter(PrimitiveType.intType(), "userId");
            MethodCallExpr call = new MethodCallExpr(null, "super");
            call.addArgument(entity.getName() + ".class.getName()");
            call.addArgument("userId");
            constUser.createBody().addStatement(call);

            ConstructorDeclaration constUserWithSession = daoObject.addConstructor(Modifier.PUBLIC);
            constUserWithSession.addParameter("Session", "session");
            constUserWithSession.addParameter(PrimitiveType.intType(), "userId");
            call = new MethodCallExpr(null, "super");
            call.addArgument("session");
            call.addArgument(entity.getName() + ".class.getName()");
            call.addArgument("userId");
            constUserWithSession.createBody().addStatement(call);

            Files.write(daoFile.toPath(), cu.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            this.showMessage("Successfully created " + dao + ".java file.");
        }
    }

    private void writeHibernate() throws TransformerException {
        // Write mapping in hibernate config file.
        String entityClass = this.entity.getModuleName()
                + ".entity." + this.entity.getName();
        if (this.entity.isMappedWithDB()) {
            HibernateManager.getInstance().addEntity(entityClass);
        } else {
            HibernateManager.getInstance().removeMapping(entityClass);
        }
        HibernateManager.getInstance().writeConfig();
    }

    @Override
    protected Boolean call() throws Exception {
        //Init Entity
        this.initEntity();

        //Clean search fields
        this.entity.setSearchFields(new HashSet<>());

        //Add properties to entity
        ClassOrInterfaceDeclaration entityObject = this.entity.getEntityObject();
        for (PropertyModel property : this.entity.getProperties()) {
            this.createProperty(entityObject, property);
        }

        //Add Search Field 
        if (!entityObject.getFieldByName("search").isPresent()) {
            FieldDeclaration searchField = entityObject.addField(String.class, "search", Modifier.PRIVATE);
            //Add @JsonIgnore
            searchField.addMarkerAnnotation("JsonIgnore");

            //Add @NotAudited
            searchField.addMarkerAnnotation("NotAudited");

            this.addSearchFormula(searchField);
            if (entityObject.getMethodsByName("getSearch").isEmpty()) {
                searchField.createGetter();
            }
            if (entityObject.getMethodsByName("setSearch").isEmpty()) {
                searchField.createSetter();
            }
        } else {
            entityObject.getFieldByName("search").ifPresent(searchField -> this.addSearchFormula(searchField));
        }

        //Add Self Link to entity
        if (entityObject.getMethodsByName("getSelfLink").isEmpty()) {
            this.createSelfLink(entityObject);
        }

        //Write Entity File
        this.showMessage("Writing file " + entity.getFile().getName());
        String code = entity.getCompiledObject().toString();
        Files.write(entity.getFile().toPath(), code.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        //Write DAO File
        this.writeDAO();

        //Write Resource File
        this.writeResource();

        //Write Hibernate mapping
        this.writeHibernate();
        return true;
    }

}
