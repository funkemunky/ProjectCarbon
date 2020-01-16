package dev.brighten.db;

import dev.brighten.db.db.Database;
import lombok.val;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Carbon {

    public static Carbon INSTANCE;
    private static boolean setup = false;

    private Set<Database> databases = new HashSet<>();

    public static void setup() {
        if(setup) return;  //This is to prevent duplicate setups.

        INSTANCE = new Carbon();
        setup = true;
    }

    public void shutdown() {
        databases.clear();
        INSTANCE = null;
        setup = false; //Allowing for future setups if a program is not shutdown.
    }

    public <T extends Database> T getDatabase(String name) throws NullPointerException {
        val optional = (databases.size() > 20 ? databases.parallelStream() : databases.stream())
                .filter(database -> database.getName().equals(name)).findFirst();

        if(optional.isPresent()) {
            return (T) optional.get();
        }

        throw new NullPointerException("Could not find database \"" + name + "\".");
    }

    public void addDatabase(Database database) {
        database.connect();
        this.databases.add(database);
    }

    public void unregisterDatabase(Database database) {
        database.disconnect();
        this.databases.remove(database);
    }

    public void unregisterDatabase(String name) {
        databases.stream()
                .filter(db -> db.getName().equals(name))
                .forEach(this::unregisterDatabase);
    }
}
