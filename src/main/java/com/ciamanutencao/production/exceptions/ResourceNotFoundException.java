package com.ciamanutencao.production.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Object id) {
        super("Resource not found. ID: " + id);
    }

}
