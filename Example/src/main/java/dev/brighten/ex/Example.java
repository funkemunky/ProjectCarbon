package dev.brighten.ex;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.utils.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Example {

    private static FlatfileDatabase database;
    private static ScheduledExecutorService schedular;
    public static void main(String[] args) {
        Carbon.init();

        database = new FlatfileDatabase("test");
        database.loadDatabase();

        schedular = Executors.newSingleThreadScheduledExecutor();

        //It is good practice to run a task repeatedly to save the database.
        //This helps prevent any data loss.
        schedular.scheduleAtFixedRate(database::saveDatabase,
                2, 5, TimeUnit.MINUTES);

        StructureSet set;
        if(database.contains("test-set")) {
            set = database.get("test-set");
            System.out.println("Field: " + set.getField("test-field"));
            set.inputField("test-field", 0);
        } else set = database.createStructure("test-set",
                new Pair<>("test-field", 1)); //This will add the set to database cache.
        database.updateObject(set); //This will insert the set, or update any existing set.

        System.out.println("Field (2): " + set.getField("test-field"));

        //I am only saving the database because I immediately shutdown the program.
        //You should always save on potential crashes or shutdowns.
        database.saveDatabase();
        System.exit(0);;
    }
}
