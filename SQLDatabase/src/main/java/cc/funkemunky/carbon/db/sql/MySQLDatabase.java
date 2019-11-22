package cc.funkemunky.carbon.db.sql;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.Pair;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;
import lombok.val;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

//This is compatible with 1.2 and 1.2.1. No need to convert anything.
public class MySQLDatabase extends Database {
    @Getter
    private Connection connection;

    protected static String ip = "localhost", username = "root", password = "password";
    private String database;
    private int port = 3306;

    public MySQLDatabase(String name) {
        super(name, DatabaseType.SQL);

        database = name;
        connectIfDisconected();
    }

    public MySQLDatabase(String name, String database, int port) {
        super(name, DatabaseType.SQL);

        this.port = port;
        this.database = database;
        connectIfDisconected();
    }

    @Override
    public void loadDatabase() {
        try {
            connectIfDisconected();
            PreparedStatement statement = connection.prepareStatement("select * from " + getName());
            ResultSet set = statement.executeQuery();

            //We clear here instead of beginning in case there's something wrong grabbing values. If an error occurs
            //grabbing everything, it won't clear the values cached in the list without saving, causing data loss.
            while(set.next()) {
                String id = set.getString("id");
                String name = set.getString("name");
                String value = set.getString("value");

                StructureSet structSet;

                if(contains(id)) {
                    structSet = get(id);
                } else {
                    structSet = new StructureSet(id);
                }

                Object toInsert;

                long update;

                if(value.contains(":@@@:")) {
                    String[] split = value.split(":@@@:");
                    value = split[0];
                    update = Long.parseLong(split[1]);
                } else update = 0;

                byte[] array = GeneralUtils.bytesFromString(value);
                toInsert = MiscUtils.objectFromBytes(array);

                structSet.inputField(name, toInsert);

                updateObject(structSet);
            }
            statement.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDatabase() {
        connectIfDisconected();

        //Pair<Id, Name>
        try {
            List<Pair<String, String>> itemsToUpdate = new ArrayList<>();

            PreparedStatement statement = connection.prepareStatement("select * from " + getName());
            ResultSet set = statement.executeQuery();

            while(set.next()) {
                String id = set.getString("id");
                String name = set.getString("name");

                itemsToUpdate.add(new Pair<>(id, name));
            }
            statement.close();
            StringBuilder toExecute = new StringBuilder();

            for (Pair<String, String> updatePair : itemsToUpdate) {
                String id = updatePair.key;
                String name = updatePair.value;
                StructureSet struct = get(id);

                toExecute.append("update ")
                        .append(getName())
                        .append(" set value=").append(GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.getField(name))))
                        .append(":@@@:").append(struct.getObjects().get(name).key)
                        .append(" where id = '")
                        .append(id)
                        .append("' and name = '")
                        .append(name)
                        .append("';");
            }
            statement = connection.prepareStatement(toExecute.toString());
            set = statement.executeQuery();
            statement.close();

            toExecute = new StringBuilder();

            for (StructureSet struct : getDatabaseValues()) {
                List<String> toUpdate = struct.getObjects().keySet()
                        .parallelStream() //We use a parallelStream since this can take some time if done linearly.
                        .filter(key -> itemsToUpdate.stream().noneMatch(pair -> pair.value.equals(key)))
                        .collect(Collectors.toList());

                for (String key : toUpdate) {
                    toExecute.append("insert into ")
                            .append(getName())
                            .append(" (id, name, values\nVALUES ")
                            .append(" ('")
                            .append(struct.id)
                            .append("', '")
                            .append(key)
                            .append("', '")
                            .append(GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.getField(key))))
                            .append(":@@@:").append(struct.getObjects().get(key).key)
                            .append("');");
                }
            }

            statement = connection.prepareStatement(toExecute.toString());
            set = statement.executeQuery();
            statement.close();
        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
     }

    @Override
    public void updateDatabase() {
        List<StructureSet> latestUpdates = Collections.synchronizedList(new CopyOnWriteArrayList<>());

        try {
            connectIfDisconected();
            PreparedStatement statement = connection.prepareStatement("select * from " + getName());
            ResultSet set = statement.executeQuery();

            //We clear here instead of beginning in case there's something wrong grabbing values. If an error occurs
            //grabbing everything, it won't clear the values cached in the list without saving, causing data loss.
            while(set.next()) {
                String id = set.getString("id");
                String name = set.getString("name");
                String value = set.getString("value");

                StructureSet structSet;

                if(latestUpdates.stream().anyMatch(sset -> sset.id.equals(id))) {
                    structSet = latestUpdates.stream().filter(sset -> sset.id.equals(id)).findFirst().get();
                } else {
                    structSet = new StructureSet(id);
                }

                Object toInsert;

                if(value.contains(":@@@:")) {
                    String[] split = value.split(":@@@:");
                    value = split[0];
                    long update = Long.parseLong(split[1]);
                    byte[] array = GeneralUtils.bytesFromString(value);
                    toInsert = MiscUtils.objectFromBytes(array);

                    structSet.inputField(name, toInsert);
                    latestUpdates.add(structSet);
                }
            }
            statement.close();
        } catch(SQLException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

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

    private void connectIfDisconected() {
        try {
            if(connection == null || connection.isClosed()) {
                try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + "?characterEncoding=utf8&user=" + username + "&password=" + password);
                    Statement s = connection.createStatement(), s2 = connection.createStatement(), s3 = connection.createStatement();

                    int Result = s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + database + ";");
                    int Result3 = s3.executeUpdate("USE " + database + ";");
                    int Result2 = s2.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName() +  " (id VARCHAR(64), name VARCHAR(64), value VARCHAR(512));");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setCredentials(String ip, String username, String password) {
        MySQLDatabase.ip = ip;
        MySQLDatabase.password = password;
        MySQLDatabase.username = username;
    }
}
