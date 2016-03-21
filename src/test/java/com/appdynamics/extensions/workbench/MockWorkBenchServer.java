package com.appdynamics.extensions.workbench;

import com.appdynamics.extensions.workbench.metric.WorkbenchMetricStore;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by abey.tom on 3/20/16.
 */
public class MockWorkBenchServer extends WorkBenchServer {
    public MockWorkBenchServer(String hostname, int port, WorkbenchMetricStore metricStore) {
        super(hostname, port, metricStore);
    }

    @Override
    protected InputStream getResourceAsStream(String uri) {
        File f = new File("src/main/resources", uri);
        try {
            return new FileInputStream(f);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        final WorkbenchMetricStore store = WorkbenchMetricStore.getInstance();
        setupData(store);
        MockWorkBenchServer server = new MockWorkBenchServer("0.0.0.0", 9191, store);
        server.start(5000, false);
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        setupData(store);
                    } catch (IOException e) {
                        logger.error("", e);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                }

            }
        }).start();
    }

    private static void setupData(WorkbenchMetricStore store) throws IOException {
        List<String> lines = FileUtils.readLines(new File("src/test/resources/workbench/metrics.txt"));
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] split = line.split("=");
                if (split.length == 2) {
                    store.printMetric(split[0], new BigDecimal(split[1]), "");
                }
            }
        }
        store.registerError("Error message1", new RuntimeException());
        store.registerError("Error message2", new RuntimeException());
    }
}
