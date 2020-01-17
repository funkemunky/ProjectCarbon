package dev.brighten.db.db;

import lombok.val;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MySQLDatabase extends Database {

    public MySQLDatabase(String name) {
        super(name);
    }


    Connection connection;
    boolean connected;

    @Override
    public void loadMappings() {
        if(!connected) return;

        try {
            val statement = connection.createStatement();

            val result = statement.executeQuery("select * from " + getName());

            while(result.next()) {
                String id = result.getString("id");

                getMappings().add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<StructureSet> get(String... ids) {
        return Arrays.stream(ids)
                .filter(id -> getMappings().contains(id))
                .map(id -> new SQLSet(this, id))
                .collect(Collectors.toList());
    }

    @Override
    public List<StructureSet> get(Predicate<StructureSet> predicate) {
        return getMappings().stream()
                .map(string -> new SQLSet(this, string))
                .collect(Collectors.toList());
    }

    @Override
    public StructureSet create(String id) {
        return new SQLSet(this, id);
    }

    @Override
    public int remove(String... idArray) {
        if(!connected) return 0 ;
        List<String> ids = Arrays.stream(idArray)
                .filter(id -> getMappings().contains(id))
                .collect(Collectors.toList());

        try {
            val statement = connection.prepareStatement("delete from " + getName() + " where "
                    + ids.stream()
                    .map(id -> "id = '" + id + "'")
                    .collect(Collectors.joining(" or ")));

            int amount = statement.executeUpdate();

            statement.close();
            return amount;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int remove(Predicate<StructureSet> predicate) {
        if(!connected) return 0;
        val ids = getMappings().stream().map(id -> new SQLSet(this, id))
                .filter(predicate)
                .map(StructureSet::getId)
                .toArray(String[]::new);

        return remove(ids);
    }

    /*
    Arguments: HOST, PORT, DATABASE, SSL, USERNAME, PASSWORD
    RECOMMENDED YOU PUT SSL AS TRUE
     */
    @Override
    public void connect(String... args) {
        if(args.length == 6) return;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + args[0] + ":" + args[1] + "/" + args[2]
                    + "?autoReconnect=true&useSSL=" + args[3], args[4], args[5]);
            Statement s = connection.createStatement(), s2 = connection.createStatement(), s3 = connection.createStatement();
            int Result = s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + args[2] + ";");
            int Result3 = s3.executeUpdate("USE " + args[2] + ";");
            int Result2 = s2.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName()
                    + " (id VARCHAR(64), name VARCHAR(64), value VARCHAR(512));");

            s.close();
            s2.close();
            s3.close();
            connected = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            connection.close();
            connected = false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
