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
import org.starship.ui.ModuleSelector;

/**
 * Launches the StarshipOS Module Selector UI
 */
@Mojo(name = "configure", requiresProject = false)
public class ConfigureMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("***********************************************************");
        getLog().info("* Launching StarshipOS Module Selector UI                *");
        getLog().info("***********************************************************");

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
    }
}
