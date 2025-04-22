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

package org.starship.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


/**
 * Utility class for generating Maven POM files for StarshipOS modules.
 * This class provides functionality to create a multi-module Maven project structure
 * with predefined configurations for StarshipOS components.
 */
public class MavenizeUtil {

    private static final String STARSHIPOS_GROUP_ID = "org.starshipos";
    private static final String VERSION = "1.0.0-SNAPSHOT";

    /**
     * Generates Maven POM files for StarshipOS modules in the specified base directory.
     * Creates module directories if they don't exist and generates both module-specific POMs
     * and a parent POM file with proper Maven configuration.
     *
     * @param baseDir the base directory where module directories and POM files will be created
     * @throws IOException if there are issues creating directories or writing POM files
     */
    public void generateModulePoms(File baseDir) throws IOException {
        List<String> modules = List.of("fiasco", "l4", "openjdk");

        for (String module : modules) {
            File moduleDir = new File(baseDir, module);
            if (!moduleDir.exists() && !moduleDir.mkdirs()) {
                throw new IOException("Failed to create module directory: " + moduleDir.getAbsolutePath());
            }

            File pomFile = new File(moduleDir, "pom.xml");
            try (FileWriter writer = new FileWriter(pomFile)) {
                writer.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "  <parent>\n" +
                        "    <groupId>" + STARSHIPOS_GROUP_ID + "</groupId>\n" +
                        "    <artifactId>starship-parent</artifactId>\n" +
                        "    <version>" + VERSION + "</version>\n" +
                        "    <relativePath>../pom.xml</relativePath>\n" +
                        "  </parent>\n" +
                        "  <artifactId>" + module + "</artifactId>\n" +
                        "  <version>" + VERSION + "</version>\n" +
                        "  <build>\n" +
                        "    <plugins>\n" +
                        "      <plugin>\n" +
                        "        <groupId>" + STARSHIPOS_GROUP_ID + "</groupId>\n" +
                        "        <artifactId>starship-maven-plugin</artifactId>\n" +
                        "        <version>" + VERSION + "</version>\n" +
                        "        <executions>\n" +
                        "          <execution>\n" +
                        "            <id>build</id>\n" +
                        "            <goals><goal>build</goal></goals>\n" +
                        "            <phase>compile</phase>\n" +
                        "            <configuration>\n" +
                        "              <module>" + module + "</module>\n" +
                        "              <arch>x86_64</arch>\n" +
                        "            </configuration>\n" +
                        "          </execution>\n" +
                        "        </executions>\n" +
                        "      </plugin>\n" +
                        "    </plugins>\n" +
                        "  </build>\n" +
                        "</project>\n");
            }
        }

        File parentPom = new File(baseDir, "pom.xml");
        try (FileWriter writer = new FileWriter(parentPom)) {
            writer.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "  <modelVersion>4.0.0</modelVersion>\n" +
                    "  <groupId>" + STARSHIPOS_GROUP_ID + "</groupId>\n" +
                    "  <artifactId>starship-parent</artifactId>\n" +
                    "  <version>" + VERSION + "</version>\n" +
                    "  <packaging>pom</packaging>\n" +
                    "  <modules>\n" +
                    "    <module>fiasco</module>\n" +
                    "    <module>l4</module>\n" +
                    "    <module>openjdk</module>\n" +
                    "  </modules>\n" +
                    "</project>\n");
        }
    }
}
