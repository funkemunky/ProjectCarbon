package cc.funkemunky.carbon;

import cc.funkemunky.carbon.db.Database;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Carbon {

    public static Carbon INSTANCE;

    private Map<String, Database> databases = new ConcurrentHashMap<>();

    public <T extends Database> T getDatabase(String name) {
        return (T) databases.get(name);
    }

    public boolean isDatabase(String name) {
        return databases.containsKey(name);
    }

    public static void init() {
        INSTANCE = new Carbon();
    }
}