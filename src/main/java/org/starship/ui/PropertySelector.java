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

import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Handles the management of the `starship-dev.properties` file.
 * Loads, displays, and saves the properties as checkboxes.
 */
public class PropertySelector extends AbstractFeatureSelector {

    private final File propertiesFile;
    private final Map<String, CheckBox> propertyCheckBoxes = new LinkedHashMap<>();
    private final Map<String, String> defaultProperties;

    public PropertySelector(File propertiesFile, Map<String, String> defaultProperties) {
        this.propertiesFile = propertiesFile;
        this.defaultProperties = defaultProperties;
    }

    @Override
    public void initialize() throws IOException {
        Properties properties = new Properties();

        if (!propertiesFile.exists()) {
            // Create the properties file with default values
            properties.putAll(defaultProperties);
            try (FileWriter writer = new FileWriter(propertiesFile)) {
                properties.store(writer, "Starship Development Default Properties");
            }
        } else {
            // Load the existing properties
            try (FileReader reader = new FileReader(propertiesFile)) {
                properties.load(reader);
            }
        }

        // Create checkboxes based on the loaded properties
        for (String key : properties.stringPropertyNames()) {
            boolean isChecked = Boolean.parseBoolean(properties.getProperty(key));
            CheckBox checkBox = new CheckBox(key);
            checkBox.setSelected(isChecked);
            propertyCheckBoxes.put(key, checkBox);
        }
    }

    @Override
    public VBox getUI() {
        VBox vbox = new VBox(5);
        vbox.setStyle("-fx-padding: 10; -fx-alignment: top-left;");

        // Add all property checkboxes to the VBox
        propertyCheckBoxes.values().forEach(vbox.getChildren()::add);
        return vbox;
    }

    @Override
    public void save() throws IOException {
        Properties properties = new Properties();

        // Save the state of the checkboxes to the properties file
        for (Map.Entry<String, CheckBox> entry : propertyCheckBoxes.entrySet()) {
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue().isSelected()));
        }

        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "Starship Development Updated Properties");
        }
    }
}