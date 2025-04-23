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

        Thread fxThread = new Thread(() -> {
            try {
                ModuleSelector.setMojo(mojo); // inject before launch
                ModuleSelector.launch(ModuleSelector.class);
            } catch (Exception e) {
                error("Failed to launch JavaFX UI: " + e.getMessage());
            }
        });

        fxThread.setDaemon(false);
        fxThread.start();
        fxThread.join();
    }

}
