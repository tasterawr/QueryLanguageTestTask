package com.digdes.school.query_executor.exception;

public class SyntaxErrorException extends Exception {
    public SyntaxErrorException(){
        super();
    }

    public SyntaxErrorException(String message){
        super(message);
    }
}
