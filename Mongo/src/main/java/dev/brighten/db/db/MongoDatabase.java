package dev.brighten.db.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.SneakyThrows;
import lombok.val;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MongoDatabase extends Database {

    public MongoDatabase(String name) {
        super(name);
    }

    private com.mongodb.client.MongoDatabase database;
    MongoCollection<Document> collection;
    private MongoClient client;
    private boolean connected;

    @Override
    public void loadMappings() {
        collection.find(Filters.exists("id")).forEach((Consumer<? super Document>) document -> {
            getMappings().add(document.getString("id"));
        });
    }

    @Override
    public List<StructureSet> get(boolean parallel, String... id) {
        //Synchronization is for parallel. Shouldn't affect performance for access with sequential streams.
        val docList = Collections.synchronizedList(getDocsToArray());

        Set<String> ids = Collections.synchronizedSet(Arrays.stream(id).collect(Collectors.toSet()));

        val toReturn = (parallel ? docList.parallelStream(): docList.stream())
                .filter(doc -> ids.contains(doc.getString("id")))
                .map(doc -> (StructureSet) new MongoSet(doc)) //Casting since the abstract method is StructureSet.
                .collect(Collectors.toList());

        //It's good practice to clean up after yourself instead of waiting for the JVM to garbage collect.
        ids.clear();
        docList.clear();

        return toReturn;
    }

    @Override
    public List<StructureSet> get(String... ids) {
        return get(false, ids);
    }

    @Override
    public List<StructureSet> get(boolean parallel, Predicate<StructureSet> predicate) {
        return (parallel
                //Sync may be unnecessary but should help with preventing corruption/errors.
                ? Collections.synchronizedList(getDocsToArray()).parallelStream()
                : getDocsToArray().stream())
                .map(MongoSet::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<StructureSet> get(Predicate<StructureSet> predicate) {
        return get(false, predicate);
    }

    @Override
    public StructureSet create(String id) {
        getMappings().add(id);
        return new MongoSet(id);
    }

    @Override
    public int remove(String... id) {

        long count = 0;
        for (String arg : id) {
            val result = collection.deleteMany(Filters.eq("id", arg));

            count+= result.getDeletedCount();
        }

        return (int) count;
    }

    @Override
    public int remove(Predicate<StructureSet> predicate) {
        AtomicLong deleted = new AtomicLong(0);
        collection.find(Filters.exists("id")).into(new ArrayList<>()).stream()
                .map(MongoSet::new)
                .filter(predicate)
                .forEach(set -> {
                    val result = collection.deleteMany(Filters.eq("id", set.getId()));

                    deleted.addAndGet(result.getDeletedCount());
                });

        return deleted.intValue();
    }

    /*
    Argument Options:
    One arg: Mongo URL string.
    Many args: IP, PORT, DATABASE, USERNAME, PASSWORD, AUTHDB
    Many args: IP, PORT, DATABASE, USERNAME, PASSWORD
    Many Args no Auth: IP, PORT, DATABASE
     */
    @SneakyThrows
    @Override
    public void connect(String... args) {
        if(connected) return;
        if(args.length == 1) {
           val uri = new MongoClientURI(args[0]);

           client = MongoClients.create(args[0]);
           this.database = client.getDatabase(uri.getDatabase());
           collection = database.getCollection(getName());

           connected = true;
        } else if(args.length >= 5) {
            this.client = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(
                                            Collections
                                                    .singletonList(
                                                            new ServerAddress(args[0], Integer.parseInt(args[1])))))
                            .credential(MongoCredential.createCredential(args[3], args[(args.length == 6 ? 5 : 2)], args[4].toCharArray()))
                            .build());

            this.database = client.getDatabase(args[2]);
            collection = database.getCollection(getName());
            connected = true;
        } else if(args.length == 3) {
            this.client = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(
                                            Collections
                                                    .singletonList(
                                                            new ServerAddress(args[0], Integer.parseInt(args[1])))))
                            .build());
            this.database = client.getDatabase(args[2]);
            if(!database.listCollectionNames().into(new ArrayList<>()).contains(getName())) {
                System.out.println("Creating collection " + getName() + "...");
                database.createCollection(getName());
            }
            collection = database.getCollection(getName());
            connected = true;
        } else throw new Exception("Argument length must either be 1, 3, or 5. Please reference documentation." +
                " (args=" + args.length + ")");
    }

    @Override
    public void disconnect() {
        getMappings().clear();
        collection = null;
        client.close();
        connected = false;
    }

    private List<Document> getDocsToArray() {
        if(!connected) return new ArrayList<>();
        return collection.find(Filters.exists("id"))
                .into(new ArrayList<>());
    }
}
