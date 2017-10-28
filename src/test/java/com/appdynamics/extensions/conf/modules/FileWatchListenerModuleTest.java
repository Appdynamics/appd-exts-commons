package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by venkata.konala on 10/25/17.
 */
public class FileWatchListenerModuleTest {

    int count = 0;

    @Test
    public void whenFileChangedWillCountNumberOfChanges() throws IOException, InterruptedException {

        FileWatchListenerModule fileWatchListenerModule = new FileWatchListenerModule();
        MonitorConfiguration.FileWatchListener fileWatchListener = new MonitorConfiguration.FileWatchListener() {
            public void onFileChange(File file) {
                   count++;
            }
        };
        fileWatchListenerModule.createListener("src/test/resources/conf/config_WithFileWatchListener.yml", fileWatchListener, new File("/Users/venkata.konala/AppDynamics/Repos/appd-exts-commons/src/test/resources/conf"), null, 2000);
        int i = 0;
        while(i <= 2) {
            List<String> newLines = new ArrayList<>();
            newLines.add("Hi");// newLines.add("By1e");
            Files.write(Paths.get(new File("src/test/resources/conf/config_WithFileWatchListener.yml").toURI()), newLines, StandardCharsets.UTF_8);
            Thread.sleep(2500);
            i++;
        }
        Assert.assertTrue(count == 3);
    }
}
