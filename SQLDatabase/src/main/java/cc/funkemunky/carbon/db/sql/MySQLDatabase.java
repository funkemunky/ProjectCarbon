package cc.funkemunky.carbon.db.sql;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.Pair;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//This is compatible with 1.2 and 1.2.1. No need to convert anything.
public class MySQLDatabase extends Database {
    @Getter
    private Connection connection;

    private String ip = "localhost", username = "root", password = "password", database;
    private int port = 3306;

    public MySQLDatabase(String name) {
        super(name, DatabaseType.SQL);

        database = name;
        connectIfDisconected();
    }

    public MySQLDatabase(String name, String ip, String username, String password, String database, int port) {
        super(name, DatabaseType.SQL);

        this.ip = ip;
        this.username = username;
        this.password = password;
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
                        .append(" set value=")
                        .append(GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.getField(name))))
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
                            .append("', ")
                            .append(GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.getField(key))))
                            .append(");");
                }
            }

            statement = connection.prepareStatement(toExecute.toString());
            set = statement.executeQuery();
            statement.close();
        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
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
}
