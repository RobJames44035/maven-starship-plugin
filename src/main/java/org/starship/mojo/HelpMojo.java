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

package org.starship.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Displays help information for the Starship Maven Plugin.
 */
@Mojo(name = "help", requiresProject = false)
public class HelpMojo extends AbstractMojo {
    public void execute() {
        getLog().info("StarshipOS Plugin Help:");
        getLog().info("  mvn starship:initialize    - Bootstrap StarshipOS project structure");
        getLog().info("  mvn starship:add-module    - Add a new module to the project");
        getLog().info("  mvn starship:build         - Build a specific StarshipOS component");
        getLog().info("  mvn help:describe          - View plugin details (built-in)");
        getLog().info("");
        getLog().info("  Tip: Use -Darch=arm or -Darch=x86_64 for cross-arch support.");
    }
}