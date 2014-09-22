package com.appdynamics.extensions.util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by abey.tom on 9/17/14.
 *
 * @deprecated I didnt get a chance to test this. Use Filewatcher.java for simple use cases
 */
@Deprecated // I didnt get a chance to test this. Use Filewatcher.java for simple use cases
public class AdvancedFileWatcher {

    private Map<File, Long> modTimeMap;
    private Map<File, List<WeakReference<FileChangeListener>>> listenerMap;

    protected AdvancedFileWatcher() {
        modTimeMap = new ConcurrentHashMap<File, Long>();
        listenerMap = new ConcurrentHashMap<File, List<WeakReference<FileChangeListener>>>();
        final AtomicInteger counter = new AtomicInteger();
        Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "AdvancedFileWatcher-" + counter.incrementAndGet());
            }
        }).scheduleWithFixedDelay(new FileTimestampMonitor(), 1, 5, TimeUnit.SECONDS);
    }

    public void addListener(File file, FileChangeListener listener) {
        if (file != null) {
            if (!listenerMap.containsKey(file)) {
                synchronized (listenerMap) {
                    if (!listenerMap.containsKey(file)) {
                        List<WeakReference<FileChangeListener>>
                                list = new CopyOnWriteArrayList<WeakReference<FileChangeListener>>();
                        list.add(new WeakReference<FileChangeListener>(listener));
                    } else {
                        listenerMap.get(file).add(new WeakReference<FileChangeListener>(listener));
                    }
                }
            } else {
                listenerMap.get(file).add(new WeakReference<FileChangeListener>(listener));
            }
        }
    }

    public static AdvancedFileWatcher getInstance() {
        return FileWatchServiceHolder.INSTANCE;
    }

    protected class FileTimestampMonitor implements Runnable {
        public void run() {
            Set<File> files = listenerMap.keySet();
            for (File file : files) {
                if (file.exists()) {
                    Long lastModTime = modTimeMap.get(file);
                    long l = file.lastModified();
                    if (lastModTime == null || lastModTime.longValue() < l) {
                        modTimeMap.put(file, l);
                        List<WeakReference<FileChangeListener>> references = listenerMap.get(file);
                        for (Iterator<WeakReference<FileChangeListener>> iterator = references.iterator(); iterator.hasNext(); ) {
                            WeakReference<FileChangeListener> reference = iterator.next();
                            FileChangeListener listener = reference.get();
                            if (listener != null) {
                                listener.fileChanged(file);
                            } else {
                                //This means it has been garbage collected
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        }
    }


    private static class FileWatchServiceHolder {
        private static final AdvancedFileWatcher INSTANCE = new AdvancedFileWatcher();
    }

    public interface FileChangeListener {
        public void fileChanged(File file);
    }
}
