package ru.practicum.internal.error.exception;

public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String massage) {
        super(massage);
    }
}
