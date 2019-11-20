/*
 * Copyright (c) 2019 AppDynamics,Inc.
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
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.appdynamics.extensions.conf.MonitorContext.isWorkbenchMode;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class FileWatcher {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(FileWatcher.class);
    private Set<File> monitoredDirs;
    private Map<File, FileWatchListener> listenerMap;
    private FileAlterationMonitor monitor;
    private Integer fileWatcherInterval;

    public void createListener(String path, FileWatchListener fileWatchListener, File installDir, Integer fileWatcherInterval) {
        this.fileWatcherInterval = fileWatcherInterval;
        File file = resolvePath(path, installDir);
        logger.debug("The path [{}] is resolved to file {}", path, file.getAbsolutePath());
        createListener(file, fileWatchListener);
        createWatcher(file);
        //Initialize it for the fisrt time
        //TODO do we really need to initialize it explicitly?
        // TODO PN - config.yml is already registered
        //fileWatchListener.onFileChange(file,true);
    }

    private File resolvePath(String path, File installDir) {
        File file = PathResolver.getFile(path, installDir);
        if (file != null && file.exists()) {
            return file;
        } else {
            throw new IllegalArgumentException("The path [" + path + "] cannot be resolved to a file");
        }
    }

    private void createListener(File file, FileWatchListener fileWatchListener) {
        if (listenerMap == null) {
            listenerMap = new HashMap<File, FileWatchListener>();
        }
        listenerMap.put(file, fileWatchListener);
    }

    private void createWatcher(File file) {
        File dir = file.getParentFile();
        if (monitor == null) {
            initMonitor();
        }
        if (monitoredDirs == null || !monitoredDirs.contains(dir)) {
            logger.debug("Creating a watcher for the directory {}", dir.getAbsolutePath());
            FileAlterationObserver observer = new FileAlterationObserver(dir);
            observer.addListener(new FileAlterationListenerAdaptor() {
                @Override
                public void onFileChange(File file) {
                    try {
                        logger.info("The file {} has been modified", file.getAbsolutePath());
                        FileWatchListener fileWatchListener = listenerMap.get(file);
                        if (fileWatchListener != null) {
                            fileWatchListener.onFileChange(file);
                        }
                    } catch (Exception e) {
                        logger.error("Error while invoking the file watch listener", e);
                    }
                }
            });
            monitor.addObserver(observer);
            if (monitoredDirs == null) {
                monitoredDirs = new HashSet<File>();
            }
            monitoredDirs.add(dir);
        }
    }

    private void initMonitor() {
        int interval;
        if (isWorkbenchMode()) {
            interval = 3000;
        } else {
            interval = fileWatcherInterval;
        }
        logger.debug("Created a FileAlterationMonitor with an interval of {}", interval);
        //TODO: Polling every 3 secs might be costly, looks for alternatives.
        monitor = new FileAlterationMonitor(interval);
        try {
            monitor.start();
        } catch (Exception e) {
            logger.error("Exception while starting the FileAlterationMonitor", e);
        }
    }
}
