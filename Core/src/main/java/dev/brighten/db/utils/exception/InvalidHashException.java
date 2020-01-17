package dev.brighten.db.utils.exception;

public class InvalidHashException extends ClassCastException {
    public InvalidHashException() {
        super("The hash return object is not the same as the one being requested.");
    }
}
