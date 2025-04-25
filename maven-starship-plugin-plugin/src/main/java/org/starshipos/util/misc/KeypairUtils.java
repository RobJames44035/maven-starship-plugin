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

// KeypairUtils.java
package org.starshipos.util.misc;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

public class KeypairUtils {

    public static void createKeystore() throws Exception {
        String username = System.getProperty("user.name");
        String alias = "starship-os";
        File keystore = new File(System.getProperty("user.home"), "./starship-os-" + username + ".jks");
        keystore.getParentFile().mkdirs();

        ProcessBuilder pb = new ProcessBuilder(
                "keytool",
                "-genkeypair",
                "-alias", alias,
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "3650",
                "-keystore", keystore.getAbsolutePath()
        );
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new MojoExecutionException("keytool failed with exit code " + exitCode);
        }
    }
}
