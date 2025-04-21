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

public class BuildL4Util {

    private static final String L4_BASE_DIR = "StarshipOS/l4";

    public BuildL4Util(MavenProject ignoredProject) {
    }

    public void buildL4(String architecture) {
        try {
            File l4Dir = getAbsolutePath();
            File objDir = new File(l4Dir, "target/" + architecture);

            validateDirectory(l4Dir);

            setupBuild(l4Dir, architecture);
            copyPrebuiltConfig(l4Dir, architecture);
//            runOldconfig(l4Dir);
            buildL4Re(objDir, architecture);
        } catch (Exception e) {
            throw new IllegalStateException("L4Re build for architecture '" + architecture + "' failed: " + e.getMessage(), e);
        }
    }

    private void buildL4Re(File baseDir, String architecture) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("make")
                .directory(baseDir)
                .inheritIO();

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("make B=target/" + architecture + " failed with exit code: " + exitCode);
        }
    }

    private void copyPrebuiltConfig(File objDir, String arch) throws IOException {
        String resourceName = "l4.config." + arch;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            File outFile = new File(objDir, ".config");
            FileUtils.copyStreamToFile(() -> in, outFile);
        } catch (IOException e) {
            throw new IOException("Failed to copy config for architecture '" + arch + "': " + e.getMessage(), e);
        }
    }

    private File getAbsolutePath() {
        return new File(System.getProperty("user.dir"), BuildL4Util.L4_BASE_DIR);
    }

    private void validateDirectory(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("L4Re Base" + " directory does not exist or is invalid: " + dir.getAbsolutePath());
        }
    }

    private void setupBuild(File objDir, String architecture) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("make", "B=target/" + architecture)
                .directory(objDir)
                .inheritIO();

        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("make olddefconfig failed with exit code: " + exitCode);
        }
    }
}
