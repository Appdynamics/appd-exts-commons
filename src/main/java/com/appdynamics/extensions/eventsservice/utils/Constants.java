/*
 *  Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.eventsservice.utils;

/**
 * @author : Aditya Jagtiani
 */
public class Constants {
    public static final String SCHEMA_PATH_PARAMS = "/events/schema/";
    public static final String EVENT_PATH_PARAM = "/events/publish/";
    public static final String ACCOUNT_NAME_HEADER = "X-Events-API-AccountName";
    public static final String API_KEY_HEADER = "X-Events-API-Key";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ACCEPTED_CONTENT_TYPE = "application/vnd.appd.events+json;v=2";
    public static final String CONTENT_TYPE_HEADER = "Content-type";
}