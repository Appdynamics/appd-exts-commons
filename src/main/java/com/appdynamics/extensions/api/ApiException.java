package com.appdynamics.extensions.api;

public class ApiException extends Throwable {

    ApiException(String s,Throwable e) {
        super(s,e);
    }
}
