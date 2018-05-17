/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions;

/**
 * Stores the running extension name.
 *
 * @author Satish Muddam
 */
public final class ExtensionName {

    private static String name;

    public static void setName(String name) {
        if (ExtensionName.name == null) {
            ExtensionName.name = name;
        }
    }

    public static String getName() {
        return name;
    }
}