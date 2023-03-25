package com.digdes.school.query_executor.exception;

public class QueryExecutorInstantiationException extends RuntimeException {
    public QueryExecutorInstantiationException(){
        super();
    }

    public QueryExecutorInstantiationException(String message, Exception cause){
        super(message, cause);
    }
}
