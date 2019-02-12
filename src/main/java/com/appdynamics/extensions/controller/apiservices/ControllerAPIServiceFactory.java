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

package com.appdynamics.extensions.controller.apiservices;

import com.appdynamics.extensions.controller.ControllerClient;
import com.appdynamics.extensions.controller.ControllerInfo;
import com.appdynamics.extensions.util.AssertUtils;

/**
 * Created by venkata.konala on 12/20/18.
 */
public class ControllerAPIServiceFactory {

    private static final ControllerAPIService controllerAPIService = new ControllerAPIService();

    //#TODO Assert for ControllerClient
    public static ControllerAPIService initialize(ControllerInfo controllerInfo, ControllerClient controllerClient) {
        //#TODO @venkata.konala this reset is not needed.
        AssertUtils.assertNotNull(controllerClient, "The ControllerClient is null");
        controllerAPIService.initialize(controllerInfo, controllerClient);
        return controllerAPIService;
    }
}
