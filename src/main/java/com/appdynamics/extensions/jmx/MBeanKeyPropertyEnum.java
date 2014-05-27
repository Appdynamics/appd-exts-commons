package com.appdynamics.extensions.jmx;


public enum MBeanKeyPropertyEnum {
    TYPE("type"),
    SCOPE("scope"),
    NAME("name"),
    KEYSPACE("keyspace"),
    PATH("path"),
    COLUMNFAMILY("columnfamily");

    private final String name;

    private MBeanKeyPropertyEnum(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }
}
