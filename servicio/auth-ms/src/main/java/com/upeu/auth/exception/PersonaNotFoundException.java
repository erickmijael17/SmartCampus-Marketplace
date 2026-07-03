package com.upeu.auth.exception;

public class PersonaNotFoundException extends RuntimeException {
    public PersonaNotFoundException(String message) {
        super(message);
    }
}
