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

package org.starshipos.ui;

import javafx.scene.layout.VBox;

/**
 * Abstract class for feature selectors like POM modules or install configuration.
 * Defines common methods like initialization and getting the UI component.
 */
public abstract class AbstractFeatureSelector {
    /**
     * Initializes the selector (e.g., loads configuration or state).
     *
     * @throws Exception if initialization fails
     */
    public abstract void initialize() throws Exception;

    /**
     * Returns the UI component (e.g., VBox) for rendering.
     *
     * @return the UI component
     */
    public abstract VBox getUI();

    /**
     * Saves any changes made through the UI or state.
     *
     * @throws Exception if saving fails
     */
    public abstract void save() throws Exception;
}