/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.sdm.core.Globalizer;
import com.sdm.ide.helper.TypeManager;
import com.sdm.ide.helper.ValidationManager;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.PropertyModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;
import org.json.JSONObject;

/**
 *
 * @author htoonlin
 */
public class ParseEntityTask extends Task<EntityModel> {

    private static final Pattern ENTITY_NAME_PATTERN = Pattern.compile("\\w+Entity");

    private final File javaFile;
    private final EntityModel entity;
    private final Set<String> validations;

    public ParseEntityTask(File javaFile) throws Exception {
        super();
        this.javaFile = javaFile;
        this.validations = ValidationManager.getInstance().getValidations().keySet();
        updateMessage("Init entity.");
        this.entity = new EntityModel(javaFile);
    }

    private void showMessage(String message) {
        try {
            updateMessage(message);
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            updateMessage(message);
        }
    }

    @Override
    protected EntityModel call() throws Exception {
        showMessage("Init Java Parser.");
        CompilationUnit cu = JavaParser.parse(javaFile);
        this.entity.setCompiledObject(cu);
        showMessage("Parsed entity file.");

        //Load Imports
        cu.getImports().forEach(importedObject -> {
            this.entity.addImport(importedObject.getNameAsString());
        });

        //Load Package
        cu.getPackageDeclaration().ifPresent(pkg -> {
            String module = pkg.getNameAsString().replaceAll("\\.entity$", "");
            this.entity.setModuleName(module);
            showMessage("Loaded module : " + module);
        });

        //Load Entity
        cu.getClassByName(this.entity.getName()).ifPresent(entityObject -> {
            showMessage("Found " + entityObject.getNameAsString());
            this.entity.setName(entityObject.getNameAsString());
            this.entity.setEntityObject(entityObject);
            this.loadEntityInfo(entityObject);
            showMessage("Loaded entity info.");
        });

        //Load Resource
        this.loadResourceInfo();
        showMessage("Finished!");
        return this.entity;
    }

