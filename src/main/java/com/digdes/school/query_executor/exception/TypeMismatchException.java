package com.digdes.school.query_executor.exception;

public class TypeMismatchException extends Exception{
    public TypeMismatchException(){
        super();
    }

    public TypeMismatchException(String message){
        super(message);
    }


    public TypeMismatchException(String message, Exception cause){
        super(message, cause);
    }
}
