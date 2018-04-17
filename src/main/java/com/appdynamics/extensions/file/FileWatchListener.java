/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.file;

import java.io.File;

/**
 * Created by venkata.konala on 4/2/18.
 */
public interface FileWatchListener {
    void onFileChange(File file);
}
