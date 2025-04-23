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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.starship.util.*;

import java.io.*;
import java.util.Properties;

@SuppressWarnings("unused")
@Mojo(name = "initialize", requiresProject = false)
public class InitializeMojo extends AbstractMojo {

    private static final File baseDir = new File(System.getProperty("user.dir"));
    private static final String hamRepo = "https://github.com/kernkonzept/ham.git";
    private static final String manifestRepo = "https://github.com/kernkonzept/manifest.git";

    @Component
    private BuildPluginManager pluginManager;
    @Parameter(defaultValue = "${project}", readonly = true, required = false)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = false)
    private MavenSession session;
    private boolean installToolchainFlag;
    private boolean installCodebase;
    private boolean buildFiasco;
    private boolean buildFiasco_ARM;
    private boolean buildFiasco_x86_64;
    private boolean buildL4;
    private boolean buildL4_ARM;
    private boolean buildL4_x86_64;
    private boolean buildJDK;
    private boolean buildJDK_ARM;
    private boolean buildJDK_x86_64;
    private boolean runQEMU;
    private boolean runQEMU_ARM;
    private boolean runQEMU_x86_64;

    @Override
    public void execute() throws MojoExecutionException {
        File projectDir = baseDir;
        System.out.println("Current directory: " + projectDir.getAbsolutePath());
        System.out.println("Current baseDir: " + baseDir);
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new MojoExecutionException("Failed to create project directory: " + projectDir.getAbsolutePath());
        }

        try {
            processPropertiesFile();

            if (installToolchainFlag) {
                getLog().info("*******************************************************");
                getLog().info("  Installing toolchains for ARM and x86_64");
                getLog().info("*******************************************************");
                new InstallToolchainUtil(projectDir).installToolchain();
            }

            if (installCodebase) {
                getLog().info("*******************************************************");
                getLog().info("  Cloning Repositories and Setting up StarshipOS");
                getLog().info("*******************************************************");
                new InstallHamUtil(hamRepo, manifestRepo).cloneRepositoriesAndSetupStarshipOS();
                new DownloadOpenJdkUtil().cloneOpenJdk("jdk-21-ga");
            }

            if (buildFiasco) {
                getLog().info("*******************************************************");
                getLog().info("  Building Fiasco");
                getLog().info("*******************************************************");
                BuildFiascoUtil util = new BuildFiascoUtil();
                if (buildFiasco_x86_64) util.buildFiasco("x86_64");
                if (buildFiasco_ARM) util.buildFiasco("arm");
            }

            if (buildL4) {
                getLog().info("*******************************************************");
                getLog().info("  Building L4");
                getLog().info("*******************************************************");
                BuildL4Util util = new BuildL4Util();
                if (buildL4_x86_64) util.buildL4("x86_64");
                if (buildL4_ARM) util.buildL4("arm");
            }

            if (buildJDK) {
                getLog().info("*******************************************************");
                getLog().info("  Building OpenJDK jdk-21-ga");
                getLog().info("*******************************************************");
                BuildJDKUtil util = new BuildJDKUtil();
                if (buildJDK_x86_64) util.buildJDK("x86_64");
                if (buildJDK_ARM) util.buildJDK("arm");
            }

            if (runQEMU) {
                getLog().info("*******************************************************");
                getLog().info("  Running Hello World Demo in QEMU");
                getLog().info("*******************************************************");
                RunHelloQemuUtil util = new RunHelloQemuUtil();
                if (runQEMU_x86_64) util.runHelloDemo("x86_64");
                if (runQEMU_ARM) util.runHelloDemo("arm");
            }

            getLog().info("*******************************************************");
            getLog().info("  Creating Apache Maven project: StarshipOS");
            getLog().info("*******************************************************");
            new MavenizeUtil().generateModulePoms(baseDir);

            getLog().info("*******************************************************");
            getLog().info("  Cleanup. Almost done");
            getLog().info("*******************************************************");
            new CleanupUtil().scrubAndInit();

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize StarshipOS: " + e.getMessage(), e);
        }
    }

    public void processPropertiesFile() throws IOException {
        File targetDirectory = new File(System.getProperty("user.dir"), "StarshipOS/.starship");

        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("starship-dev.properties")) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource 'starship-dev.properties' not found.");
            }
            properties.load(inputStream);
        }

        installToolchainFlag = Boolean.parseBoolean(properties.getProperty("installToolchainFlag", "false"));
        installCodebase = Boolean.parseBoolean(properties.getProperty("installCodebase", "false"));
        buildFiasco = Boolean.parseBoolean(properties.getProperty("buildFiasco", "false"));
        buildFiasco_ARM = Boolean.parseBoolean(properties.getProperty("buildFiasco.ARM", "false"));
        buildFiasco_x86_64 = Boolean.parseBoolean(properties.getProperty("buildFiasco.x86_64", "false"));
        buildL4 = Boolean.parseBoolean(properties.getProperty("buildL4", "false"));
        buildL4_ARM = Boolean.parseBoolean(properties.getProperty("buildL4.ARM", "false"));
        buildL4_x86_64 = Boolean.parseBoolean(properties.getProperty("buildL4.x86_64", "false"));
        buildJDK = Boolean.parseBoolean(properties.getProperty("buildJDK", "false"));
        buildJDK_ARM = Boolean.parseBoolean(properties.getProperty("buildJDK.ARM", "false"));
        buildJDK_x86_64 = Boolean.parseBoolean(properties.getProperty("buildJDK.x86_64", "false"));
        runQEMU = Boolean.parseBoolean(properties.getProperty("runQEMU", "false"));
        runQEMU_ARM = Boolean.parseBoolean(properties.getProperty("runQEMU.ARM", "false"));
        runQEMU_x86_64 = Boolean.parseBoolean(properties.getProperty("runQEMU.x86_64", "false"));

        if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new IOException("Failed to create directory: " + targetDirectory.getAbsolutePath());
        }

        File propertiesFile = new File(targetDirectory, "starship-dev.properties");
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            properties.store(writer, "Starship Development Updated Properties");
        }
    }

    public void warnAndPrompt() {
        getLog().warn("===============================================================");
        getLog().warn("        ⚠️  Some Operations May Require Sudo Privileges ⚠️");
        getLog().warn("===============================================================");
        getLog().warn("This process may request your sudo password when setting up ");
        getLog().warn("toolchains, installing dependencies, or performing privileged tasks.");
        getLog().warn("");
        getLog().warn("        Do NOT run: sudo mvn starship:initialize");
        getLog().warn("        Maven should remain under your user account.");
        getLog().warn("");
        getLog().warn("        Press ENTER to continue or Ctrl+C to abort...");
        getLog().warn("        (Continuing automatically in 5 seconds)");
        getLog().warn("===============================================================");
    }

    private File returnToProjectDirectory(File projectDir) throws IOException {
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Failed to access or create project directory: " + projectDir.getAbsolutePath());
        }

        if (!projectDir.setWritable(true)) {
            getLog().warn("Could not set project directory writable: " + projectDir.getAbsolutePath());
        }

        return projectDir;
    }
}
