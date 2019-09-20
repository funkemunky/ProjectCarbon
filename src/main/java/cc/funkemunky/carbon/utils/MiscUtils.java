package cc.funkemunky.carbon.utils;

import sun.rmi.transport.ObjectTable;

import java.io.*;
import java.util.Base64;

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
}
