package cc.funkemunky.carbon;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.db.mongo.Mongo;
import cc.funkemunky.carbon.db.mongo.MongoDatabase;
import cc.funkemunky.carbon.db.sql.MySQLDatabase;
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

    public void createSQLDatabase(String name, String ip, String username, String password, int port) {
        Database database = new MySQLDatabase(name, ip, username, password, port);

        databases.put(name, database);
    }

    public Database getDatabase(String name) {
        return databases.get(name);
    }

    public boolean isDatabase(String name) {
        return databases.containsKey(name);
    }

    public Mongo initMongo(String ip, int port, String database, String username, String password) {
        return this.mongo = new Mongo(ip, port, database, username, password);
    }
}
