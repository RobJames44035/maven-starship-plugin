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

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CleanupUtil {

    public void scrubGitMetadata(File rootDir) {
        if (!rootDir.exists() || !rootDir.isDirectory()) return;

        File[] files = rootDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals(".git")) {
                    deleteRecursively(file);
                } else {
                    scrubGitMetadata(file);
                }
            } else if (isGitMetadataFile(file.getName())) {
                file.delete();
            }
        }
    }

    private boolean isGitMetadataFile(String fileName) {
        return fileName.equals(".gitignore") ||
                fileName.equals(".gitattributes") ||
                fileName.equals(".gitmodules") ||
                fileName.equals(".gitkeep");
    }

    private void deleteRecursively(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteRecursively(f);
            }
        }
        file.delete();
    }

    public void scrubAndInit() {
        File root = new File(System.getProperty("user.dir"), "StarshipOS");
        scrubGitMetadata(root);
//        initializeGitRepository(root);
    }
}
