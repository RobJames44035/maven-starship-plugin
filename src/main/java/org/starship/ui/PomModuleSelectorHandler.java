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
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PomModuleSelectorHandler extends AbstractFeatureSelector {

    private final File pomFile;
    private final Map<String, CheckBox> moduleCheckBoxes = new LinkedHashMap<>();
    private Document dom;

    public PomModuleSelectorHandler(File pomFile) {
        this.pomFile = pomFile;
    }

    @Override
    public void initialize() throws Exception {
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
    }

    @Override
    public VBox getUI() {
        VBox vbox = new VBox(5);
        vbox.setStyle("-fx-padding: 10; -fx-alignment: top-left;");
        moduleCheckBoxes.values().forEach(vbox.getChildren()::add);
        return vbox;
    }

    @Override
    public void save() throws Exception {
        NodeList modulesNodes = dom.getElementsByTagName("modules");
        if (modulesNodes.getLength() == 0) return;

        Node modulesNode = modulesNodes.item(0);
        List<Node> toRemove = new ArrayList<>();
        for (int i = 0; i < modulesNode.getChildNodes().getLength(); i++) {
            Node node = modulesNode.getChildNodes().item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.COMMENT_NODE) {
                toRemove.add(node);
            }
        }
        toRemove.forEach(modulesNode::removeChild);

        for (Map.Entry<String, CheckBox> entry : moduleCheckBoxes.entrySet()) {
            String moduleName = entry.getKey();
            boolean enabled = entry.getValue().isSelected();

            if (enabled) {
                Element moduleElem = dom.createElement("module");
                moduleElem.setTextContent(moduleName);
                modulesNode.appendChild(moduleElem);
            } else {
                String comment = "<module>" + moduleName + "</module>";
                Comment commentNode = dom.createComment(comment);
                modulesNode.appendChild(commentNode);
            }
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");

        try (FileWriter writer = new FileWriter(pomFile)) {
            transformer.transform(new DOMSource(dom), new StreamResult(writer));
        }
    }
}
