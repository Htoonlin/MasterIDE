/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.task;

import com.sdm.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.concurrent.Task;

/**
 *
 * @author Htoonlin
 */
public class PackProjectTask extends Task<String>{
   
    private final Pattern IS_WAR_PATH = Pattern.compile("(Building war:)(\\s)+(.*)\\.war");
    private final File projectDir;
    private final File mvnDir;
    
    public PackProjectTask(File projectDir, File mvnDir){
        this.projectDir = projectDir;
        this.mvnDir = mvnDir;
    }
    
    @Override
    protected String call() throws Exception {
        String command = mvnDir.getPath() + " " + Constants.IDE.MVN_PACK_COMMAND;
        Process proc = Runtime.getRuntime().exec(command, null, projectDir);
        InputStream istr = proc.getInputStream();
        String result = projectDir.getPath() + File.pathSeparator + "target";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(istr))) {
            String message;
            while ((message = br.readLine()) != null) {
                updateMessage(message);
                Matcher matcher = IS_WAR_PATH.matcher(message);
                if(matcher.find()){
                    result = matcher.group().substring(14);
                }
            }
            proc.waitFor();
        }
        return result;
    }
    
}
