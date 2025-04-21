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

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class BuildFiascoUtil {

    private static final String FIASCO_BASE_DIR = "StarshipOS/fiasco";
    private static final String FIASCO_SRC_DIR = "src";

    public BuildFiascoUtil(MavenProject ignoredProject) {
    }

    public void buildFiasco(String architecture) {
        try {
            File fiascoDir = getAbsolutePath();
            File srcDir = new File(fiascoDir, FIASCO_SRC_DIR);
            File objDir = new File(fiascoDir, "target/" + architecture);

            validateDirectory(fiascoDir, "Fiasco Base");
            validateDirectory(srcDir, "Fiasco Source");

            runMakeCommand(fiascoDir, architecture);
            copyPrebuiltConfig(objDir, architecture);
            runOldConfig(objDir);
            buildL4ReUserland(objDir);

        } catch (Exception e) {
            throw new IllegalStateException("Fiasco build for architecture '" + architecture + "' failed: " + e.getMessage(), e);
        }
    }

    private void runMakeCommand(File baseDir, String architecture) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("make", "B=target/" + architecture)
                .directory(baseDir)
                .inheritIO();

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Building Fiasco kernel failed with exit code: " + exitCode);
        }
    }

    private void copyPrebuiltConfig(File objDir, String arch) throws IOException {
        String resourceName = "fiasco.globalconfig." + arch;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            //                throw new IOException("Resource not found: " + resourceName);
            File outFile = new File(objDir, "globalconfig.out");
            FileUtils.copyStreamToFile(() -> in, outFile);
        } catch (IOException e) {
            throw new IOException("Failed to copy config for architecture '" + arch + "': " + e.getMessage(), e);
        }
    }

    private void runOldConfig(File objDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("make", "olddefconfig")
                .directory(objDir)
                .inheritIO();

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("make olddefconfig failed with exit code: " + exitCode);
        }
    }

    private void buildL4ReUserland(File objDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("make", "-j" + Runtime.getRuntime().availableProcessors())
                .directory(objDir)
                .inheritIO();

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Building L4Re userland failed in " + objDir.getAbsolutePath() +
                    " with exit code: " + exitCode);
        }
    }

    private File getAbsolutePath() {
        return new File(System.getProperty("user.dir"), BuildFiascoUtil.FIASCO_BASE_DIR);
    }

    private void validateDirectory(File dir, String description) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException(description + " directory does not exist or is invalid: " + dir.getAbsolutePath());
        }
    }
}
