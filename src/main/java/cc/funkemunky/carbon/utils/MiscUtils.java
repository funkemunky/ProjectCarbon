package cc.funkemunky.carbon.utils;

public class MiscUtils {
    public static <T> T parseObjectFromString(String s, Class<T> clazz) throws Exception {
        return clazz.getConstructor(new Class[] {String.class}).newInstance(s);
    }
}
