package com.appdynamics.extensions.util.derived;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * Created by venkata.konala on 8/28/17.
 */
public class Splitters {

    public Splitter getPipeSplitter(){
        Splitter pipeSplitter = Splitter.on('|')
                .omitEmptyStrings()
                .trimResults();
        return pipeSplitter;
    }

    public Splitter getFormulaSplitter(){
        Splitter formulaSplitter = Splitter.on(CharMatcher.anyOf("(+-*/%^) "))
                .trimResults()
                .omitEmptyStrings();
        return formulaSplitter;
    }

}
