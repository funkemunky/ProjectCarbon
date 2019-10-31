package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    public <T> T getField(String key) {
        return (T) objects.get(key);
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