    private void loadEntityInfo(ClassOrInterfaceDeclaration entityObject) {
        //Load Comment
        entityObject.getJavadoc().ifPresent(comment -> {
            this.entity.setDescription(comment.getDescription().toText());
        });

        //Load Auditable
        if (entityObject.isAnnotationPresent("Audited")) {
            showMessage("Auditable : true");
            this.entity.setAuditable(true);
        }

        //Load DynamicUpdate
        entityObject.getAnnotationByName("DynamicUpdate").ifPresent(annotation -> {
            if (annotation instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr dynamicUpdate = (NormalAnnotationExpr) annotation;
                dynamicUpdate.getPairs().forEach(pair -> {
                    if (pair.getNameAsString().equalsIgnoreCase("value")) {
                        boolean is = Boolean.parseBoolean(pair.getValue().toString());
                        showMessage("DynamicUpdate : " + is);
                        this.entity.setDynamicUpdate(is);
                    }
                });
            } else {
                this.entity.setDynamicUpdate(true);
            }
        });

        //Load Entity
        entityObject.getAnnotationByName("Entity").ifPresent(annotation -> {
            if (annotation instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr entityAnno = (NormalAnnotationExpr) annotation;
                entityAnno.getPairs().forEach(pair -> {
                    if (pair.getNameAsString().equals("name")) {
                        String name = pair.getValue().toString().replaceAll("\"", "");
                        showMessage("Entity name : " + name);
                        this.entity.setEntityName(name);
                    } else if (pair.getNameAsString().equals("dynamicUpdate")) {
                        boolean is = Boolean.parseBoolean(pair.getValue().toString());
                        this.entity.setDynamicUpdate(is);
                    }
                });
            }
        });

        //Load Table
        entityObject.getAnnotationByName("Table").ifPresent(tableAnno -> {
            tableAnno.getChildNodesByType(MemberValuePair.class).forEach(pair -> {
                if (pair.getNameAsString().equals("name")) {
                    String name = pair.getValue().toString().replaceAll("\"", "");
                    showMessage("Table name : " + name);
                    this.entity.setTableName(name);
                }
            });
        });

        //Load NamedQueries
        entityObject.getAnnotationByName("NamedQueries").ifPresent(annotation -> {
            if (annotation instanceof SingleMemberAnnotationExpr) {
                SingleMemberAnnotationExpr queries = (SingleMemberAnnotationExpr) annotation;
                queries.getMemberValue().getChildNodesByType(NormalAnnotationExpr.class).forEach(namedQuery -> {
                    loadNamedQuery(namedQuery);
                });
            } else if (annotation instanceof NormalAnnotationExpr) {
                NormalAnnotationExpr queries = (NormalAnnotationExpr) annotation;
                queries.getPairs().forEach(pair -> {
                    if (pair.getNameAsString().equals("value")) {
                        pair.getValue().getChildNodesByType(NormalAnnotationExpr.class).forEach(namedQuery -> {
                            loadNamedQuery(namedQuery);
                        });
                    }
                });
            }
        });

        //Load NamedQuery 
        entityObject.getAnnotationByName("NamedQuery").ifPresent(annotation -> loadNamedQuery((NormalAnnotationExpr) annotation));

        //Load Search Field
        entityObject.getFieldByName("search").ifPresent(field -> {
            field.getAnnotationByName("Formula").ifPresent(annotation -> {
                if (annotation instanceof SingleMemberAnnotationExpr) {
                    SingleMemberAnnotationExpr formula = (SingleMemberAnnotationExpr) annotation;
                    this.loadSearchFields(formula.getMemberValue().toString().replaceAll("\"", ""));
                } else if (annotation instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr formula = (NormalAnnotationExpr) annotation;
                    formula.getPairs().forEach(pair -> {
                        if (pair.getNameAsString().equals("value")) {
                            this.loadSearchFields(pair.getValue().toString().replaceAll("\"", ""));
                        }
                    });
                }
            });
        });

        //Load fields
        entityObject.getFields().forEach(field -> {
            // Check properties must have exists UIStructure || Column annotation.
            boolean hasUI = field.isAnnotationPresent("UIStructure");
            boolean hasColumn = field.isAnnotationPresent("Column");
            if (hasUI || hasColumn) {
                String fieldName = field.getVariable(0).getNameAsString();
                showMessage("Found a property : " + fieldName);
                try {
                    this.loadFieldInfo(field);
                } catch (IOException ex) {
                    showMessage(ex.getLocalizedMessage());
                }
                showMessage("Loaded property : " + fieldName);
            }
        });

        //Load Methods
        entityObject.getMethods().forEach(method -> {
            // Check method name is property getter|setter.
            String propertyName = method.getNameAsString().replaceAll("(get|set)", "");
            PropertyModel property = this.entity.findProperty(propertyName);
            if (property != null) {
                if (propertyName.startsWith("get")) {
                    property.setGetter(method);
                } else if (propertyName.startsWith("setter")) {
                    property.setSetter(method);
                }
                this.propertyAnalysis(property, method);
            }
        });
    }

    private void loadNamedQuery(NormalAnnotationExpr annotation) {
        String name = "";
        String query = "";
        for (MemberValuePair pair : annotation.getPairs()) {
            if (pair.getNameAsString().equals("name")) {
                name = pair.getValue().toString();
            } else if (pair.getNameAsString().equals("query")) {
                query = pair.getValue().toString();
            }
        }

        if (name.length() > 2 && query.length() > 2) {
            this.showMessage("Loading query " + name);
            this.entity.addNamedQuery(name.substring(1, name.length() - 1),
                    query.substring(1, query.length() - 1));
        }
    }

