package com.sdm.ide.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.sdm.ide.model.DatabaseModel;
import com.sdm.ide.model.EntityModel;
import com.sdm.ide.model.ModuleModel;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class TemplateManager {

    private final Configuration config;

    public TemplateManager() {
        config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        config.setClassForTemplateLoading(getClass(), "/template/");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void write(String ftl, Map<String, Object> data, Writer writer) throws TemplateNotFoundException,
            MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Template template = config.getTemplate(ftl);
        template.process(data, writer);
    }

    public void writeHibernateConfig(DatabaseModel db, List<ModuleModel> modules, File output) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("db", db);
        data.put("modules", modules);
        try (FileWriter writer = new FileWriter(output)) {
            this.write("hibernate.ftl", data, writer);
        } catch (Exception e) {
            throw e;
        }
    }

    public void writeEntity(EntityModel entity, String moduleDir) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("author", System.getProperty("user.name"));
        data.put("serializeId", (new Random()).nextLong() + "L");
        data.put("entity", entity);

        File output = new File(moduleDir + "/entity/" + entity.getName() + ".java");
        try (FileWriter writer = new FileWriter(output)) {
            this.write("entity.ftl", data, writer);
        } catch (Exception e) {
            throw e;
        }
    }

    public void writeDAO(EntityModel entity, String moduleDir) throws Exception {

        Map<String, Object> data = new HashMap<>();
        data.put("author", System.getProperty("user.name"));
        data.put("entity", entity);

        File output = new File(moduleDir + "/dao/" + entity.getBaseName() + "DAO.java");

        try (FileWriter writer = new FileWriter(output)) {
            this.write("dao.ftl", data, writer);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void writeResource(EntityModel entity, String moduleDir) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("author", System.getProperty("user.name"));
        data.put("entity", entity);

        File output = new File(moduleDir + "/resource/" + entity.getBaseName() + "Resource.java");

        try (FileWriter writer = new FileWriter(output)) {
            this.write("resource.ftl", data, writer);
        } catch (Exception ex) {
            throw ex;
        }
    }
}
