package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.encryption.AES;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@RequiredArgsConstructor
public class StructureSet {
    public final String id;

    @Getter
    private Map<String, Object> objects = new HashMap<>();

    public void inputField(String key, Object object) {
        objects.put(key, object);
    }

    public void inputEncryptedField(String key, Object object, String password, HashType type) {
        Hash hash = Hash.getHashByType(type);

        String hashed = hash.hash(password);
        try {
            byte[] encryptedBytes = AES.encrypt(MiscUtils.getBytesOfObject(object), password);
            String encryptedString = GeneralUtils.bytesToString(encryptedBytes);

            objects.put(key + ":@:" + type.name(), encryptedString + ":@@:" + hashed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T getField(String key) {
        if(objects.containsKey(key)) {
            Object object = objects.get(key);

            try {
                return (T) object;
            } catch(ClassCastException e) {
                return null;
            }
        }

        return null;
    }

    public int getInteger(String key) {
        if(objects.containsKey(key)) {
            Object object = objects.get(key);

            return (int) object;
        }
        return -1;
    }

    public double getDouble(String key) {
        if(objects.containsKey(key)) {
            Object object = objects.get(key);

            if(object instanceof Integer) {
                return (int) object;
            }

            return (double) object;
        }
        return -1;
    }

    public float getFloat(String key) {
        if(objects.containsKey(key)) {
            Object object = objects.get(key);

            if(object instanceof Integer) {
                return (int) object;
            }

            return (float) object;
        }
        return -1;
    }

    public long getLong(String key) {
        if(objects.containsKey(key)) {
            Object object = objects.get(key);

            return (long) object;
        }
        return -1;
    }

    public <T> T getEncryptedField(String key, String password, HashType type) {
        if(objects.containsKey((key + ":@:" + type.name()))) {
            String string = (String) objects.get((key + ":@:" + type.name()));
            String[] split = string.split(":@@:");
            String encrypted = split[0];
            String hashedPass = split[1];

            Hash hash = Hash.getHashByType(type);

            if(hash.hashEqualsKey(hashedPass, password)) {

                byte[] decrypted = AES.decrypt(GeneralUtils.bytesFromString(encrypted), password);
                try {
                    return (T) MiscUtils.objectFromBytes(decrypted);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    return null;
                }
            } else return null;
        }

        return null;
    }

    public boolean containsKey(String key) {
        return objects.containsKey(key);
    }

    public boolean removeField(String key) {
        if(objects.containsKey(key)) {
            objects.remove(key);
            return true;
        }
        return false;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("id", id);
        for (String key : objects.keySet()) {
            object.put(key, objects.get(key));
        }

        return object;
    }
}
