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

package com.appdynamics.extensions.executorservice;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface MonitorExecutorService extends ConcealedMonitorExecutorService {

    Future<?> submit(String name,Runnable task);

    void execute(String name,Runnable task);

    <T> Future<T> submit(String name,Callable<T> task);

    void scheduleAtFixedRate(String name,Runnable task, int initialDelaySeconds, int taskDelaySeconds, TimeUnit seconds);
}

