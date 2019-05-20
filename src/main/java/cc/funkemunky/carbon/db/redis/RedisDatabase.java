package cc.funkemunky.carbon.db.redis;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;

//TODO: Create redis database code.
public class RedisDatabase extends Database {

    public RedisDatabase(String name) {
        super(name, DatabaseType.REDIS);
    }

    @Override
    public void loadDatabase() {

    }

    @Override
    public void saveDatabase() {

    }

    @Override
    public void inputField(String string, Object object) {

    }

    @Override
    public Object getField(String key) {
        return null;
    }
}
