package dev.brighten.db.utils.security.hash;

import dev.brighten.db.utils.exception.InvalidHashException;
import dev.brighten.db.utils.security.hash.impl.CRC16;
import dev.brighten.db.utils.security.hash.impl.SHA1;
import dev.brighten.db.utils.security.hash.impl.SHA2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class Hash {
    public HashType hashType;

    private static Map<HashType, Hash> hashes = new HashMap<>();

    public Hash(HashType hashType) {
        this.hashType = hashType;
    }

    public abstract String hash(String toHash);

    public static boolean hashEquals(String one, String two) {
        return one.equals(two);
    }

    public abstract boolean hashEqualsKey(String hash, String key);


    public static void loadHashes() {
        Arrays.asList(new CRC16(), new SHA1(), new SHA2())
                .forEach(hash -> hashes.put(hash.hashType, hash));
    }

    public static <T> T getHashByType(HashType type) {
        Hash hash = hashes.get(type);

        try {
            return (T) hashes.get(type);
        } catch(ClassCastException e) {
            throw new InvalidHashException();
        }
    }
}
