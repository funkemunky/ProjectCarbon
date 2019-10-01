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

    public void createFlatfileDatabase(String name) {
        Database database = new FlatfileDatabase(name);

        databases.put(name, database);
    }

    public void createFlatfileDatabase(String directory, String name) {
        Database database = new FlatfileDatabase(directory, name);

        databases.put(name, database);
    }

    public void createMongoDatabase(String name, Mongo mongo) {
        Database database = new MongoDatabase(name, mongo);

        databases.put(name, database);
    }

    public void createMongoDatabase(String name) {
        Database database = new MongoDatabase(name, mongo);

        databases.put(name, database);
    }

    public void createSQLDatabase(String databaseName, String ip, int port, String username, String password) {
        Database database = new MySQLDatabase(databaseName, ip, username, password, databaseName, port);

        databases.put(databaseName, database);
    }

    public Database getDatabase(String name) {
        return databases.get(name);
    }

    public boolean isDatabase(String name) {
        return databases.containsKey(name);
    }

    public Mongo initMongo(String database, String ip, int port, String username, String password) {
        return this.mongo = new Mongo(ip, port, database, username, password);
    }
}
