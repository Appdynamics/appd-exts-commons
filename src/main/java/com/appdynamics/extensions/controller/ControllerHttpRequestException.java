package com.appdynamics.extensions.controller;

public class ControllerHttpRequestException extends Throwable {

    public ControllerHttpRequestException(String s, Throwable e) {
        super(s,e);
    }
}
