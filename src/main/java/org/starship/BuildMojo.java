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
import org.apache.maven.project.MavenProject;
import org.starship.util.BuildFiascoUtil;
import org.starship.util.BuildJDKUtil;
import org.starship.util.BuildL4Util;


/**
 * Maven plugin mojo responsible for building different modules of the StarshipOS project.
 * Supports building Fiasco microkernel, L4 environment, and OpenJDK components.
 */
@Mojo(name = "build")
public class BuildMojo extends AbstractMojo {

    /**
     * The module to build. Valid values are "fiasco", "l4", or "openjdk".
     */
    @Parameter(required = true)
    private String module;

    /**
     * Target architecture for the build. Defaults to "x86_64".
     */
    @Parameter(defaultValue = "x86_64")
    private String arch;

    /**
     * The Maven project instance.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Executes the build process for the specified module and architecture.
     *
     * @throws MojoExecutionException if the build process fails or if an unknown module is specified
     */
    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("[StarshipOS] Executing build for module: " + module + ", architecture: " + arch);

        try {
            if ("fiasco".equalsIgnoreCase(module)) {
                new BuildFiascoUtil(project).buildFiasco(arch);
            } else if ("l4".equalsIgnoreCase(module)) {
                new BuildL4Util(project).buildL4(arch);
            } else if ("openjdk".equalsIgnoreCase(module)) {
                new BuildJDKUtil().buildJDK(arch);
            } else {
                throw new MojoExecutionException("Unknown module: " + module);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to build module '" + module + "' for arch '" + arch + "': " + e.getMessage(), e);
        }
    }
}
