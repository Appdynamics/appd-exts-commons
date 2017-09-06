package com.appdynamics.extensions.util.derived;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
/**
 * Created by venkata.konala on 8/28/17.
 */
class Constants {
         static final Splitter pipeSplitter = Splitter.on('|')
                .omitEmptyStrings()
                .trimResults();
         static final Splitter formulaSplitter = Splitter.on(CharMatcher.anyOf("(+-*/%^) "))
                .trimResults()
                .omitEmptyStrings();
         static final MetricNameFetcher metricNameFetcher = new MetricNameFetcher();
}
