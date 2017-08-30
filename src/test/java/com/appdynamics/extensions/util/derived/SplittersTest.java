package com.appdynamics.extensions.util.derived;

import com.google.common.base.Splitter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by venkata.konala on 8/29/17.
 */
public class SplittersTest {
    private Splitters splitters = new Splitters();
    private String metricPath = "Server|Component:AppLevels|Custom Metrics|Redis|Server1|Queue|Q2|ratio";
    private String formula = "hit / (hits + misses) * 4";

    @Test
    public void pipeSplitterTest(){
        Splitter pipeSplitter = splitters.getPipeSplitter();
        List<String> splitList = pipeSplitter.splitToList(metricPath);
        Assert.assertTrue(splitList.size() == 8);
        Assert.assertTrue(splitList.get(splitList.size() - 1).equals("ratio"));
        List<String> emptySplitList = pipeSplitter.splitToList("");
        Assert.assertTrue(emptySplitList.size() == 0);
    }

    @Test
    public void formulaSplitterTest(){
        Splitter formulaSplitter = splitters.getFormulaSplitter();
        List<String> splitList = formulaSplitter.splitToList(formula);
        Assert.assertTrue(splitList.size() == 4);
        Assert.assertTrue(splitList.get(splitList.size() - 1).equals("4"));
        List<String> emptySplitList = formulaSplitter.splitToList("");
        Assert.assertTrue(emptySplitList.size() == 0);
    }
}