    private void loadSearchFields(String formula) {
        Matcher matcher = Pattern.compile("\\(.*\\)").matcher(formula);
        if (matcher.find()) {
            String code = matcher.group().substring(1);
            code = code.substring(0, code.length() - 1);
            String[] columns = code.trim().split(",");
            for (String col : columns) {
                this.entity.addSearchField(col.trim());
            }
        }
    }

    private void loadResourceInfo() throws FileNotFoundException {
        showMessage("Preparing resource file");
        String moduleDir = this.javaFile.getParent().replaceAll("entity", "resource");
        String resourceName = this.javaFile.getName().replaceAll("Entity\\.java", "Resource");
        File resourceFile = new File(moduleDir + "/" + resourceName + ".java");
        if (resourceFile.exists() && resourceFile.isFile()) {
            CompilationUnit cu = JavaParser.parse(resourceFile);
            cu.getClassByName(resourceName).ifPresent(resourceObject -> {
                resourceObject.getAnnotationByName("Path").ifPresent(annotation -> {
                    //Check Annotation Type
                    if (annotation instanceof SingleMemberAnnotationExpr) {
                        SingleMemberAnnotationExpr pathAnno = (SingleMemberAnnotationExpr) annotation;
                        this.entity.setResourcePath(pathAnno.getMemberValue().toString().replaceAll("\"", ""));
                    } else if (annotation instanceof NormalAnnotationExpr) {
                        NormalAnnotationExpr pathAnno = (NormalAnnotationExpr) annotation;
                        pathAnno.getPairs().forEach(pair -> {
                            if (pair.getNameAsString().equalsIgnoreCase("value")) {
                                this.entity.setResourcePath(pair.getValue().toString().replaceAll("\"", ""));
                            }
                        });
                    }
                    showMessage("Resource path : " + this.entity.getResourcePath());
                });
            });
        } else {
            showMessage("There is no resource file.");
        }
    }

    private void loadFieldInfo(FieldDeclaration field) throws IOException {
        showMessage("Init property model.");
        PropertyModel property = new PropertyModel();
        //Load property info
        property.setName(field.getVariable(0).getNameAsString());
        property.setType(field.getElementType().asString());

        //Load Comment
        field.getJavadoc().ifPresent(comment -> {
            property.setDescription(comment.getDescription().toText());
        });

        //Init default property values
        property.setAuditable(true);
        property.setJsonIgnore(false);
        property.setSearchable(false);
        property.setRequired(false);

        JSONObject json = TypeManager.getInstance().getLinkType(property.getType());
        property.setInputType(json.getString("input"));
        property.setColumnDef(json.getString("db"));

        //Analysis property now
        this.propertyAnalysis(property, field);

        //Validate searchFields
        if (this.entity.getSearchFields() != null && property.getColumnName() != null
                && this.entity.getSearchFields().contains(property.getColumnName().trim())) {
            property.setSearchable(true);
        }

        //Set Primary Property in Entity
        if (property.isPrimary()) {
            this.entity.setPrimaryProperty(property);
        }

        property.setFieldObject(field);
        this.entity.addProperty(property);
    }

    /**
     * It will look for getMMProperty() and setMMProperty()
     *
     * @param entityObject
     * @param propertyName
     * @return allowMMFont?
     */
    private boolean allowMMFont(String propertyName) {
        String getterName = "getMM" + Globalizer.capitalize(propertyName);
        String setterName = "setMM" + Globalizer.capitalize(propertyName);

        ClassOrInterfaceDeclaration entityObject = this.entity.getEntityObject();

        return (!entityObject.getMethodsByName(getterName).isEmpty()
                && !entityObject.getMethodsByName(setterName).isEmpty());
    }

