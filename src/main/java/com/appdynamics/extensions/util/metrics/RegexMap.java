package com.appdynamics.extensions.util.metrics;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A wrapper to match regex patterns.
 * @param <V>
 */

public class RegexMap<V>{

    protected Map<Pattern,V> mapOfPatterns = new HashMap<Pattern, V>();

    private static final boolean DEFAULT_ESCAPE_KEY = true;
    boolean escapeKey = DEFAULT_ESCAPE_KEY;

    public RegexMap() {
        this(DEFAULT_ESCAPE_KEY);
    }

    public RegexMap(boolean escapeKeyString) {
        this.escapeKey = escapeKeyString;
    }

    public V put(String key, V value) {
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        String escapedKey = escape(key);
        Pattern compiledPattern = Pattern.compile(escapedKey);
        return mapOfPatterns.put(compiledPattern, value);
    }

    public V get(Object key) {
        Pattern matchedPattern = null;
        if(key == null){
            throw new IllegalArgumentException("Key cannot be null");
        }
        for(Pattern pattern : mapOfPatterns.keySet()){
            if(pattern.matcher(key.toString()).matches()){
                matchedPattern = pattern;
                break;
            }
        }
        if(matchedPattern != null){
            return mapOfPatterns.get(matchedPattern);
        }
        return null;
    }

    private String escape(String pattern) {
        if(escapeKey) {
            return pattern.replaceAll("\\|", "\\\\|");
        }
        return pattern;
    }
}
