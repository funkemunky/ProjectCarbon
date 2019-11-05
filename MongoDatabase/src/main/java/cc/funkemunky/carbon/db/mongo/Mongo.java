package cc.funkemunky.carbon.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Objects;

@Getter
@Setter
public class Mongo {

    private MongoDatabase mongoDatabase;
    private MongoClient client;

    private String database = "Carbon", ip = "127.0.0.1", username = "username", password = "password";
    private int port = 27017;
    private boolean enabled = false, connected = false;

    public Mongo(String ip, int port, String database, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        enabled = true;
        connect();
    }

    public Mongo(String ip, int port, String database) {
        this.ip = ip;
        this.port = port;
        this.database = database;
        connect();
    }

    public Mongo(String connectionString) {
        connect(connectionString);
    }

    public void connect() {
       try {
            this.client = new MongoClient(ip, port);
            if(enabled) {
                val credential = MongoCredential.createCredential(username, database, password.toCharArray());
                client.getCredentialsList().add(credential);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not connect to the database!");
            this.connected = false;
            return;
        }
        System.out.println("Connected to Mongo database with IP " + ip + " and name " + database + ".");
        this.mongoDatabase = client.getDatabase(database);
        this.connected = true;
    }

    public void connect(String string) {
        MongoClientURI uri = new MongoClientURI(string);

        this.client = new MongoClient(uri);
        this.mongoDatabase = client.getDatabase(this.database = uri.getDatabase());
        this.connected = true;
        this.username = uri.getUsername();
        this.password = String.copyValueOf(Objects.requireNonNull(uri.getPassword()));
    }

    public void disconnect() {
        client.close();
    }

}
