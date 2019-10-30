package cc.funkemunky.carbon;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.db.mongo.Mongo;
import cc.funkemunky.carbon.db.mongo.MongoDatabase;
import cc.funkemunky.carbon.db.sql.MySQLDatabase;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Carbon {

    private Mongo mongo;
    public static Carbon INSTANCE;

    private Map<String, Database> databases = new ConcurrentHashMap<>();

    public Carbon() {
        INSTANCE = this;
        Hash.loadHashes();
    }

    public Database getDatabase(String name) {
        return databases.get(name);
    }

    public boolean isDatabase(String name) {
        return databases.containsKey(name);
    }
}
