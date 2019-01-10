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

package com.appdynamics.extensions.util;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by abey.tom on 9/17/14.
 */
public class FileWatcher {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(FileWatcher.class);
    private File file;
    private FileChangeListener listener;

    private long lastModifiedTime = -1;
    private ScheduledExecutorService executor;


    public static FileWatcher watch(File file, FileChangeListener listener) {
        FileWatcher fileWatcher = new FileWatcher(file, listener);
        fileWatcher.start();
        return fileWatcher;
    }

    public FileWatcher(File file, FileChangeListener listener) {
        if (file != null && listener != null) {
            this.file = file;
            this.listener = listener;
        } else {
            throw new IllegalArgumentException("The File and Listener cannot be null");
        }
    }

    public void start() {
        if (file != null && listener != null) {
            if (executor == null) {
                final AtomicInteger counter = new AtomicInteger();
                executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable, "FileWatcher-" + counter.incrementAndGet());
                    }
                });
                logger.info("Created the File Watcher for the file {} and Listener {}", file.getAbsolutePath(), listener);
                executor.scheduleWithFixedDelay(new FileTimestampMonitor(), 1, 60, TimeUnit.SECONDS);
            } else {
                throw new IllegalStateException("Looks like the task has been already started");
            }
        } else {
            throw new IllegalStateException("Looks like the task has been cancelled. Please create a new instance of file watcher");
        }
    }

    public void stop() {
        executor.shutdown();
        file = null;
        listener = null;
    }

    private class FileTimestampMonitor implements Runnable {

        public void run() {
            if (file != null && listener != null && file.exists()) {
                logger.debug("Checking the timestamp of the file {}, previous={} and current={}"
                        , file.getAbsolutePath(), lastModifiedTime, file.lastModified());
                if (lastModifiedTime < file.lastModified()) {
                    if (lastModifiedTime > 0) {
                        try {
                            logger.info("The file {}, was updated previous={} and current={}"
                                    , file.getAbsolutePath(), lastModifiedTime, file.lastModified());
                            listener.fileChanged();
                        } catch (Exception e) {
                            logger.error("Exception while invoking the file watch callback");
                        }
                    }
                    lastModifiedTime = file.lastModified();
                }
            }
        }
    }

    public interface FileChangeListener {
        public void fileChanged();
    }
}
