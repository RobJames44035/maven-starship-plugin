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

package org.starship.util.ui;

import org.apache.maven.plugin.AbstractMojo;
import org.starship.ui.ModuleSelector;
import org.starship.util.AbstractUtil;

public class UiRunnerUtil extends AbstractUtil {

    public UiRunnerUtil(AbstractMojo mojo) {
        super(mojo);
    }

    public void runUi(AbstractMojo mojo) throws Exception {
        info("Starting JavaFX UI...");

        // Ensure JavaFX runtime is available before attempting to launch the UI
        if (!isJavaFxRuntimeAvailable()) {
            error("JavaFX runtime components are missing. Please ensure that JavaFX is properly configured.");
            throw new IllegalStateException("JavaFX runtime is missing. Ensure that javafx.* dependencies are included and properly configured.");
        }

        // Launch JavaFX in a separate thread
        Thread fxThread = new Thread(() -> {
            try {
                ModuleSelector.setMojo(mojo); // Inject the mojo instance
                ModuleSelector.launch(ModuleSelector.class); // Launch the JavaFX application
            } catch (Exception e) {
                error("Failed to launch JavaFX UI: " + e.getMessage());
            }
        });

        fxThread.setDaemon(false); // Ensure thread remains active
        fxThread.start();
        fxThread.join(); // Wait for the thread to complete
    }

    /**
     * Checks whether the JavaFX runtime (javafx.* libraries) is available in the classpath at runtime.
     *
     * @return true if JavaFX is available, otherwise false
     */
    private boolean isJavaFxRuntimeAvailable() {
        try {
            // Attempt to load a core JavaFX class to verify runtime availability
            Class.forName("javafx.application.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false; // JavaFX is not available
        }
    }
}