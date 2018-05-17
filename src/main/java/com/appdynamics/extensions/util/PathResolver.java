/*
 * Copyright (c) 2018 AppDynamics,Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 4/4/14
 * Time: 9:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class PathResolver {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(PathResolver.class);

    public static File resolveDirectory(Class clazz) {
        File installDir = null;
        try {
            ProtectionDomain pd = clazz.getProtectionDomain();
            if (pd != null) {
                CodeSource cs = pd.getCodeSource();
                if (cs != null) {
                    URL url = cs.getLocation();
                    if (url != null) {
                        String path = URLDecoder.decode(url.getFile(), "UTF-8");
                        File dir = new File(path).getParentFile();
                        if (dir.exists()) {
                            installDir = dir;
                        } else {
                            logger.error("Install dir resolved to " + dir.getAbsolutePath() + ", however it doesnt exist.");
                        }
                    }
                } else {
                    logger.warn("Cannot resolve path for the class {} since CodeSource is null", clazz.getName());
                }

            }
        } catch (Exception e) {
            logger.error("Error while resolving the Install Dir", e);
        }
        if (installDir != null) {
            logger.info("Install dir resolved to " + installDir.getAbsolutePath());
            return installDir;
        } else {
            File workDir = new File("");
            logger.info("Failed to resolve install dir, returning current work dir" + workDir.getAbsolutePath());
            return workDir;
        }
    }

    public static File getFile(String path, Class<?> clazz) {
        if (path == null) {
            return null;
        }

        File file = new File(path);
        if (file.exists()) {
            return new File(path);
        }

        File installDir = resolveDirectory(clazz);
        if (installDir != null) {
            logger.debug("The install directory is resolved to {}", installDir.getAbsolutePath());
            file = new File(installDir, path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public static File getFile(String path, File installDir) {
        if (path == null) {
            return null;
        }

        File file = new File(path);
        if (file.exists()) {
            return new File(path);
        }

        if (installDir != null) {
            logger.debug("The install directory is resolved to {}", installDir.getAbsolutePath());
            file = new File(installDir, path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
}
