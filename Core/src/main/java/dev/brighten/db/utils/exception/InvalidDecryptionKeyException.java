package dev.brighten.db.utils.exception;

public class InvalidDecryptionKeyException extends Exception {

    public InvalidDecryptionKeyException(String key) {
        super("Invalid decryption key: " + key);
    }
}
