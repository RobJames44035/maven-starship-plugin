/*
 * Copyright (c) 2025 R. A.  and contributors..
 * /This file is part of StarshipOS, an experimental operating system.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the specific
 *  language governing permissions and limitations under the License.
 *
 */

package org.starship.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Creates a button row containing "Save" and "Cancel" buttons.
 * Handles the saving of both POM module states and installation configuration.
 */
@SuppressWarnings("CallToPrintStackTrace")
public class ButtonRow extends HBox {

    public ButtonRow(AbstractFeatureSelector pomModuleHandler, AbstractFeatureSelector installConfigHandler, Stage primaryStage) {
        super(10); // Spacing between buttons
        this.setStyle("-fx-padding: 10;");

        // Save button
        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            try {
                pomModuleHandler.save();
                installConfigHandler.save();
                primaryStage.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Cancel button
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> primaryStage.close());

        // Add buttons to the HBox
        this.getChildren().addAll(saveButton, cancelButton);
    }
}