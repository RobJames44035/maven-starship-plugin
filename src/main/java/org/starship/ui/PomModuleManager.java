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

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class PomModuleManager {

    private final File pomFile;
    private final Map<String, Boolean> moduleStates = new LinkedHashMap<>();

    public PomModuleManager(File pomFile) {
        this.pomFile = pomFile;
    }

    /**
     * Loads current modules and their state (commented = false, uncommented = true) from pom.xml.
     */
    public void loadModules() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);

            NodeList modulesNodes = document.getElementsByTagName("modules");
            if (modulesNodes.getLength() == 0) return;

            Node modulesNode = modulesNodes.item(0);

            moduleStates.clear(); // Clear any previous state

            for (int i = 0; i < modulesNode.getChildNodes().getLength(); i++) {
                Node child = modulesNode.getChildNodes().item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE && "module".equals(child.getNodeName())) {
                    String moduleName = child.getTextContent().trim();
                    moduleStates.put(moduleName, true);
                } else if (child.getNodeType() == Node.COMMENT_NODE) {
                    String commentContent = child.getNodeValue().trim();
                    if (commentContent.startsWith("<module>") && commentContent.endsWith("</module>")) {
                        String moduleName = commentContent.replace("<module>", "")
                                .replace("</module>", "").trim();
                        moduleStates.put(moduleName, false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the modules based on the current state and saves the pom.xml with proper formatting.
     */
    public void saveModules() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile);

            NodeList modulesNodes = document.getElementsByTagName("modules");
            if (modulesNodes.getLength() == 0) return;

            Node modulesNode = modulesNodes.item(0);

            // Clear the existing `<modules>` content
            while (modulesNode.hasChildNodes()) {
                modulesNode.removeChild(modulesNode.getFirstChild());
            }

            // Write back the modules based on the current state
            for (Map.Entry<String, Boolean> module : moduleStates.entrySet()) {
                if (module.getValue()) {
                    // Write uncommented modules
                    Element moduleElement = document.createElement("module");
                    moduleElement.setTextContent(module.getKey());
                    modulesNode.appendChild(moduleElement);
                } else {
                    // Write commented modules
                    Comment commentedModule = document.createComment("<module>" + module.getKey() + "</module>");
                    modulesNode.appendChild(commentedModule);
                }
            }

            // Write the updated document back to file
            saveFormattedDocumentToFile(document, pomFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the XML Document back to file with proper formatting.
     *
     * @param document XML document to write.
     * @param file     Destination file.
     * @throws TransformerException If there's a problem during transformation.
     */
    private void saveFormattedDocumentToFile(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Pretty print the XML with proper indentation
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    /**
     * Helper to update the state of a specific module.
     *
     * @param moduleName Module name to update.
     * @param active     True to uncomment the module, false to comment it.
     */
    public void setModuleState(String moduleName, boolean active) {
        moduleStates.put(moduleName, active);
    }

    /**
     * Gets the state of the modules.
     *
     * @return Map of module names and their states (true = uncommented, false = commented).
     */
    public Map<String, Boolean> getModuleStates() {
        return moduleStates;
    }
}