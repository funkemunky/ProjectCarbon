package cc.funkemunky.carbon.db.sql;

import cc.funkemunky.carbon.db.Database;;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.Structure;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;
import lombok.val;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
            getDatabaseValues().clear();
            while(set.next()) {
                String id = set.getString("id");
                String name = set.getString("name");
                String value = set.getString("value");

                StructureSet structSet;

                if(containsStructure(id)) {
                    structSet = getStructureSet(id);
                    getDatabaseValues().remove(structSet);
                } else structSet = new StructureSet(id);

                Object toInsert;

                byte[] array = GeneralUtils.bytesFromString(value);
                toInsert = MiscUtils.objectFromBytes(array);

                structSet.addStructure(new Structure(name, toInsert));
                getDatabaseValues().add(structSet);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDatabase() {
        try {
            connectIfDisconected();

            PreparedStatement statement2 = connection.prepareStatement("delete ignore from " + getName());
            statement2.executeUpdate();
            statement2.close();
            for (StructureSet structSet : getDatabaseValues()) {
                //Object object = key.;
                //PreparedStatement statement = connection.prepareStatement("insert into " + getName() + " (keyVal, value)\nVALUES ('" + key + "', '" + object.getClass().getName() + "-" + object.toString() + "');");
                for (Structure struct : structSet.structures) {
                    PreparedStatement statement = connection.prepareStatement("insert into " + getName() + " (id, name, value)\nVALUES ('" + structSet.id + "', '" + struct.name + "', '" + GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.object)) + "');");

                    statement.executeUpdate();
                    statement.close();
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void connectIfDisconected() {
        try {
            if(connection == null || connection.isClosed()) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
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
