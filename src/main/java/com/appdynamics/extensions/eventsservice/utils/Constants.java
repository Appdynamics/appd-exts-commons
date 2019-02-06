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

package com.appdynamics.extensions.eventsservice.utils;

/**
 * @author : Aditya Jagtiani
 */
public class Constants {
    public static final String SCHEMA_PATH = "/events/schema/";
    public static final String PUBLISH_PATH = "/events/publish/";
    public static final String QUERY_PATH = "/events/query";
    public static final String ACCOUNT_NAME_HEADER = "X-Events-API-AccountName";
    public static final String API_KEY_HEADER = "X-Events-API-Key";
    public static final String ACCEPT_HEADER = "Accept";
    public static final String ACCEPTED_CONTENT_TYPE = "application/vnd.appd.events+json;v=2";
    public static final String CONTENT_TYPE_HEADER = "Content-type";
}