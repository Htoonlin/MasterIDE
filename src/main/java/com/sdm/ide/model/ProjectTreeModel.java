package com.sdm.ide.model;

import java.io.File;

import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;

public class ProjectTreeModel {

    public enum Type {
        ROOT("/image/open_folder.png"),
        MODULE("/image/module.png"),
        ENTITY("/image/entity.png"),
        SETTING("/image/settings.png"),
        HIBERNATE_SETTING("/image/database.png"),
        LOG4J_SETTING("/image/log4j.png"),
        SYSTEM_SETTING("/image/setting.png");

        private final String filePath;

        Type(String filePath) {
            this.filePath = filePath;
        }

        Image getImage(Dimension2D size) {
            return new Image(getClass().getResourceAsStream(filePath),
                    size.getWidth(), size.getHeight(),
                    true, true);
        }
    };

    private final double DEF_ICON_SIZE = 15;

    private Type type;
    private File file;
    private String label;

    public ProjectTreeModel() {
    }

    public ProjectTreeModel(Type type, File file, String label) {
        this.type = type;
        this.file = file;
        this.label = label;
    }

    public Image getImage() {
        return this.getImage(new Dimension2D(DEF_ICON_SIZE, DEF_ICON_SIZE));
    }

    public Image getImage(Dimension2D size) {
        return this.type.getImage(size);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public String getLabel() {
        return label;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }

}
