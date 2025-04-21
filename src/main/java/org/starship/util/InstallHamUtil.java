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
import java.io.IOException;

public class InstallHamUtil {

    private final String hamRepoUrl;
    private final String manifestRepoUrl;

    public InstallHamUtil(String hamRepoUrl, String manifestRepoUrl) {
        if (hamRepoUrl == null || hamRepoUrl.isBlank()) {
            throw new IllegalArgumentException("HAM repository URL cannot be null or blank.");
        }
        if (manifestRepoUrl == null || manifestRepoUrl.isBlank()) {
            throw new IllegalArgumentException("Manifest repository URL cannot be null or blank.");
        }

        this.hamRepoUrl = hamRepoUrl;
        this.manifestRepoUrl = manifestRepoUrl;
    }

    public void cloneRepositoriesAndSetupStarshipOS() throws IllegalStateException {
        try {
            File starshipOSDir = createAndNavigateToStarshipOSDirectory();

            cloneRepository(hamRepoUrl, starshipOSDir);
            buildHamTool(new File(starshipOSDir, "ham"));

            executeHamCommands(starshipOSDir);
            verifyRequiredDirectories(starshipOSDir);

            cleanupUnnecessaryDirectories(starshipOSDir);
        } catch (Exception e) {
            String errorMessage = "Failed to clone repositories or set up StarshipOS: " + e.getMessage();
            throw new IllegalStateException(errorMessage, e);
        }
    }

    private File createAndNavigateToStarshipOSDirectory() throws Exception {
        File currentDir = new File(System.getProperty("user.dir"));
        File starshipOSDir = new File(currentDir, "StarshipOS");

        if (!starshipOSDir.exists() && !starshipOSDir.mkdirs()) {
            throw new IOException("Failed to create 'StarshipOS' directory.");
        }

        return starshipOSDir;
    }

    private void cloneRepository(String repoUrl, File workingDirectory) throws Exception {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new IllegalArgumentException("Repository URL cannot be null or blank.");
        }

        ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl, "ham")
                .directory(workingDirectory)
                .inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Command 'git clone' failed for " + "ham" + " with exit code: " + exitCode);
        }
    }

    private void buildHamTool(File hamDir) throws Exception {
        if (!hamDir.exists() || !hamDir.isDirectory()) {
            throw new Exception("'ham' directory does not exist. Cannot build HAM tool.");
        }

        ProcessBuilder builder = new ProcessBuilder("make")
                .directory(hamDir)
                .inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Building HAM tool failed with exit code: " + exitCode);
        }
    }

    private void executeHamCommands(File workingDirectory) throws Exception {
        File hamExecutable = new File(workingDirectory, "ham/ham");

        if (!hamExecutable.exists() || !hamExecutable.canExecute()) {
            throw new IOException("HAM executable not found or not executable: " + hamExecutable.getAbsolutePath());
        }

        executeShellCommand(workingDirectory, hamExecutable.getAbsolutePath(), "init", "-u", manifestRepoUrl);
        executeShellCommand(workingDirectory, hamExecutable.getAbsolutePath(), "sync");
    }

    private void executeShellCommand(File workingDirectory, String... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(workingDirectory)
                .inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Command '" + String.join(" ", command) + "' failed with exit code: " + exitCode);
        }
    }

    private void verifyRequiredDirectories(File starshipOSDir) throws Exception {
        File fiascoDir = new File(starshipOSDir, "fiasco");
        File l4Dir = new File(starshipOSDir, "l4");

        if (!fiascoDir.exists() || !fiascoDir.isDirectory()) {
            throw new Exception("Fiasco directory is missing under 'StarshipOS'.");
        }
        if (!l4Dir.exists() || !l4Dir.isDirectory()) {
            throw new Exception("L4 directory is missing under 'StarshipOS'.");
        }
    }

    private void cleanupUnnecessaryDirectories(File starshipOSDir) throws IOException {
        File hamDir = new File(starshipOSDir, "ham");
        File hamMetadataDir = new File(starshipOSDir, ".ham");

        deleteDirectoryRecursively(hamDir);
        deleteDirectoryRecursively(hamMetadataDir);
    }

    private void deleteDirectoryRecursively(File dir) throws IOException {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
            if (!dir.delete()) {
                throw new IOException("Failed to delete directory: " + dir.getAbsolutePath());
            }
        }
    }
}
