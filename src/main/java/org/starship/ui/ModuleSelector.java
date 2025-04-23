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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.maven.plugin.AbstractMojo;

import java.io.File;

@SuppressWarnings("ALL")
public class ModuleSelector extends Application {

    private final File pomFile = new File("pom.xml");
    private final File propertiesFile = new File(".starship/starship-dev.properties");

    private final PomModuleSelectorHandler pomModuleHandler = new PomModuleSelectorHandler(pomFile);
    private final PropertiesSelectorHandler propertiesHandler = new PropertiesSelectorHandler(propertiesFile);

    public static void main(String[] args) {
        launch(args);
    }

    public static void setMojo(AbstractMojo mojo) {
        // future mojo injection if needed
    }

    @Override
    public void start(Stage primaryStage) {
        VBox mainVBox = new VBox(10);
        mainVBox.setStyle("-fx-padding: 10;");

        VBox leftPanel = new VBox(10);
        leftPanel.setStyle("-fx-padding: 10; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: rgba(255, 255, 255, 0.8);");

        Label leftTitle = new Label("POM Modules");
        leftTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        ScrollPane leftScrollPane = new ScrollPane();
        VBox leftContent = new VBox(10);

        try {
            pomModuleHandler.initialize();
            leftContent.getChildren().addAll(pomModuleHandler.getUI().getChildren());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing POM modules: " + e.getMessage());
        }

        leftScrollPane.setContent(leftContent);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setStyle("-fx-background: transparent; -fx-padding: 10;");
        leftPanel.getChildren().addAll(leftTitle, leftScrollPane);

        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-padding: 10; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: rgba(255, 255, 255, 0.8);");

        Label rightTitle = new Label("Configuration Properties");
        rightTitle.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333;");

        ScrollPane rightScrollPane = new ScrollPane();
        VBox rightContent = new VBox(10);

        try {
            propertiesHandler.initialize();
            rightContent.getChildren().addAll(propertiesHandler.getUI().getChildren());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing Configuration Properties: " + e.getMessage());
        }

        rightScrollPane.setContent(rightContent);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background: transparent; -fx-padding: 10;");
        rightPanel.getChildren().addAll(rightTitle, rightScrollPane);

        HBox contentHBox = new HBox(20);
        contentHBox.getChildren().addAll(leftPanel, rightPanel);
        contentHBox.setStyle("-fx-padding: 10;");

        HBox buttonRow = new ButtonRow(pomModuleHandler, propertiesHandler, primaryStage);

        mainVBox.getChildren().addAll(contentHBox, buttonRow);

        Scene scene = new Scene(mainVBox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("StarshipOS Feature Selector");

        primaryStage.sizeToScene();
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setOnShown(event -> {
            primaryStage.centerOnScreen();
        });

        primaryStage.setResizable(true);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.toFront();
        primaryStage.requestFocus();
        primaryStage.show();
    }
}
