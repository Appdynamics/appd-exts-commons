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

package com.appdynamics.extensions.file;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.PathResolver;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;

import java.io.File;

/**
 * Created by abey.tom on 7/1/15.
 */
public class FileLoader {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(FileLoader.class);

    public static void load(final Listener listener, String... paths) {
        FileAlterationMonitor monitor = new FileAlterationMonitor(30000);
        for (String path : paths) {
            File file = PathResolver.getFile(path, AManagedMonitor.class);
            if (file != null && file.exists()) {
                logger.info("Adding file watcher for {}", file.getAbsolutePath());
                FileAlterationObserver observer = new FileAlterationObserver(file.getParentFile());
                observer.addListener(new FileWatchListener(listener, file));
                monitor.addObserver(observer);
                listener.load(file);
            } else {
                String message = String.format("The file is not found.The file path %s is resolved to %s",
                        path, file != null ? file.getAbsolutePath() : null);
                logger.error(message);
                throw new IllegalArgumentException(message);
            }
        }
        try {
            monitor.start();
        } catch (Exception e) {
            logger.error("Exception while starting the FileAlterationMonitor", e);
        }
    }

    public interface Listener {
        void load(File file);
    }

    public static class FileWatchListener extends FileAlterationListenerAdaptor {
        private Listener listener;
        private File monitoredFile;

        public FileWatchListener(Listener listener, File monitoredFile) {
            this.listener = listener;
            this.monitoredFile = monitoredFile;
        }

        public void onFileChange(File file) {
            if (monitoredFile.equals(file)) {
                logger.info("The file [{}] has changed, reloading", file.getAbsolutePath());
                listener.load(file);
            }
        }
    }

}
