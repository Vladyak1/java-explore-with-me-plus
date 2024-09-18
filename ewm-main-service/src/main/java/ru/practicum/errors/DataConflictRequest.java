package ru.practicum.errors;

public class DataConflictRequest extends RuntimeException {
    public DataConflictRequest(String message) {
        super(message);
    }
}
