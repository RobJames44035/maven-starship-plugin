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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class BuildJDKUtil {

    private static final String JDK_SRC_DIR = "StarshipOS/openjdk";

    public BuildJDKUtil() {
        // Constructor for future setup if needed
    }

    public void buildJDK(String arch) throws Exception {
        configure(arch);
        build(arch);
    }

    public void configure(String architecture) throws Exception {
        File jdkSrcDir = new File(System.getProperty("user.dir"), JDK_SRC_DIR);
        File buildDir = new File(jdkSrcDir, "build-" + architecture);

        if (!buildDir.exists() && !buildDir.mkdirs()) {
            throw new Exception("Failed to create build directory: " + buildDir.getAbsolutePath());
        }

        File configureScript = new File(jdkSrcDir, "configure");
        Path configurePath = configureScript.toPath();
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);
        Files.setPosixFilePermissions(configurePath, perms);

        String targetTriplet = architecture.equals("arm") ? "arm-linux-gnueabihf" : "x86_64-linux-gnu";

        ProcessBuilder configureBuilder = new ProcessBuilder(
                "../configure",
                "--openjdk-target=" + targetTriplet,
                "--with-debug-level=release",
                "--enable-option-checking=fatal",
                "--with-native-debug-symbols=none",
                "--with-jvm-variants=server",
                "--with-version-pre=starship",
                "--with-version-build=1",
                "--with-version-opt=reloc",
                "--with-toolchain-type=gcc",
                "--disable-warnings-as-errors"
        ).directory(buildDir).inheritIO();

        Process process = configureBuilder.start();
        if (process.waitFor() != 0) {
            throw new Exception("OpenJDK configure failed for: " + architecture);
        }
    }

    public void build(String architecture) throws Exception {
        File jdkSrcDir = new File(System.getProperty("user.dir"), JDK_SRC_DIR);
        File buildDir = new File(jdkSrcDir, "build-" + architecture);

        ProcessBuilder buildBuilder = new ProcessBuilder("make", "images")
                .directory(buildDir).inheritIO();

        Process process = buildBuilder.start();
        if (process.waitFor() != 0) {
            throw new Exception("OpenJDK build failed for: " + architecture);
        }
    }
}
