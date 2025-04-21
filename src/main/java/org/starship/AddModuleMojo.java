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

package org.starship;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mojo(name = "add-module")
public class AddModuleMojo extends AbstractMojo {

    @Parameter(property = "module", required = true)
    private String module;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            File baseDir = new File(System.getProperty("user.dir"));
            File moduleDir = new File(baseDir, module);

            if (!moduleDir.exists() && !moduleDir.mkdirs()) {
                throw new MojoExecutionException("Failed to create module directory: " + moduleDir);
            }

            String[] paths = {
                    "src/main/java", "src/main/groovy",
                    "src/test/java", "src/test/groovy"
            };
            for (String p : paths) {
                Files.createDirectories(Paths.get(moduleDir.getAbsolutePath(), p));
            }

            File pomFile = new File(moduleDir, "pom.xml");
            try (FileWriter writer = new FileWriter(pomFile)) {
                writer.write(
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                "    <parent>\n" +
                                "        <groupId>org.starshipos</groupId>\n" +
                                "        <artifactId>starship-parent</artifactId>\n" +
                                "        <version>1.0.0</version>\n" +
                                "    </parent>\n" +
                                "    <artifactId>" + module + "</artifactId>\n" +
                                "    <version>1.0.0</version>\n" +
                                "    <properties>\n" +
                                "        <groovy.version>4.0.18</groovy.version>\n" +
                                "    </properties>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>org.apache.groovy</groupId>\n" +
                                "            <artifactId>groovy-all</artifactId>\n" +
                                "            <version>${groovy.version}</version>\n" +
                                "            <type>pom</type>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>\n"
                );
            }

            File parentPom = new File(baseDir, "pom.xml");
            if (!parentPom.exists()) {
                throw new MojoExecutionException("Parent POM not found: " + parentPom);
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(parentPom);
            doc.getDocumentElement().normalize();

            NodeList modulesList = doc.getElementsByTagName("modules");
            Element modulesElement;
            if (modulesList.getLength() == 0) {
                modulesElement = doc.createElement("modules");
                doc.getDocumentElement().appendChild(modulesElement);
            } else {
                modulesElement = (Element) modulesList.item(0);
            }

            boolean alreadyExists = false;
            NodeList existingModules = modulesElement.getElementsByTagName("module");
            for (int i = 0; i < existingModules.getLength(); i++) {
                if (existingModules.item(i).getTextContent().equals(module)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                Element newModule = doc.createElement("module");
                newModule.setTextContent(module);
                modulesElement.appendChild(newModule);

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(new DOMSource(doc), new StreamResult(parentPom));
            }

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to add module: " + module, e);
        }
    }
}
