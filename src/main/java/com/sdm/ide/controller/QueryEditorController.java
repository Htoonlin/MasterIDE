/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller;

import com.sdm.ide.component.Callback;
import com.sdm.ide.helper.BoundsPopup;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import static org.reactfx.EventStreams.nonNullValuesOf;
import org.reactfx.Subscription;

/**
 * FXML Controller class
 *
 * @author htoonlin
 */
public class QueryEditorController implements Initializable {

    private final String[] HQL_KEYWORDS = new String[]{
        "all", "any", "and", "as", "asc", "avg", "between", "class",
        "count", "delete", "desc", "dot", "distinct", "elements", "escape",
        "exists", "false", "fetch", "from", "full", "group", "having", "in",
        "indices", "inner", "insert", "into", "is", "join", "left", "like",
        "max", "min", "new", "not", "null", "or", "order", "outer",
        "properties", "right", "select", "set", "some", "sum", "true",
        "update", "versioned", "where", "nulls", "first", "last"
    };

    private final String KEYWORD_PATTERN = "(?i)\\b(" + String.join("|", HQL_KEYWORDS) + ")\\b";
    private final String PAREN_PATTERN = "\\(|\\)";
    private final String BRACE_PATTERN = "\\{|\\}";
    private final String BRACKET_PATTERN = "\\[|\\]";
    private final String SEMICOLON_PATTERN = "\\;";
    private final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private final String PARAM_PATTERN = ":[A-Za-z0-9_]+";

    private final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
            + "|(?<PARAM>" + PARAM_PATTERN + ")"
    );

    private StyleSpans<Collection<String>> highlightNow(String source) {
        Matcher matcher = PATTERN.matcher(source);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass
                    = matcher.group("KEYWORD") != null ? "keyword"
                    : matcher.group("PAREN") != null ? "paren"
                    : matcher.group("BRACE") != null ? "brace"
                    : matcher.group("BRACKET") != null ? "bracket"
                    : matcher.group("SEMICOLON") != null ? "semicolon"
                    : matcher.group("STRING") != null ? "string"
                    : matcher.group("COMMENT") != null ? "comment"
                    : matcher.group("PARAM") != null ? "param"
                    : null;
            /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), source.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @FXML
    private StackPane editorPane;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TextField txtName;

    private VirtualizedScrollPane vsPane;
    private CodeArea codeArea;

    private Callback<Pair> saveHandler;

    public void loadPopup(Stage primaryStage){
        //Start Caret Popup                
        BoundsPopup caretPopup = new BoundsPopup("Hello", primaryStage);
        VBox caretOptions = createPopupOptions(caretPopup, "Show/Hide caret-based popup", "Show/Hide popup even when caret is out of viewport");

        EventStream<Optional<Bounds>> caretBounds = nonNullValuesOf(codeArea.caretBoundsProperty());
        Subscription cBoundsSub = feedVisibilityToLabelText(caretBounds, caretPopup, "Caret");

        double caretXOffset = 0;
        double caretYOffset = 0;

        Subscription caretPopupSub = EventStreams.combine(caretBounds, caretPopup.outsideViewportValues())
                .subscribe(tuple3 -> {
                    Optional<Bounds> opt = tuple3._1;
                    boolean showPopupWhenCaretOutside = tuple3._2;

                    if (opt.isPresent()) {
                        Bounds b = opt.get();
                        caretPopup.setX(b.getMaxX() + caretXOffset);
                        caretPopup.setY(b.getMaxY() + caretYOffset);

                        if (caretPopup.isHiddenTemporarily()) {
                            caretPopup.show(primaryStage);
                            caretPopup.setHideTemporarily(false);
                        }

                    } else if (!showPopupWhenCaretOutside) {
                        caretPopup.hide();
                        caretPopup.setHideTemporarily(true);
                    }
                });

        Subscription caretSubs = caretPopupSub.and(cBoundsSub);
        caretPopup.show(primaryStage);        
        codeArea.moveTo(0);
        codeArea.requestFollowCaret();
    }
    
    public void setQuery(String title, String code) {        
        codeArea.replaceText(0, 0, code);
        txtName.setText(title);
    }

    public void onSave(Callback<Pair> handler) {
        this.saveHandler = handler;
    }

    private void close() {
        Stage stage = (Stage) this.rootPane.getScene().getWindow();
        stage.close();
    }

    private VBox createPopupOptions(BoundsPopup p, String showHideButtonText, String toggleViewportText) {
        Button showOrHidePopup = new Button(showHideButtonText);
        showOrHidePopup.setOnAction(ae -> p.invertVisibility());
        Button toggleOutOfViewportOption = new Button(toggleViewportText);
        toggleOutOfViewportOption.setOnAction(ae -> p.invertViewportOption());
        return new VBox(showOrHidePopup, toggleOutOfViewportOption);
    }

    private Subscription feedVisibilityToLabelText(EventStream<Optional<Bounds>> boundsStream, BoundsPopup popup, String item) {
        return boundsStream
                .map(o -> o.isPresent() ? " is " : " is not ")
                .subscribe(visibilityStatus -> popup.setText(item + visibilityStatus + "within the viewport"));
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    codeArea.setStyleSpans(0, highlightNow(codeArea.getText()));
                });

        vsPane = new VirtualizedScrollPane<>(codeArea);
        this.editorPane.getChildren().add(vsPane);
    }

    @FXML
    private void closeEditor(ActionEvent event) {
        Stage stage = (Stage) this.rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveCode(ActionEvent event) {
        if (this.saveHandler != null) {
            this.saveHandler.call(new Pair(txtName.getText(), codeArea.getText()));
        }
        this.closeEditor(event);
    }

}
