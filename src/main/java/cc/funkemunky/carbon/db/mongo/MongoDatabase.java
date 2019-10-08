package cc.funkemunky.carbon.db.mongo;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.Structure;
import cc.funkemunky.carbon.db.StructureSet;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

public class MongoDatabase extends Database {
    private MongoCollection<Document> collection;

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
        getDatabaseValues().clear();
        collection.find().forEach((Block<? super Document>) doc -> {
            if(doc.containsKey("id")) {
                StructureSet set = new StructureSet(doc.getString("id"));

                doc.keySet().stream()
                        .filter(key -> !key.equals("id"))
                        .forEach(key -> set.addStructure(new Structure(key, doc.get(key))));

                getDatabaseValues().add(set);
            }
        });
    }

    @Override
    public void saveDatabase() {
        for (StructureSet structSet : getDatabaseValues()) {
            Document document = new Document("id", structSet.id);
            structSet.structures.forEach(struct -> document.put(struct.name, struct.object));

            collection.deleteMany(Filters.eq("id", structSet.id));
            collection.insertOne(document);
        }
    }
}
