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
import org.starship.ui.ModuleSelector;
import org.starship.util.*;

import java.io.File;
import java.io.IOException;


/**
 * Maven Mojo responsible for initializing the StarshipOS development environment.
 * This plugin handles the complete setup process including:
 * - Installing required toolchains for ARM and x86_64
 * - Cloning necessary repositories
 * - Building Fiasco and L4 kernels
 * - Setting up OpenJDK
 * - Creating Maven project structure
 */
@SuppressWarnings({"unused", "ConstantValue"})
@Mojo(name = "initialize", requiresProject = false)
public class InitializeMojo extends AbstractMojo {
    /**
     * Base directory for the StarshipOS project
     */
    private static final File baseDir = new File(System.getProperty("user.dir"), "StarshipOS");
    /**
     * Fixed project name
     */
    private static final String LOCKED_PROJECT_NAME = "StarshipOS";
    /**
     * User's home directory
     */
    @Parameter(defaultValue = "${user.home}", readonly = true)
    private File homeDirectory;
    /**
     * Current Maven project
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    /**
     * Current Maven session
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    /**
     * Plugin manager for build operations
     */
    @Component
    private BuildPluginManager pluginManager;

    /**
     * Executes the initialization process for StarshipOS.
     * This includes setting up toolchains, cloning repositories,
     * building necessary components, and configuring the development environment.
     *
     * @throws MojoExecutionException if any initialization step fails
     */
    @Override
    public void execute() throws MojoExecutionException {
        File projectDir = new File(homeDirectory, LOCKED_PROJECT_NAME);

        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new MojoExecutionException("Failed to create project directory: " + projectDir.getAbsolutePath());
        }

        String hamRepo = "https://github.com/kernkonzept/ham.git";
        String manifestRepo = "https://github.com/kernkonzept/manifest.git";

        try {
            warnAndPrompt();

            boolean installToolchainFlag = true;
            if (installToolchainFlag) {
                getLog().info("*******************************************************");
                getLog().info("  Installing toolchains for ARM and x86_64");
                getLog().info("*******************************************************");

                InstallToolchainUtil toolchainUtil = new InstallToolchainUtil(projectDir);
                toolchainUtil.installToolchain();
            }

            boolean installCodebase = true;
            if (installCodebase) {
                getLog().info("*******************************************************");
                getLog().info("  Cloning Repositories and Setting up StarshipOS");
                getLog().info("*******************************************************");

                InstallHamUtil installHamUtil = new InstallHamUtil(hamRepo, manifestRepo);
                installHamUtil.cloneRepositoriesAndSetupStarshipOS();
                DownloadOpenJdkUtil downloadOpenJdkUtil = new DownloadOpenJdkUtil();
                downloadOpenJdkUtil.cloneOpenJdk("jdk-21-ga");
            }

            boolean buildFiasco = false;
            if (buildFiasco) {
                getLog().info("*******************************************************");
                getLog().info("  Building Fiasco");
                getLog().info("*******************************************************");

                BuildFiascoUtil buildFiascoUtil = new BuildFiascoUtil(project);
                buildFiascoUtil.buildFiasco("x86_64");
                buildFiascoUtil.buildFiasco("arm");
            }
            boolean buildJDK = false;
            if (buildJDK) {
                getLog().info("*******************************************************");
                getLog().info("  Building OpenJDK jdk-21-ga");
                getLog().info("*******************************************************");

                BuildJDKUtil buildJDKUtil = new BuildJDKUtil();
                boolean x86_64 = true;
                if (x86_64) {
                    buildJDKUtil.buildJDK("x86_64");
                }
                boolean arm = true;
                if (arm) {
                    buildJDKUtil.buildJDK("arm");
                }
            }

            boolean buildL4 = false;
            if (buildL4) {
                getLog().info("*******************************************************");
                getLog().info("  Building L4");
                getLog().info("*******************************************************");

                BuildL4Util buildL4Util = new BuildL4Util(project);
                boolean x86_64 = true;
                if (x86_64) {
                    buildL4Util.buildL4("x86_64");
                }
                boolean arm = true;
                if (arm) {
                    buildL4Util.buildL4("arm");
                }
            }

            boolean runQEMU = false;
            if (runQEMU) {
                getLog().info("*******************************************************");
                getLog().info("  Running Hello World Demo in QEMU (x86_64)");
                getLog().info("*******************************************************");

                RunHelloQemuUtil runHelloQemuUtil = new RunHelloQemuUtil();
                new RunHelloQemuUtil();
                runHelloQemuUtil.runHelloDemo("x86_64");
// todo            runHelloQemuUtil.runHelloDemo("arm");
            }


            getLog().info("*******************************************************");
            getLog().info("  Creating Apache Maven project: StarshipOS");
            getLog().info("*******************************************************");

            MavenizeUtil mavenizeUtil = new MavenizeUtil();
            mavenizeUtil.generateModulePoms(baseDir);

            getLog().info("*******************************************************");
            getLog().info("  Cleanup.  Almost done");
            getLog().info("*******************************************************");

            CleanupUtil cleanupUtil = new CleanupUtil();
            cleanupUtil.scrubAndInit();

            File returnedDir = returnToProjectDirectory(projectDir);
            getLog().info("Returned to project directory: " + returnedDir.getAbsolutePath());

            try {
                // JavaFX must be started on a separate thread if not already running
                Thread fxThread = new Thread(() -> {
                    try {
                        ModuleSelector.launch(ModuleSelector.class);
                    } catch (Exception e) {
                        getLog().error("Failed to launch JavaFX UI: " + e.getMessage());
                    }
                });
                fxThread.setDaemon(false);
                fxThread.start();
                fxThread.join();
            } catch (InterruptedException e) {
                throw new MojoExecutionException("JavaFX UI thread was interrupted", e);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🚀 StarshipOS is ready.");
                System.out.println("👉 Run the following to switch to your project:");
                System.out.println("   cd ./StarshipOS");
            }));

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize StarshipOS: " + e.getMessage(), e);
        }
    }

    /**
     * Returns to the project directory, creating it if necessary.
     *
     * @param projectDir the project directory to return to
     * @return the project directory File object
     * @throws IOException if directory creation or access fails
     */
    private File returnToProjectDirectory(File projectDir) throws IOException {
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Failed to access or create project directory: " + projectDir.getAbsolutePath());
        }

        if (!projectDir.setWritable(true)) {
            getLog().warn("Could not set project directory writable: " + projectDir.getAbsolutePath());
        }

        return projectDir;
    }

    /**
     * Displays warning messages about sudo privileges and prompts for user confirmation.
     * Provides information about the initialization process and required permissions.
     */
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
}
