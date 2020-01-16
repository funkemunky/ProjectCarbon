package dev.brighten.db.utils;

import cc.funkemunky.carbon.utils.reflection.types.WrappedClass;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import cc.funkemunky.carbon.utils.security.hash.impl.SHA1;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class MiscUtils {
    public static <T> T parseObjectFromString(String s, Class<T> clazz) {
        WrappedClass wrappedClass = new WrappedClass(clazz);
        return wrappedClass.getConstructor(new Class[] {String.class}).newInstance(s);
    }

    public static <T> T parseObjectFromString(String s, WrappedClass wrapped) {
        return (T) wrapped.getConstructor(new Class[] {String.class}).newInstance(s);
    }

    public static byte[] getBytesOfObject(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(bos);
        objectStream.writeObject(object);
        objectStream.flush();

        return bos.toByteArray();
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
                SHA1 sha1 = Hash.getHashByType(HashType.SHA1);
                String string = sha1.hash(GeneralUtils.bytesToString(bytesOfDouble));
                if(string != null) {
                    return string.substring(0, Math.min(count, string.length() - 1));
                }
            } else {
                return GeneralUtils.bytesToHex(bytesOfDouble);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
