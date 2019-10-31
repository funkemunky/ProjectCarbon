package cc.funkemunky.carbon.utils.security;

import java.util.Base64;

public class GeneralUtils {

    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String bytesToString(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] bytesFromString(String bytesString) {
        return Base64.getDecoder().decode(bytesString);
    }
}
