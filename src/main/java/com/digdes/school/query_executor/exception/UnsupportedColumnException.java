package com.digdes.school.query_executor.exception;

public class UnsupportedColumnException extends Exception {
    public UnsupportedColumnException(){
        super();
    }

    public UnsupportedColumnException(String message){
        super(message);
    }
}
