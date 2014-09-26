package com.appdynamics.extensions.crypto;


class CipherInitException extends RuntimeException
{
    private static final long serialVersionUID = 4592515503937873874L;

    public CipherInitException(Throwable t){
        super(t);
    }

    public CipherInitException(String msg,Throwable t){
        super(msg,t);
    }

    public CipherInitException(String msg){
        super(msg);
    }



}
