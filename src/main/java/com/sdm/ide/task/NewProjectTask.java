/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author htoonlin
 */
public class NewProjectTask extends Task<File> {

    private static final String GITHUB_URL = "https://github.com/Htoonlin/MasterAPI/archive/master.zip";
    private final File projectDir;

    public NewProjectTask(File rootDir) {
        this.projectDir = rootDir;
    }

    private void showMessage(String message) {
        try {
            updateMessage(message);
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            updateMessage(message);
        }
    }

    @Override
    protected File call() throws Exception {
        updateMessage("Downloading from github.");
        File downloadFile = new File(projectDir.getPath() + File.separatorChar + "MasterAPI.zip");
        FileUtils.copyURLToFile(new URL(GITHUB_URL), downloadFile);
        showMessage("Download successful.");
        this.extractZipFile(downloadFile);
        return downloadFile;
    }

    private void extractZipFile(File sourceFile) throws IOException {
        FileInputStream fileInput = new FileInputStream(sourceFile);

        //buffer for read and write data to file
        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(fileInput);
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String[] fileNames = ze.getName().split("/", 2);
            String fileName = "";
            if (fileNames.length == 2) {
                fileName += fileNames[1];
            }

            File newFile = new File(projectDir.getPath() + File.separator + fileName);
            this.updateMessage(fileName);

            //create directories for sub directories in zip
            if (ze.isDirectory()) {
                newFile.mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }

            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();

        showMessage("Successfully extracted project.");
    }

}
