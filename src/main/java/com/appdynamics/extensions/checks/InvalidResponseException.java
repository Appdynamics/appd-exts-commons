package com.appdynamics.extensions.checks;

/**
 * @author Satish Muddam
 */
public class InvalidResponseException extends RuntimeException {
    
    public InvalidResponseException(int errorCode, String message) {
        super("[HTTP " + errorCode + "] " + message);
    }

}
