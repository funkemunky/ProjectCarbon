package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
public class StructureSet {
    public final String id;

    protected JSONObject json = new JSONObject();

    public void inputField(String key, Object object) {
        try {
            json.put(key, object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public <T> T getField(String key) {
        try {
            return (T) json.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("Object does not contain key \"" + key + "\"");
    }

    protected String toJSON() {
        return json.toString();
    }
}
