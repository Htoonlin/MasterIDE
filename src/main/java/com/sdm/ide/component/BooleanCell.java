package com.sdm.ide.component;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

public class BooleanCell<M> extends TableCell<M, Boolean> {

    private CheckBox checkbox;

    public BooleanCell() {
        checkbox = new CheckBox();
        checkbox.setDisable(true);
        checkbox.selectedProperty().addListener((obs, newValue, oldValue) -> {
            if (isEditing()) {
                commitEdit(newValue == null ? false : newValue);
            }
        });
        this.setGraphic(checkbox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setAlignment(Pos.CENTER);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (isEmpty()) {
            return;
        }
        checkbox.setDisable(false);
        checkbox.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        checkbox.setDisable(true);
    }

    @Override
    public void commitEdit(Boolean value) {
        super.commitEdit(value);
        checkbox.setDisable(true);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty()) {
            checkbox.setVisible(true);
            checkbox.setSelected(item);
        } else {
            checkbox.setVisible(false);
        }
    }
}
