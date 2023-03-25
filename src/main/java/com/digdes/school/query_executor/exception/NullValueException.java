package com.digdes.school.query_executor.exception;

public class NullValueException extends Exception{
    public NullValueException(){
        super();
    }

    public NullValueException(String message){
        super(message);
    }
}
