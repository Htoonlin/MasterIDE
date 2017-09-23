/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.AlertDialog;
import com.sdm.ide.helper.ProjectManager;
import com.sdm.ide.helper.SyntaxHighlighting;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * FXML Controller class
 *
 * @author htoonlin
 */
public class CodeEditorController implements Initializable {

    @FXML
    private StackPane editorPane;

    private CodeArea codeArea;

    private File javaFile;
    @FXML
    private Label lblTitle;
    @FXML
    private AnchorPane rootPane;

    private String openedText;

    public void setFile(File javaFile) throws IOException {
        this.javaFile = javaFile;
        this.openedText = new String(Files.readAllBytes(javaFile.toPath()));
        codeArea.replaceText(0, 0, openedText);

        lblTitle.setText(ProjectManager.getClassNameWithPackage(javaFile.getPath()));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, SyntaxHighlighting.highlightNow(codeArea.getText()));
                });

        this.editorPane.getChildren().add(new VirtualizedScrollPane<>(codeArea));
    }

    @FXML
    private void closeEditor(ActionEvent event) {
        if (!this.openedText.equals(codeArea.getText())) {
            Optional<ButtonType> result = AlertDialog.showQuestion("Source code is modified. Do you want to save?");
            if (result.isPresent() && result.get().equals(ButtonType.YES)) {
                saveSourceCode(event);
            }
        }
        Stage stage = (Stage) this.rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveSourceCode(ActionEvent event) {
        try {
            Files.write(javaFile.toPath(), codeArea.getText().getBytes(), StandardOpenOption.WRITE);
            this.openedText = codeArea.getText();
            AlertDialog.showInfo("Save successful.");
        } catch (IOException ex) {
            AlertDialog.showException(ex);
        }
    }

}