    private void propertyAnalysis(PropertyModel property, NodeWithAnnotations node) {
        this.showMessage("Analysis " + property.getName());

        //Check MMFont
        property.setAllowMMFont(this.allowMMFont(property.getName()));

        //Load Primary Info
        if (node.isAnnotationPresent("Id") || node.isAnnotationPresent("GeneratedValue")) {
            property.setPrimary(true);
        }

        if (node.isAnnotationPresent("JsonIgnore") && !property.isAllowMMFont()) {
            property.setJsonIgnore(true);
        }

        if (node.isAnnotationPresent("NotAudited")) {
            property.setAuditable(false);
        }

        for (String constraint : this.validations) {
            node.getAnnotationByName(constraint).ifPresent(validation -> {
                property.addValidation((AnnotationExpr) validation);
            });
        }

        node.getAnnotationByName("UIStructure").ifPresent(annotation -> {
            this.uiAnalysis(property, (NormalAnnotationExpr) annotation);
        });

        node.getAnnotationByName("Column").ifPresent(annotation -> {
            this.columnAnalysis(property, (NormalAnnotationExpr) annotation);
        });

        //Check Relation
        this.relationAnalysis(property, node);
    }

    private void relationAnalysis(PropertyModel property, NodeWithAnnotations node) {
        //Check Relation
        for (PropertyModel.Relation type : PropertyModel.Relation.values()) {
            node.getAnnotationByName(type.toString()).ifPresent(relation -> {
                property.setRelationAnnotation((AnnotationExpr) relation);
                property.setRelationSource(getSourceOfRelation(property));
            });
        }

        //Check JoinColumn
        node.getAnnotationByName("JoinColumn").ifPresent(annotation -> {
            property.setColumnDef("");
            this.columnAnalysis(property, (NormalAnnotationExpr) annotation);
            property.setJoinAnnotaion((NormalAnnotationExpr) annotation);
        });

        //Check JoinTable
        node.getAnnotationByName("JoinTable").ifPresent(annotation -> {
            property.setColumnDef("");
            property.setJoinAnnotaion((NormalAnnotationExpr) annotation);
        });
    }

    private String getSourceOfRelation(PropertyModel property) {
        String entityName = property.getType();
        Matcher matcher = ENTITY_NAME_PATTERN.matcher(property.getType());
        if (matcher.find()) {
            entityName = matcher.group();
            for (String entityPkg : this.entity.getImportedObjects()) {
                if (entityPkg.endsWith(entityName)) {
                    entityName = entityPkg;
                    break;
                }
            }
        }
        return entityName;
    }

    private void uiAnalysis(PropertyModel property, NormalAnnotationExpr annotation) {
        annotation.getPairs().forEach(pair -> {
            if (pair.getNameAsString().equalsIgnoreCase("inputType")) {
                property.setInputType(pair.getValue().toString().replaceAll(".*\\.", ""));
            } else if (pair.getNameAsString().equalsIgnoreCase("label")) {
                property.setLabel(pair.getValue().toString().replaceAll("\"", ""));
            } else if (pair.getNameAsString().equalsIgnoreCase("hideInGrid")) {
                boolean is = Boolean.parseBoolean(pair.getValue().toString());
                property.setHideInGrid(is);
            } else if (pair.getNameAsString().equalsIgnoreCase("readOnly")) {
                boolean is = Boolean.parseBoolean(pair.getValue().toString());
                property.setReadOnly(is);
            } else if (pair.getNameAsString().equalsIgnoreCase("order")) {
                int index = Integer.parseInt(pair.getValue().toString());
                property.setIndex(index);
            }
        });
    }

    private void columnAnalysis(PropertyModel property, NormalAnnotationExpr annotation) {
        annotation.getPairs().forEach(pair -> {
            if (pair.getNameAsString().equalsIgnoreCase("name")) {
                property.setColumnName(pair.getValue().toString().replaceAll("\"", ""));
            } else if (pair.getNameAsString().equalsIgnoreCase("columnDefinition")) {
                property.setColumnDef(pair.getValue().toString().replaceAll("\"", ""));
            } else if (pair.getNameAsString().equalsIgnoreCase("nullable")) {
                boolean is = !Boolean.parseBoolean(pair.getValue().toString());
                property.setRequired(is);
            }
        });
    }
}
