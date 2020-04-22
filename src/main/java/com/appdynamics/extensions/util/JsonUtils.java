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

package com.appdynamics.extensions.util;


import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;

import java.util.Iterator;

/**
 * Created by abey.tom on 3/16/16.
 */
public class JsonUtils {
    public static final Logger logger = ExtensionsLoggerFactory.getLogger(JsonUtils.class);

    public static String asJson(Object object) {
        if (object != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(object);
            } catch (Exception e) {
                logger.error("Error while converting the Object to Json " + object, e);
            }
        }
        return null;
    }

    /**
     * Gets the nested JSON object based on a path. The wildcards are also supported
     *
     * @param entry
     * @param nested
     * @return
     */
    public static JsonNode getNestedObject(JsonNode entry, String... nested) {
        if (entry != null && nested != null) {
            JsonNode parent = entry;
            for (int i = 0; i < nested.length; i++) {
                String key = nested[i];
                // * is used in hostInfo
                if ("*".equals(key)) {
                    ArrayNode children = getChildren(parent);
                    // If the wildcard comes in the last, then return the child nodes
                    if (nested.length == i + 1) {
                        return children;
                    } else {
                        //Iterate thru the children, and get the matching nodes.
                        //Split the nested and take the remaining part of it.
                        String[] arr = new String[nested.length - i - 1];
                        System.arraycopy(nested, i + 1, arr, 0, arr.length);
                        ArrayNode arrayNodes = JsonNodeFactory.instance.arrayNode();
                        for (JsonNode jsonNode : children) {
                            //Using the remaining part of nested, get the nested objects
                            JsonNode jsonObject = getNestedObject(jsonNode, arr);
                            if (jsonObject instanceof ArrayNode) {
                                //If multiple nodes are matched, add it to an array
                                ArrayNode nodes = (ArrayNode) jsonObject;
                                for (JsonNode node : nodes) {
                                    arrayNodes.add(node);
                                }
                            } else if (jsonObject != null) {
                                arrayNodes.add(jsonObject);
                            }
                        }
                        return arrayNodes;
                    }
                } else {
                    if (parent instanceof ArrayNode) {
                        ArrayNode parentNodes = (ArrayNode) parent;
                        ArrayNode arrayNodes = JsonNodeFactory.instance.arrayNode();
                        for (JsonNode parentNode : parentNodes) {
                            JsonNode jsonNode = parentNode.get(key);
                            if (jsonNode != null) {
                                arrayNodes.add(jsonNode);
                            }
                        }
                        parent = arrayNodes.size() > 0 ? arrayNodes : null;
                    } else {
                        parent = parent.get(key);
                    }
                    if (parent == null) {
                        return null;
                    }
                }
            }
            return parent;
        } else {
            return null;
        }
    }

    private static ArrayNode getChildren(JsonNode node) {
        if (node != null) {
            ArrayNode nodes = JsonNodeFactory.instance.arrayNode();
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                JsonNode jsonNode = elements.next();
                nodes.add(jsonNode);
            }
            return nodes;
        }
        return null;
    }

    public static String getTextValue(JsonNode node, String... nested) {
        JsonNode jsonObject = getNestedObject(node, nested);
        if (jsonObject != null) {
            if (jsonObject.isValueNode()) {
                if (jsonObject.isTextual()) {
                    return jsonObject.asText();
                } else {
                    return jsonObject.toString();
                }
            }
        }
        return null;
    }
}
