package cc.funkemunky.carbon.utils;

import sun.rmi.transport.ObjectTable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class MiscUtils {
    public static <T> T parseObjectFromString(String s, Class<T> clazz) throws Exception {
        return clazz.getConstructor(new Class[] {String.class}).newInstance(s);
    }

    public static byte[] getBytesOfObject(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bos);
        objectStream.writeObject(object);
        objectStream.flush();

        return bos.toByteArray();
    }

    public static String bytesToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] bytesFromString(String bytesString) {
        return Base64.getDecoder().decode(bytesString);
    }

    public static Object objectFromBytes(byte[] byteArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream ias = new ByteArrayInputStream(byteArray);
        ObjectInputStream ois = new ObjectInputStream(ias);

        return ois.readObject();
    }

    public static String randomString(int count, boolean fast) {
        double val = ThreadLocalRandom.current().nextDouble(-10000, 10000);

        try {
            byte[] bytesOfDouble = getBytesOfObject(val);

            if(!fast) {
                String string = getSHA256String(bytesToString(bytesOfDouble));
                if(string != null) {
                    return string.substring(0, Math.min(count, string.length() - 1));
                }
            } else {
                return bytesToHex(bytesOfDouble);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Credit: https://www.baeldung.com/sha-256-hashing-java
    public static byte[] getSHA256(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(
                    string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Credit: https://www.baeldung.com/sha-256-hashing-java
    public static String getSHA256String(String string) {
        byte[] array = getSHA256(string);

        if(array != null) {
            return bytesToHex(array);
        }
        return null;
    }

    //Credit: https://www.baeldung.com/sha-256-hashing-java
    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
