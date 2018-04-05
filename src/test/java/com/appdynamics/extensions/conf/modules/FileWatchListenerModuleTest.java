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

package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.file.FileWatchListener;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        FileWatchListener fileWatchListener = new FileWatchListener() {
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
        //Assert.assertTrue(count == 3);
    }
}
