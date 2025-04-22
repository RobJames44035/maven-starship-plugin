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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Handles the management of configuration properties (starship-dev.properties).
 * Displays dynamically-generated checkboxes for properties and architectures.
 */
public class InstallConfigHandler extends AbstractFeatureSelector {

    private final File propertiesFile;
    private final Map<String, String> defaultProperties;

    private final Map<String, Map<String, CheckBox>> propertyCheckboxes = new LinkedHashMap<>();
    private final List<String> architectures = Arrays.asList("x86_64", "ARM");

    private final List<String> orderedProperties = Arrays.asList(
            "installToolchainFlag",
            "installCodebase",
            "buildFiasco",
            "buildJDK",
            "buildL4",
            "runQEMU"
    );

    public InstallConfigHandler(File propertiesFile, Map<String, String> defaultProperties) {
        this.propertiesFile = propertiesFile;
        this.defaultProperties = defaultProperties;
    }

    @Override
    public void initialize() throws IOException {
        Properties properties = new Properties();

        // Load or create default properties
        if (!propertiesFile.exists()) {
            properties.putAll(defaultProperties);
            try (FileWriter writer = new FileWriter(propertiesFile)) {
                properties.store(writer, "Starship Development Default Properties");
            }
        } else {
            try (FileReader reader = new FileReader(propertiesFile)) {
                properties.load(reader);
            }
        }

        // Create checkboxes for each property and its corresponding architectures
        for (String property : orderedProperties) {
            Map<String, CheckBox> archCheckboxes = new LinkedHashMap<>();

            if (isBuildProperty(property)) {
                for (String arch : architectures) {
                    String archKey = property + "." + arch;
                    boolean isChecked = Boolean.parseBoolean(properties.getProperty(archKey, "false"));
                    CheckBox archCheckbox = new CheckBox(arch);
                    archCheckbox.setSelected(isChecked);
                    archCheckboxes.put(arch, archCheckbox);
                }
            }

            // Main property checkbox
            boolean isChecked = Boolean.parseBoolean(properties.getProperty(property, defaultProperties.getOrDefault(property, "false")));
            CheckBox propertyCheckbox = new CheckBox(formatLabel(property));
            propertyCheckbox.setSelected(isChecked);

            archCheckboxes.put(property, propertyCheckbox);
            propertyCheckboxes.put(property, archCheckboxes);

            // Validate the loaded state
            validatePropertyState(property);
        }

        // Update UI dynamically on selection changes
        for (String property : orderedProperties) {
            Map<String, CheckBox> archCheckboxes = propertyCheckboxes.get(property);

            if (archCheckboxes != null) {
                CheckBox propertyCheckbox = archCheckboxes.get(property);

                for (String arch : architectures) {
                    if (archCheckboxes.containsKey(arch)) {
                        CheckBox archCheckbox = archCheckboxes.get(arch);

                        // Update the main checkbox if any architecture checkbox changes
                        archCheckbox.setOnAction(event -> {
                            boolean hasSelectedArch = architectures.stream()
                                    .anyMatch(a -> archCheckboxes.get(a).isSelected());

                            propertyCheckbox.setSelected(hasSelectedArch);
                        });
                    }
                }
            }
        }
    }

    /**
     * Validate the state of a property.
     * Automatically unchecks the main property checkbox if no architectures are selected.
     *
     * @param property The property name (e.g., "buildFiasco").
     */
    private void validatePropertyState(String property) {
        Map<String, CheckBox> archBoxes = propertyCheckboxes.get(property);

        if (archBoxes != null) {
            CheckBox propertyCheckbox = archBoxes.get(property);

            // Check if any architecture checkbox is selected
            boolean hasValidArch = architectures.stream()
                    .anyMatch(arch -> archBoxes.containsKey(arch) && archBoxes.get(arch).isSelected());

            // Uncheck the main property if no architecture is selected
            if (!hasValidArch) {
                propertyCheckbox.setSelected(false);
            }
        }
    }

    @Override
    public VBox getUI() {
        VBox vbox = new VBox(10); // Container for all properties
        vbox.setStyle("-fx-padding: 10; -fx-alignment: top-left;");

        // Create layout for each property and architecture checkboxes
        for (Map.Entry<String, Map<String, CheckBox>> entry : propertyCheckboxes.entrySet()) {
            String property = entry.getKey();
            Map<String, CheckBox> archBoxes = entry.getValue();

            HBox propertyRow = new HBox(10); // Row for one property
            propertyRow.setStyle("-fx-alignment: center-left;");

            // Add the main property checkbox
            CheckBox propertyCheckbox = archBoxes.get(property);
            propertyRow.getChildren().add(propertyCheckbox);

            for (String arch : architectures) {
                if (archBoxes.containsKey(arch)) {
                    propertyRow.getChildren().add(archBoxes.get(arch));
                }
            }

            vbox.getChildren().add(propertyRow);
        }

        return vbox;
    }

    @Override
    public void save() throws IOException {
        validateAllProperties(); // Ensure all properties have valid states before saving

        Properties properties = new Properties();

        // Save main property and architecture checkboxes state
        for (Map.Entry<String, Map<String, CheckBox>> entry : propertyCheckboxes.entrySet()) {
            String property = entry.getKey();
            Map<String, CheckBox> archBoxes = entry.getValue();

            // Save the main property checkbox
            CheckBox propertyCheckbox = archBoxes.get(property);
            properties.setProperty(property, String.valueOf(propertyCheckbox.isSelected()));

            for (String arch : architectures) {
                if (archBoxes.containsKey(arch)) {
                    String archKey = property + "." + arch;
                    properties.setProperty(archKey, String.valueOf(archBoxes.get(arch).isSelected()));
                }
            }
        }

        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "Starship Development Updated Properties");
        }
    }

    /**
     * Validate all properties and ensure no invalid states.
     */
    private void validateAllProperties() {
        for (String property : orderedProperties) {
            validatePropertyState(property);
        }
    }

    private String formatLabel(String key) {
        String formatted = key.replaceAll("([a-z])([A-Z])", "$1 $2").replace("Flag", "").trim();
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }

    private boolean isBuildProperty(String property) {
        return property.startsWith("build");
    }
}