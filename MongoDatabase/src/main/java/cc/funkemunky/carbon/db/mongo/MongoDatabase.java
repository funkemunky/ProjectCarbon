package cc.funkemunky.carbon.db.mongo;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import lombok.val;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//Compatible with 1.2 and 1.2.1.
public class MongoDatabase extends Database {
    private MongoCollection<Document> collection;

    public static Mongo mongo;
    private List<StructureSet> removedSets = Collections.synchronizedList(new ArrayList<>());

    public MongoDatabase(String name) {
        super(name, DatabaseType.MONGO);

        collection = mongo.getMongoDatabase().getCollection(name);
    }

    public MongoDatabase(String name, Mongo mongo) {
        super(name, DatabaseType.MONGO);

        collection = mongo.getMongoDatabase().getCollection(name);
    }

    @Override
    public void loadDatabase() {
        super.loadDatabase();
        collection.find()
                .forEach((Consumer<? super Document>) doc -> {
                    StructureSet set = new StructureSet(doc.getString("id"));

                    Set<String> keys = doc.keySet();
                    keys.remove("id");

                    if(doc.containsKey("update")) {
                        set.lastUpdate = doc.getLong("update");
                    } else set.lastUpdate = lastLoad;

                    for (String key : keys) {
                        doc.get(key);
                        long lastUpdate;
                        if (doc.containsKey(key + "-update")) {
                            lastUpdate = doc.getLong(key + "-update");
                        } else lastUpdate = 0;
                        set.inputField(key, lastUpdate, doc.get(key));
                    }

                    updateObject(set);
                });
    }

    @Override
    public void saveDatabase() {
        if(removedSets.size() > 0) {
            removedSets.parallelStream()
                    .forEach(set -> collection.deleteMany(Filters.eq("id", set.id)));
            removedSets.clear();
        }
        getDatabaseValues().parallelStream().filter(set -> set.lastRemove > lastLoad || set.lastUpdate > lastLoad).forEach(structSet -> {
            Document document = new Document("id", structSet.id);

            document.put("version", "2.0");
            document.put("update", structSet.lastUpdate);

            structSet.getObjects().forEach((key, pair) -> {
                document.put(key, structSet.getObjects().get(key).value);
                document.put(key + "-update", pair.key);
            });

            collection.deleteMany(Filters.eq("id", structSet.id));
            collection.insertOne(document);
        });
    }

    @Override
    public void updateDatabase() {
        List<StructureSet> latestUpdates = Collections.synchronizedList(new CopyOnWriteArrayList<>());

        collection.find()
                .forEach((Consumer<? super Document>) doc -> {
                    StructureSet set = new StructureSet(doc.getString("id"));

                    Set<String> keys = doc.keySet();
                    keys.remove("id");

                    for (String key : keys) {
                        doc.get(key);
                        long lastUpdate;
                        if (doc.containsKey(key + "-update")) {
                            lastUpdate = doc.getLong(key + "-update");
                            set.inputField(key, lastUpdate, doc.get(key));
                        }
                    }

                    latestUpdates.add(set);
                });

        latestUpdates.parallelStream()
                .filter(set -> !contains(set.id))
                .forEach(set -> {
                    updateObject(set);
                    latestUpdates.remove(set);
                });

        latestUpdates.parallelStream().forEach(set -> {
            StructureSet oldSet = get(set.id);

            oldSet.getObjects().forEach((key, pair) -> {
                val updated = set.getObjects().get(key);

                if(updated.key > pair.key) {
                    oldSet.inputField(key, updated.key, updated.value);
                }
            });
            latestUpdates.remove(set);
        });
    }

    //This is required for Mongo databases to work.

    public static Mongo initMongo(String database, String authDB, String ip, int port, String username, String password) {
        return mongo = new Mongo(ip, port, database, authDB, username, password);
    }

    public static Mongo initMongo(String database, String ip, int port, String username, String password) {
        return mongo = new Mongo(ip, port, database, username, password);
    }

    public static Mongo initMongo(String database, String ip, int port) {
        return mongo = new Mongo(ip, port, database);
    }

    public synchronized boolean remove(String id) {
        List<StructureSet> sets = getDatabaseValues().parallelStream()
                .filter(set -> set.id.equals(id))
                .collect(Collectors.toList());

        if(sets.size() > 0) {
            for (StructureSet set : sets) {
                removedSets.add(set);
                getDatabaseValues().remove(set);
            }
            return true;
        }
        return false;
    }
}
