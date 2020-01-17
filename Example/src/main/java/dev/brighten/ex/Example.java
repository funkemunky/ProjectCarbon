package dev.brighten.ex;

import dev.brighten.db.Carbon;
import dev.brighten.db.db.MySQLDatabase;
import lombok.val;

public class Example {

    private static MySQLDatabase mySQLDatabase;
    public static void main(String[] args) {
        Carbon.setup();


        Carbon.INSTANCE.addDatabase(mySQLDatabase = new MySQLDatabase("test"));

        mySQLDatabase.connect("localhost", "27017", "testDB");
        mySQLDatabase.loadMappings();

        if(mySQLDatabase.contains("testStruct")) {
            val setList = mySQLDatabase.get("testStruct");

            if(setList.size() > 0) {
                val set = setList.get(0);

                if(set.contains("testValue")) {
                    int value = set.getObject("testValue");
                    System.out.println("testVal: " + value);
                } else {
                    set.input("testValue", 123);
                    set.save(mySQLDatabase);

                    System.out.println("Inputted value but set exists.");
                }
            } else {
                System.exit(5);
            }
        } else {
            val struct = mySQLDatabase.create("testStruct");

            struct.input("testValue", 123);
            struct.save(mySQLDatabase);

            System.out.println("Inputted value.");
        }
        System.exit(0);
    }
}
