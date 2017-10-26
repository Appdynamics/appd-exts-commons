package com.appdynamics.extensions.conf.configurationModules;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.PathResolver;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static com.appdynamics.extensions.conf.MonitorConfiguration.isWorkbenchMode;

/**
 * Created by venkata.konala on 10/24/17.
 */
public class FileWatchListenerModule {

    private static final Logger logger = LoggerFactory.getLogger(FileWatchListenerModule.class);
    private Set<File> monitoredDirs;
    private Map<File, MonitorConfiguration.FileWatchListener> listenerMap;
    private FileAlterationMonitor monitor;
    private Integer fileWatcherInterval;

    public void createListener(String path, MonitorConfiguration.FileWatchListener fileWatchListener, File installDir, MetricWriteHelper workbench, Integer fileWatcherInterval) {
        this.fileWatcherInterval = fileWatcherInterval;
        File file = resolvePath(path, installDir);
        logger.debug("The path [{}] is resolved to file {}", path, file.getAbsolutePath());
        createListener(file, fileWatchListener);
        createWatcher(file, workbench);
        //Initialize it for the fisrt time
        fileWatchListener.onFileChange(file);
    }

    private File resolvePath(String path, File installDir) {
        File file = PathResolver.getFile(path, installDir);
        if (file != null && file.exists()) {
            return file;
        } else {
            throw new IllegalArgumentException("The path [" + path + "] cannot be resolved to a file");
        }
    }

    private void createListener(File file, MonitorConfiguration.FileWatchListener fileWatchListener) {
        if (listenerMap == null) {
            listenerMap = new HashMap<File, MonitorConfiguration.FileWatchListener>();
        }
        listenerMap.put(file, fileWatchListener);
    }

    private void createWatcher(File file, final MetricWriteHelper workbench) {
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
                        MonitorConfiguration.FileWatchListener fileWatchListener = listenerMap.get(file);
                        if (fileWatchListener != null) {
                            fileWatchListener.onFileChange(file);
                        }
                    } catch (Exception e) {
                        logger.error("Error while invoking the file watch listener", e);
                    } finally {
                        if (workbench != null) {
                            workbench.reset();
                        }
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
        monitor = new FileAlterationMonitor(interval);
        try {
            monitor.start();
        } catch (Exception e) {
            logger.error("Exception while starting the FileAlterationMonitor", e);
        }
    }
}
