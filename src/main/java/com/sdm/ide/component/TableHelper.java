package com.sdm.ide.component;

import java.lang.reflect.Field;

import com.sdm.ide.component.annotation.FXColumn;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableHelper {

    public static <M> void generateColumns(Class<M> model, TableView<M> tableView) {
        tableView.getColumns().clear();
        for (Field field : model.getDeclaredFields()) {
            if (field.isAnnotationPresent(FXColumn.class)) {
                FXColumn fxColumn = field.getAnnotation(FXColumn.class);
                String fieldName = field.getName();
                TableColumn<M, ?> column = new TableColumn<>();
                if (fxColumn.label().length() > 0) {
                    column.setText(fxColumn.label());
                } else {
                    column.setText(fieldName);
                }
                column.setVisible(fxColumn.visible());
                column.setEditable(fxColumn.editable());
                column.setSortable(fxColumn.sortable());
                column.setSortType(SortType.ASCENDING);
                column.setPrefWidth(fxColumn.width());

                column.setCellValueFactory(new PropertyValueFactory<>(fieldName));
                if (BooleanProperty.class.isAssignableFrom(field.getType())) {
                    column.setCellFactory((col) -> new BooleanCell());
                }
                tableView.getColumns().add(column);
            }
        }
    }
}
