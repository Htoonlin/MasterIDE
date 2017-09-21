package com.sdm.ide.helper;

import java.io.File;
import java.io.IOException;

import com.sdm.Constants;

public class ProjectManager {

    public static boolean validTableName(String name) {
        return name.matches("tbl_[a-z_]+");
    }

    public static boolean validJavaClass(String name) {
        return name.matches("[A-Z][a-zA-Z0-9]*");
    }

    public static boolean validJavaProperty(String name) {
        return name.matches("[a-z][a-zA-Z0-9]*");
    }

    public static boolean validColumnName(String name) {
        return name.matches("[a-z][a-zA-Z0-9]*");
    }

    public static boolean validJavaPackage(String name) {
        return name.matches("^([a-z][a-z0-9]*(\\.){0,1})*([A-Z][a-zA-Z0-9]*){0,1}");
    }

    public static String getFilePath(File file) {
        return file.getPath().replace(File.separatorChar, '/');
    }

    public static String getEntityClass(String filePath) {
        return filePath.replaceAll(".*/java/", "").replaceAll("\\.java", "").replaceAll("/", ".");
    }

    public static void createModule(File moduleDir) {
        if (!moduleDir.exists()) {
            moduleDir.mkdirs();
        }

        if (!ProjectManager.isModule(moduleDir)) {
            // Create default dir
            for (String dirName : Constants.IDE.MODULE_DIRS) {
                File dir = new File(moduleDir.getPath() + File.separator + dirName);
                dir.mkdirs();
            }
        }
    }

    public static void removeModule(File moduleFile) throws IOException {
        if (moduleFile.isDirectory()) {
            for (String dirName : Constants.IDE.MODULE_DIRS) {
                File dir = new File(moduleFile.getPath() + File.separator + dirName);
                for (File file : dir.listFiles()) {
                    file.delete();
                }
                dir.delete();
            }
        }

        moduleFile.delete();
    }

    public static boolean isModule(File module) {
        for (String dirName : Constants.IDE.MODULE_DIRS) {
            File dir = new File(module.getPath() + File.separator + dirName);
            if (!dir.exists() || !dir.isDirectory()) {
                return false;
            }
        }

        return true;
    }
}
