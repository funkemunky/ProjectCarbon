package cc.funkemunky.carbon.exceptions;

public class InvalidHashException extends ClassCastException {
    public InvalidHashException() {
        super("The hash return object is not the same as the one being requested.");
    }
}
