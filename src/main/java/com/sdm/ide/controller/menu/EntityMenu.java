/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.ide.controller.menu;

import com.sdm.ide.component.AlertDialog;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author htoonlin
 */
public class EntityMenu extends ContextMenu {

    public EntityMenu() {
        super();

        //New Entity
        this.addMenuItem("New Entity", "/image/entity.png", (event) -> {
            AlertDialog.showWarning("it is underconstruction.");
        });

        //Clone Entity
        this.addMenuItem("Clone Entity", "/image/clone.png", (event) -> {
            AlertDialog.showWarning("it is underconstruction.");
        });

        //Remove Entity
        this.addMenuItem("Remove Entity", "/image/remove.png", (event) -> {
            AlertDialog.showWarning("it is underconstruction.");
        });
    }

    public void addMenuItem(String text, String image, EventHandler<ActionEvent> handler) {
        Image menuImage = new Image(getClass().getResourceAsStream(image),
                15, 15, true, true);
        MenuItem item = new MenuItem(text, new ImageView(menuImage));
        item.setOnAction(handler);
        this.getItems().add(item);
    }
}
