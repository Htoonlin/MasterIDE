package com.sdm.ide.controller;

import com.sdm.Constants;
import com.sdm.IDE;
import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.component.ProgressDialog;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.model.ProjectTreeModel;
import com.sdm.ide.model.ProjectTreeModel.Type;
import com.sdm.ide.task.LoadProjectTask;
import com.sdm.ide.task.NewProjectTask;
import com.sdm.ide.task.PackProjectTask;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController implements Initializable {

    private Stage primaryStage;
    private Node activeNode;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private File projectDirectory;

    @FXML
    private SplitPane mainPane;

    @FXML
    private TreeView<ProjectTreeModel> projectTreeView;

    private void loadNode(Node node) {
        // Clear active node
        if (this.activeNode != null) {
            this.mainPane.getItems().remove(this.activeNode);
            this.activeNode = null;
        }

        // Add node in main split view
        this.mainPane.getItems().add(node);
        this.activeNode = node;
    }

    private void loadEntity(File entityFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EntityManager.fxml"));
            SplitPane entity = (SplitPane) loader.load();
            EntityManagerController controller = loader.getController();
            controller.loadEntity(entityFile);
            this.loadNode(entity);
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    private boolean loadProject() {
        File javaDir = new File(this.projectDirectory.getPath() + Constants.IDE.JAVA_DIR);
        File resDir = new File(this.projectDirectory.getPath() + Constants.IDE.RESOURCE_DIR);
        if (javaDir.exists() && javaDir.isDirectory()
                && resDir.exists() && resDir.isDirectory()) {

            LoadProjectTask task = new LoadProjectTask(this.projectDirectory);
            ProgressDialog<TreeItem<ProjectTreeModel>> dialog = new ProgressDialog<>(task, false);
            dialog.onSucceedHandler(value -> {
                projectTreeView.setRoot(value);
                projectTreeView.refresh();
            });
            dialog.start();
            return true;
        } else {
            AlertDialog.showWarning("Invalid project directory.");
            return false;
        }
    }

    private void loadHibernateSetting() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HibernateSetting.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            HibernateSettingController controller = loader.getController();
            controller.setProjectTree(projectTreeView);
            this.loadNode(root);
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    private void loadLog4jSetting(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Log4JSetting.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            Log4JSettingController controller = loader.getController();
            controller.setFile(file);
            this.loadNode(root);
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    private void loadSetting(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SystemSetting.fxml"));
            AnchorPane root = (AnchorPane) loader.load();
            SystemSettingController controller = loader.getController();
            controller.setFile(file);
            this.loadNode(root);
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

    @FXML
    void treeViewClick(MouseEvent event) {
        if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
            TreeItem<ProjectTreeModel> item = this.projectTreeView.getSelectionModel().getSelectedItem();
            if (item != null) {
                ProjectTreeModel model = item.getValue();
                if (model != null) {
                    switch (model.getType()) {
                        case ENTITY:
                            this.loadEntity(model.getFile());
                            break;
                        case HIBERNATE_SETTING:
                            this.loadHibernateSetting();
                            break;
                        case LOG4J_SETTING:
                            this.loadLog4jSetting(model.getFile());
                            break;
                        case SYSTEM_SETTING:
                            this.loadSetting(model.getFile());
                            break;
                    }

                }
            }
        }
    }

    private void chooseProjectFolder(boolean isNew) {
        String prevDirectory = IDE.getPrefs().get(Constants.IDE.PREV_PROJECT_DIR, System.getProperty("user.home"));
        File validate = new File(prevDirectory);
        if (!validate.exists() || !validate.isDirectory()) {
            prevDirectory = System.getProperty("user.home");
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(prevDirectory));

        File choice = dirChooser.showDialog(primaryStage);

        if (choice != null && choice.isDirectory()) {
            projectDirectory = choice;
            String savePath = ProjectManager.getFilePath(this.projectDirectory);
            if (isNew) {

                NewProjectTask task = new NewProjectTask(this.projectDirectory);
                ProgressDialog<File> dialog = new ProgressDialog<>(task, false);
                dialog.onSucceedHandler(value -> {
                    //Remove downloadFile 
                    Optional<ButtonType> respButton = AlertDialog.showQuestion("Do you want to remove downloaded zip file?");
                    if (respButton.isPresent() && respButton.get().equals(ButtonType.YES)) {
                        try {
                            Files.delete(value.toPath());
                        } catch (IOException ex) {
                            AlertDialog.showException(ex);
                        }
                    }

                    if (this.loadProject()) {
                        IDE.getPrefs().put(Constants.IDE.PREV_PROJECT_DIR, savePath);
                    }
                });
                dialog.start();
            } else if (this.loadProject()) {
                IDE.getPrefs().put(Constants.IDE.PREV_PROJECT_DIR, savePath);
            }
        } else {
            System.out.println("Cancel");
        }
    }

    @FXML
    void newProject(ActionEvent event) {
        this.chooseProjectFolder(true);
    }

    @FXML
    public void openProject(ActionEvent event) {
        this.chooseProjectFolder(false);
    }

    @FXML
    void showDBManager(ActionEvent event) {
        AlertDialog.showWarning("It is underconstruction!");
    }

    @FXML
    void cloneEntity(ActionEvent event) {
        final TreeItem<ProjectTreeModel> item = this.projectTreeView.getSelectionModel().getSelectedItem();
        if (item != null) {
            final ProjectTreeModel model = item.getValue();
            if (model != null && model.getType() == Type.ENTITY) {
                Optional<String> result = AlertDialog.showInput("Enter entity name",
                        new Image(getClass().getResourceAsStream("/image/entity.png"), 28, 28, true, true));
                result.ifPresent((name) -> {
                    if (ProjectManager.validJavaClass(name) && !name.endsWith("Entity")) {
                        File entityFile = new File(model.getFile().getParent() + File.separatorChar + name + "Entity.java");
                        try {
                            //Clone File
                            if (entityFile.createNewFile()) {
                                String source = new String(Files.readAllBytes(model.getFile().toPath()));
                                source = source.replaceAll(model.getLabel(), name);
                                Files.write(entityFile.toPath(), source.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                            } else {
                                AlertDialog.showWarning("Can't create new entity file.");
                            }

                            ProjectTreeModel entityModel = new ProjectTreeModel(Type.ENTITY, entityFile, name + "Entity");
                            TreeItem<ProjectTreeModel> entityTree = new TreeItem<>(entityModel,
                                    new ImageView(entityModel.getImage()));
                            item.getParent().getChildren().add(entityTree);
                            projectTreeView.refresh();

                        } catch (IOException e) {
                            AlertDialog.showException(e);
                        }
                    } else {
                        AlertDialog.showWarning("Invalid entity name <" + name + ">.");
                    }
                });
            } else {
                AlertDialog.showWarning("Invalid selection.");
            }
        }
    }

    @FXML
    void newEntity(ActionEvent event) {
        final TreeItem<ProjectTreeModel> item = this.projectTreeView.getSelectionModel().getSelectedItem();
        if (item != null) {
            final ProjectTreeModel model = item.getValue();
            File entityDir = null;

            if (model == null || model.getType() == Type.ROOT) {
                return;
            } else if (model.getType() == Type.ENTITY) {
                // Get parent module node and Entity Directory.
                entityDir = model.getFile().getParentFile();
            } else if (model.getType() == Type.MODULE) {
                entityDir = new File(model.getFile().getPath() + File.separator + "entity");
            }

            if (entityDir != null && entityDir.isDirectory()) {
                Optional<String> result = AlertDialog.showInput("Enter entity name",
                        new Image(getClass().getResourceAsStream("/image/entity.png"), 28, 28, true, true));
                final String entityPath = entityDir.getPath();
                result.ifPresent((name) -> {
                    if (ProjectManager.validJavaClass(name) && !name.endsWith("Entity")) {
                        File entityFile = new File(entityPath + File.separator + name + "Entity.java");
                        try {
                            if (entityFile.createNewFile()) {
                                ProjectTreeModel entityModel = new ProjectTreeModel(Type.ENTITY, entityFile, name + "Entity");
                                TreeItem<ProjectTreeModel> entityTree = new TreeItem<>(entityModel,
                                        new ImageView(entityModel.getImage()));
                                switch (model.getType()) {
                                    case ENTITY:
                                        item.getParent().getChildren().add(entityTree);
                                        break;
                                    case MODULE:
                                        item.getChildren().add(entityTree);
                                        break;
                                    default:
                                        AlertDialog.showWarning("Invalid selection.");
                                        break;
                                }

                                projectTreeView.refresh();
                            }
                        } catch (IOException e) {
                            AlertDialog.showException(e);
                        }
                    } else {
                        AlertDialog.showWarning("Invalid entity name <" + name + ">.");
                    }
                });

            }
        }
    }

    @FXML
    void newModule(ActionEvent event) {
        Optional<String> result = AlertDialog.showInput("Enter module name",
                new Image(getClass().getResourceAsStream("/image/module.png"), 28, 28, true, true));

        result.ifPresent((name) -> {
            String baseDir = this.projectDirectory.getPath() + Constants.IDE.JAVA_DIR;
            if (name.matches("[a-z]+")) {
                File moduleDir = new File(baseDir + File.separator + name);
                if (moduleDir.exists()) {
                    Optional<ButtonType> buttonTypes = AlertDialog
                            .showQuestion(moduleDir + " is already exsists. Do you wanna overwrite?");
                    if (buttonTypes.get() != ButtonType.YES) {
                        return;
                    }
                }

                ProjectManager.createModule(moduleDir);

                ProjectTreeModel model = new ProjectTreeModel(Type.MODULE, moduleDir, name);
                TreeItem<ProjectTreeModel> moduleTree = new TreeItem<>(model, new ImageView(model.getImage()));
                int index = projectTreeView.getRoot().getChildren().size() - 1;
                this.projectTreeView.getRoot().getChildren().add(index, moduleTree);
                this.projectTreeView.refresh();
            } else {
                AlertDialog.showWarning("Invalid module name <" + name + ">.");
            }
        });
    }

    @FXML
    void remove(ActionEvent event) {
        TreeItem<ProjectTreeModel> item = this.projectTreeView.getSelectionModel().getSelectedItem();
        if (item != null) {
            ProjectTreeModel model = item.getValue();
            if (model != null && (model.getType().equals(Type.ENTITY) || model.getType().equals(Type.MODULE))) {
                Optional<ButtonType> result = AlertDialog
                        .showQuestion("Are you sure to remove " + model.getLabel() + " ?");
                if (result.get() == ButtonType.YES) {
                    try {
                        ProjectManager.removeModule(model.getFile());
                        item.getParent().getChildren().remove(item);
                        this.projectTreeView.refresh();
                    } catch (IOException e) {
                        AlertDialog.showException(e);
                    }
                }
            } else {
                AlertDialog.showWarning("It is not allowed to remove.");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private boolean isMaven(File mvnFile) {
        return mvnFile.exists() && mvnFile.isFile() && mvnFile.getName().startsWith("mvn");
    }

    @FXML
    private void packProject(ActionEvent event) {
        final String mvnDirectory = IDE.getPrefs().get(Constants.IDE.MVN_DIR, "");
        File mvnFile = new File(mvnDirectory);
        if (!isMaven(mvnFile)) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(projectDirectory);
            mvnFile = fileChooser.showOpenDialog(primaryStage);
            IDE.getPrefs().put(Constants.IDE.MVN_DIR, mvnFile.getPath());
        }

        if (isMaven(mvnFile)) {
            PackProjectTask task = new PackProjectTask(projectDirectory, mvnFile);
            ProgressDialog<String> dialog = new ProgressDialog<>(task, true);
            dialog.onSucceedHandler(value -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(value);
                clipboard.setContent(content);
                AlertDialog.showInfo("Copied path : " + value);
            });
            dialog.start();
        }
    }
}
