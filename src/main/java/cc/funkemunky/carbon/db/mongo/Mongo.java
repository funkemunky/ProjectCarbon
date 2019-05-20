package cc.funkemunky.carbon.db.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

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
        connect();
    }

    public void connect() {
       if(enabled) {
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
    }

    public void disconnect() {
        client.close();
    }

}
