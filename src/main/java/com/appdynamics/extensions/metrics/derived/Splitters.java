package com.appdynamics.extensions.metrics.derived;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
/**
 * Created by venkata.konala on 8/28/17.
 */
class Splitters {
         static final Splitter PIPE_SPLITTER = Splitter.on('|')
                .omitEmptyStrings()
                .trimResults();
         static final Splitter FORMULA_SPLITTER = Splitter.on(CharMatcher.anyOf("(+-*/%^)"))
                .trimResults()
                .omitEmptyStrings();
}
