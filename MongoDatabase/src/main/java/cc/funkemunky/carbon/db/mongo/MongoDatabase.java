package cc.funkemunky.carbon.db.mongo;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sun.jna.Structure;
import org.bson.Document;

import java.util.Set;

public class MongoDatabase extends Database {
    private MongoCollection<Document> collection;

    public static Mongo mongo;

    public MongoDatabase(String name) {
        super(name, DatabaseType.MONGO);

        collection = Carbon.INSTANCE.getMongo().getMongoDatabase().getCollection(name);
    }

    public MongoDatabase(String name, Mongo mongo) {
        super(name, DatabaseType.MONGO);

        collection = mongo.getMongoDatabase().getCollection(name);
    }

    @Override
    public void loadDatabase() {
        collection.find(Filters.exists("version", true))
                .filter(Filters.eq("version", "2.0"))
                .forEach((Block<? super Document>)doc -> {
                    StructureSet set = new StructureSet(doc.getString("id"));

                    Set<String> keys = doc.keySet();
                    keys.remove("id");

                    for (String key : keys) {
                        set.inputField(key, doc.get(key));
                    }

                    updateObject(set);
                });
    }

    @Override
    public void saveDatabase() {
        for (StructureSet structSet : getDatabaseValues()) {
            Document document = new Document("id", structSet.id);

            document.put("version", "2.0");

        }
    }

    public static Mongo initMongo(String database, String ip, int port, String username, String password) {
        return mongo = new Mongo(ip, port, database, username, password);
    }
}
