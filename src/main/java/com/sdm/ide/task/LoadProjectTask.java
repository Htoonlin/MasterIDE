package com.sdm.ide.task;

import com.sdm.Constants;
import com.sdm.ide.helper.HibernateManager;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.model.ProjectTreeModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class LoadProjectTask extends Task<TreeItem<ProjectTreeModel>> {

    private final File rootDirectory;
    private final File javaDirectory;
    private final File resourceDirectory;

    public LoadProjectTask(File rootDir) {
        super();
        this.rootDirectory = rootDir;
        this.javaDirectory = new File(rootDir + Constants.IDE.JAVA_DIR);
        this.resourceDirectory = new File(rootDir + Constants.IDE.RESOURCE_DIR);
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
    protected TreeItem<ProjectTreeModel> call() throws Exception {
        try {
            this.updateMessage("Start module loading.");
            return loadModule(this.javaDirectory);
        } catch (Exception ex) {
            this.updateMessage(ex.getMessage());
            throw ex;
        }
    }

    private TreeItem<ProjectTreeModel> loadModule(File directory) throws Exception {
        ProjectTreeModel model = new ProjectTreeModel(ProjectTreeModel.Type.ROOT,
                this.rootDirectory, this.rootDirectory.getName());
        TreeItem<ProjectTreeModel> root = new TreeItem<>(model, new ImageView(model.getImage()));
        root.setExpanded(true);
        this.showMessage("Reading project directory.");
        for (File module : directory.listFiles()) {
            if (module.isDirectory()) {
                this.updateMessage("Found " + module.getName() + " directory.");
                if (ProjectManager.isModule(module)) {
                    root.getChildren().add(loadEntity(module.getName(), module));
                }
            }
        }
        showMessage("Finished module analysis.");

        showMessage("Loading setting files.");
        root.getChildren().add(this.loadSettingFiles());
        showMessage("Loaded setting files.");
        return root;
    }

    private TreeItem<ProjectTreeModel> loadEntity(String name, File module) {
        ProjectTreeModel model = new ProjectTreeModel(ProjectTreeModel.Type.MODULE, module, name);
        TreeItem<ProjectTreeModel> moduleTree = new TreeItem<>(model, new ImageView(model.getImage()));
        moduleTree.setExpanded(true);
        showMessage("Reading " + module.getName() + " directory");
        File entityDir = new File(module.getPath() + File.separator + "entity");
        for (File entity : entityDir.listFiles()) {
            if (entity.isFile() && entity.getName().endsWith(".java")) {
                String entityName = entity.getName().replaceAll(".java", "");
                showMessage("Load " + entityName + " entity.");
                ProjectTreeModel entityModel = new ProjectTreeModel(ProjectTreeModel.Type.ENTITY, entity, entityName);
                TreeItem<ProjectTreeModel> entityTree = new TreeItem<>(entityModel,
                        new ImageView(entityModel.getImage()));
                moduleTree.getChildren().add(entityTree);

            }
        }
        return moduleTree;
    }

    private TreeItem<ProjectTreeModel> loadSetting(File settingFile, ProjectTreeModel.Type type, String label) throws IOException {
        this.showMessage("Loading " + settingFile.getName() + ".");
        if (!settingFile.exists()) {
            showMessage("There is no " + settingFile.getName() + ".");
            File sourceFile = new File(this.resourceDirectory.getPath() + File.separator + "example." + settingFile.getName());
            Files.copy(sourceFile.toPath(), settingFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            showMessage("Cloned : " + sourceFile.getName() + " => " + settingFile.getName());
        }

        ProjectTreeModel model = new ProjectTreeModel(type, settingFile, label);
        return new TreeItem<>(model, new ImageView(model.getImage()));
    }

    private TreeItem<ProjectTreeModel> loadSettingFiles() throws Exception {
        ProjectTreeModel model = new ProjectTreeModel(ProjectTreeModel.Type.SETTING,
                this.resourceDirectory, "Settings");
        TreeItem<ProjectTreeModel> moduleTree = new TreeItem<>(model, new ImageView(model.getImage()));
        moduleTree.setExpanded(true);

        File hibernateFile = new File(this.resourceDirectory.getPath() + File.separator + "hibernate.cfg.xml");
        TreeItem<ProjectTreeModel> hibernateTree = this.loadSetting(hibernateFile, ProjectTreeModel.Type.HIBERNATE_SETTING, "Database");
        HibernateManager.getInstance().load(hibernateFile);
        moduleTree.getChildren().add(hibernateTree);

        File log4jFile = new File(this.resourceDirectory.getPath() + File.separator + "log4j.properties");
        TreeItem<ProjectTreeModel> log4jTree = this.loadSetting(log4jFile, ProjectTreeModel.Type.LOG4J_SETTING, "Logging");
        moduleTree.getChildren().add(log4jTree);

        File settingFile = new File(this.resourceDirectory.getPath() + File.separator + "setting.properties");
        TreeItem<ProjectTreeModel> settingTree = this.loadSetting(settingFile, ProjectTreeModel.Type.SYSTEM_SETTING, "System");
        moduleTree.getChildren().add(settingTree);

        return moduleTree;
    }
}
