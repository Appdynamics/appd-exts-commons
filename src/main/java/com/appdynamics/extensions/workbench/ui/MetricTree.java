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

package com.appdynamics.extensions.workbench.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by abey.tom on 3/16/16.
 */
public class MetricTree {
    public String text;
    public String metricPath;
    public List<MetricTree> nodes;

    public MetricTree() {
    }

    public MetricTree(String text) {
        this.text = text;
    }

    public List<MetricTree> getNodes() {
        return nodes;
    }

    public void setNodes(List<MetricTree> nodes) {
        this.nodes = nodes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void add(String[] paths, String path) {
        add(paths, 0,path);
    }

    public String getMetricPath() {
        return metricPath;
    }

    public void setMetricPath(String metricPath) {
        this.metricPath = metricPath;
    }

    private void add(String[] paths, int index, String path) {
        if (index < paths.length) {
            if (nodes == null) {
                nodes = new ArrayList<MetricTree>();
            }
            boolean added = false;
            for (MetricTree node : nodes) {
                if (node.getText().equals(paths[index])) {
                    node.add(paths, ++index, path);
                    added = true;
                    break;
                }
            }
            if (!added) {
                MetricTree tree = new MetricTree(paths[index]);
                tree.add(paths, ++index, path);
                nodes.add(tree);
                Collections.sort(nodes, TreeComparator.INSTANCE);

            }
        } else{
            this.metricPath = path;
        }
    }

    public static class TreeComparator implements Comparator<MetricTree> {

        public static TreeComparator INSTANCE = new TreeComparator();

        public int compare(MetricTree o1, MetricTree o2) {
            if (o1.getNodes() != null && o2.getNodes() != null) {
                return o1.getText().compareTo(o2.getText());
            } else if (o1.getNodes() != null) {
                return -1;
            } else if (o2.getNodes() != null) {
                return 1;
            } else {
                //both are null
                return o1.getText().compareTo(o2.getText());
            }
        }
    }
}
