package com.crypto.jtrade.common.util.myserialize;

public class MySerializeException extends RuntimeException {
    
    public MySerializeException() {
    
    }
    
    public MySerializeException(String message) {
        super(message);
    }
    
    public MySerializeException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
