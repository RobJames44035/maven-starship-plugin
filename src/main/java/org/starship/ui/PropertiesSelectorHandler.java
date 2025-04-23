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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class PropertiesSelectorHandler extends AbstractFeatureSelector {

    private final File propertiesFile;
    private final Properties properties = new Properties();
    private final Map<String, CheckBox> checkboxes = new LinkedHashMap<>();
    private final VBox groupedUI = new VBox(10);

    public PropertiesSelectorHandler(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    @Override
    public void initialize() throws IOException {
        if (propertiesFile.exists()) {
            try (FileReader reader = new FileReader(propertiesFile)) {
                properties.load(reader);
            }
        }

        groupedUI.getChildren().clear();

        // Top-level toggles
        addSingle("installToolchainFlag", "Install Toolchain");
        addSingle("installCodebase", "Install Codebase");

        // Grouped toggles with parent + ARM + x86-64
        addGroup("buildFiasco", "Build Fiasco");
        addGroup("buildL4", "Build L4");
        addGroup("buildJDK", "Build OpenJDK");
        addGroup("sayHello", "Say Hello");
    }

    private void addSingle(String key, String label) {
        boolean selected = Boolean.parseBoolean(properties.getProperty(key, "false"));
        CheckBox box = new CheckBox(label);
        box.setSelected(selected);
        checkboxes.put(key, box);
        groupedUI.getChildren().add(box);
    }

    private void addGroup(String baseKey, String label) {
        String armKey = baseKey + ".ARM";
        String x86Key = baseKey + ".x86_64";

        CheckBox parentBox = new CheckBox(label);
        CheckBox armBox = new CheckBox("ARM");
        CheckBox x86Box = new CheckBox("x86-64");

        parentBox.setSelected(Boolean.parseBoolean(properties.getProperty(baseKey, "false")));
        armBox.setSelected(Boolean.parseBoolean(properties.getProperty(armKey, "false")));
        x86Box.setSelected(Boolean.parseBoolean(properties.getProperty(x86Key, "false")));

        // Wire child disabling
        parentBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                armBox.setSelected(false);
                x86Box.setSelected(false);
            }
        });

        // Wire child enabling parent
        Stream.of(armBox, x86Box).forEach(child ->
                child.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal && !parentBox.isSelected()) {
                        parentBox.setSelected(true);
                    }
                })
        );

        checkboxes.put(baseKey, parentBox);
        checkboxes.put(armKey, armBox);
        checkboxes.put(x86Key, x86Box);

        HBox row = new HBox(20, parentBox, armBox, x86Box);
        groupedUI.getChildren().add(row);
    }

    @Override
    public VBox getUI() {
        return groupedUI;
    }

    @Override
    public void save() throws IOException {
        for (Map.Entry<String, CheckBox> entry : checkboxes.entrySet()) {
            properties.setProperty(entry.getKey(), Boolean.toString(entry.getValue().isSelected()));
        }
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "StarshipOS Configuration Properties");
        }
    }
}
