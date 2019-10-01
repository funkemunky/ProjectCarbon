package cc.funkemunky.carbon.utils.security.hash.impl;

import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1 extends Hash {

    public SHA1() {
        super(HashType.SHA1);
    }

    public String hash(String toHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] encodedhash = digest.digest(
                    toHash.getBytes(StandardCharsets.UTF_8));

            return GeneralUtils.bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return toHash;
    }

    public boolean hashEqualsKey(String hash, String key) {
        return hash.equals(hash(key));
    }
}
