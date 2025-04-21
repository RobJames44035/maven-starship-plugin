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

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
@Mojo(name = "initialize", requiresProject = false)
public class InitializeMojo extends AbstractMojo {
    private static final File baseDir = new File(System.getProperty("user.dir"), "StarshipOS");
    private static final String LOCKED_PROJECT_NAME = "StarshipOS";
    @Parameter(defaultValue = "${user.home}", readonly = true)
    private File homeDirectory;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Component
    private BuildPluginManager pluginManager;

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

            getLog().info("*******************************************************");
            getLog().info("  Installing toolchains for ARM and x86_64");
            getLog().info("*******************************************************");

            InstallToolchainUtil toolchainUtil = new InstallToolchainUtil(projectDir);
            toolchainUtil.installToolchain();

            getLog().info("*******************************************************");
            getLog().info("  Cloning Repositories and Setting up StarshipOS");
            getLog().info("*******************************************************");

            InstallHamUtil installHamUtil = new InstallHamUtil(hamRepo, manifestRepo);
            installHamUtil.cloneRepositoriesAndSetupStarshipOS();

            getLog().info("*******************************************************");
            getLog().info("  Building Fiasco");
            getLog().info("*******************************************************");

            BuildFiascoUtil buildFiascoUtil = new BuildFiascoUtil(project);
            buildFiascoUtil.buildFiasco("x86_64");
            buildFiascoUtil.buildFiasco("arm");

            getLog().info("*******************************************************");
            getLog().info("  Building L4");
            getLog().info("*******************************************************");

            BuildL4Util buildL4Util = new BuildL4Util(project);
            buildL4Util.buildL4("x86_64");
            buildL4Util.buildL4("arm");

            getLog().info("*******************************************************");
            getLog().info("  Running Hello World Demo in QEMU (x86_64)");
            getLog().info("*******************************************************");

            RunHelloQemuUtil runHelloQemuUtil = new RunHelloQemuUtil();
            new RunHelloQemuUtil();
            runHelloQemuUtil.runHelloDemo("x86_64");
// todo            runHelloQemuUtil.runHelloDemo("arm");
            getLog().info("*******************************************************");
            getLog().info("  Installing OpenJDK jdk-21-ga");
            getLog().info("*******************************************************");

            DownloadOpenJdkUtil downloadOpenJdkUtil = new DownloadOpenJdkUtil();
            downloadOpenJdkUtil.cloneOpenJdk("jdk-21-ga");

            getLog().info("*******************************************************");
            getLog().info("  Building OpenJDK jdk-21-ga");
            getLog().info("*******************************************************");

            BuildJDKUtil buildJDKUtil = new BuildJDKUtil();
            buildJDKUtil.buildJDK("x86_64");
            buildJDKUtil.buildJDK("arm");


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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("🚀 StarshipOS is ready.");
                System.out.println("👉 Run the following to switch to your project:");
                System.out.println("   cd ./StarshipOS");
            }));

        } catch (Exception e) {
            throw new MojoExecutionException("Failed to initialize StarshipOS: " + e.getMessage(), e);
        }
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

//        Thread promptThread = new Thread(() -> {
//            try {
//                System.in.read(); // Waits for Enter
//            } catch (IOException ignored) {}
//        });
//
//        promptThread.setDaemon(true);
//        promptThread.start();
//
//        try {
//            promptThread.join(5000); // wait up to 5 seconds
//        } catch (InterruptedException ignored) {}
    }

}
