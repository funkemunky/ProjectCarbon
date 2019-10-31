package cc.funkemunky.carbon.utils.security.hash.impl;

import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Argon extends Hash {
    private static Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public Argon() {
        super(HashType.ARGON);
    }

    public String hash(String toHash) {
        return argon2.hash(4, 1024 * 1024, 8, toHash);
    }

    @Override
    public boolean hashEqualsKey(String hash, String key) {
        return argon2.verify(hash, key);
    }
}
