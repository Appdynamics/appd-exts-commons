package com.appdynamics.extensions.util.transformers;

import com.appdynamics.extensions.util.Metric;

import java.util.Collection;
import java.util.List;

/**
 * Created by venkata.konala on 8/31/17.
 */
public class Transformer {
    private List<Metric> metricList;
    private DeltaTranform deltaTranform = new DeltaTranform();
    private MultiplierTransform multiplierTransform = new MultiplierTransform();
    private ConvertTransform convertTransform = new ConvertTransform();

    public Transformer(List<Metric> metricList){
        this.metricList = metricList;
    }


    public void transform(){
        if(metricList != null){
            for(Metric metric : metricList){
                    applyTransforms(metric);
            }
        }
    }

    private void applyTransforms(Metric metric){
        if(metric.getMetricValue() != null) {
            deltaTranform.applyDelta(metric);
        }
        if(metric.getMetricValue()  != null) {
            multiplierTransform.multiply(metric);
        }
        if(metric.getMetricValue() != null) {
            convertTransform.convert(metric);
        }
    }
}
