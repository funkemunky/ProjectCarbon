package cc.funkemunky.carbon.exceptions;

public class InvalidDecryptionKeyException extends Exception {

    public InvalidDecryptionKeyException(String key) {
        super("Invalid decryption key: " + key);
    }
}
