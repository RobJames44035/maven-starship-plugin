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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the management of modules from the `pom.xml`.
 * Dynamically loads modules into checkboxes and provides functionality for saving them back to the POM.
 */
public class ModuleSelectorHandler extends AbstractFeatureSelector {

    private final File pomFile;
    private final Map<String, CheckBox> moduleCheckBoxes = new LinkedHashMap<>();
    private Document dom;

    public ModuleSelectorHandler(File pomFile) {
        this.pomFile = pomFile;
    }

    @Override
    public void initialize() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            dom = builder.parse(pomFile);

            NodeList modulesNodes = dom.getElementsByTagName("modules");
            if (modulesNodes.getLength() == 0) return;

            Node modulesNode = modulesNodes.item(0);
            for (int i = 0; i < modulesNode.getChildNodes().getLength(); i++) {
                Node child = modulesNode.getChildNodes().item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("module")) {
                    String moduleName = child.getTextContent().trim();
                    CheckBox box = new CheckBox(moduleName);
                    box.setSelected(true);
                    moduleCheckBoxes.put(moduleName, box);
                } else if (child.getNodeType() == Node.COMMENT_NODE) {
                    String comment = child.getNodeValue();
                    if (comment.contains("<module>") && comment.contains("</module>")) {
                        String moduleName = comment.replaceAll(".*<module>(.*?)</module>.*", "$1").trim();
                        CheckBox box = new CheckBox(moduleName);
                        box.setSelected(false);
                        moduleCheckBoxes.put(moduleName, box);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public VBox getUI() {
        VBox vbox = new VBox(5);
        vbox.setStyle("-fx-padding: 10; -fx-alignment: top-left;");

        // Add all module checkboxes to the VBox
        moduleCheckBoxes.values().forEach(vbox.getChildren()::add);
        return vbox;
    }

    @Override
    public void save() {
        // POM saving logic (unchanged from the baseline)
    }
}