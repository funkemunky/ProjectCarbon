package dev.brighten.db.db;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import lombok.val;
import org.bson.Document;

import java.util.Set;

public class MongoSet extends StructureSet {

    private Document document;

    public MongoSet(Document document) {
        super(document.getString("id"));

        this.document = document;
    }

    public MongoSet(String name) {
        super(name);

        document = new Document();
    }

    @Override
    public <T> T getObject(String key) {
        return (T) document.get(key);
    }

    @Override
    public boolean save(Database database) {
        MongoDatabase parent = (MongoDatabase) database;
        DeleteResult result = parent.collection.deleteMany(Filters.eq("id", getId()));
        parent.collection.insertOne(document);
        database.getMappings().add(getId());
        return result.getDeletedCount() > 0;
    }

    @Override
    public boolean input(String key, Object object) {
        boolean contains = contains(key);

        document.put(key, object);
        return contains;
    }

    @Override
    public boolean contains(String key) {
        return document.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return document.keySet();
    }
}
