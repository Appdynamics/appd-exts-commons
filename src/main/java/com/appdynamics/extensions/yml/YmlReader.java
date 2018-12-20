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

package com.appdynamics.extensions.yml;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: abey.tom
 * Date: 8/7/14
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class YmlReader {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(YmlReader.class);

    public static <T> T read(InputStream inputStream, Class<T> clazz) {
        Yaml yaml = new Yaml(new Constructor(clazz));
        return (T) yaml.load(inputStream);
    }

    public static <T> T readFromClasspath(String path, Class<T> clazz) {
        logger.info("Reading the contextConfiguration file from class path {}", path);
        InputStream in = YmlReader.class.getResourceAsStream(path);
        if (in != null) {
            return read(in, clazz);
        } else {
            throw new InvalidYmlPathException("The file " + path + " doesn't exit in the classpath");
        }
    }

    public static <T> T readFromFile(String path, Class<T> clazz) {
        if (!Strings.isNullOrEmpty(path)) {
            File file = new File(path);
            return readFromFile(file, clazz);
        } else {
            throw new InvalidYmlPathException("The Yml file argument is not valid: " + path);
        }
    }

    public static <T> T readFromFile(File file, Class<T> clazz) {
        if (file != null) {
            try {
                return read(new FileInputStream(file), clazz);
            } catch (FileNotFoundException e) {
                throw new InvalidYmlPathException("The file " + file.getAbsolutePath() + " doesn't exit in the file system");
            }
        } else {
            throw new InvalidYmlPathException("The Yml file argument is null");
        }
    }

    public static Map<String, ?> readFromFile(File file) {
        Yaml yaml = new Yaml();
        try {
            return (Map) yaml.load(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new InvalidYmlPathException("The file " + file.getAbsolutePath() + " doesn't exit in the file system");
        }
    }

    public static Map<String, ?> readFromFileAsMap(File file) {
        Yaml yaml = new Yaml();
        try {
            return (Map) yaml.load(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new InvalidYmlPathException("The file " + file.getAbsolutePath() + " doesn't exit in the file system");
        }
    }

    public static class InvalidYmlPathException extends RuntimeException {
        public InvalidYmlPathException(String s) {
            super(s);
        }

        public InvalidYmlPathException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}
