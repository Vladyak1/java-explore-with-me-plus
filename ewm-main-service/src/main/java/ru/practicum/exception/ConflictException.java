package ru.practicum.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(final String massage) {
        super(massage);
    }
}