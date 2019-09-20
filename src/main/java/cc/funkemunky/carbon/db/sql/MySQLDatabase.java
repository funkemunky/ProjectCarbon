package cc.funkemunky.carbon.db.sql;

import cc.funkemunky.carbon.db.Database;;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.utils.MiscUtils;
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

            while(set.next()) {
                val key = set.getString("keyVal");
                val value = set.getString("value");

                String[] splitValue = value.split("-");

                Class<?> className = Class.forName(splitValue[0]);

                getDatabaseValues().put(key, MiscUtils.parseObjectFromString(splitValue[1], className));
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
            for (String key : getDatabaseValues().keySet()) {
                Object object = getDatabaseValues().get(key);
                PreparedStatement statement = connection.prepareStatement("insert into " + getName() + " (keyVal, value)\nVALUES ('" + key + "', '" + object.getClass().getName() + "-" + object.toString() + "');");


                statement.executeUpdate();
                statement.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inputField(String key, Object... objects) {
        List<String> stringObjects = getDatabaseValues().getOrDefault(key, new ArrayList<>());
        try {
            for (Object object : objects) {
                byte[] array = MiscUtils.getBytesOfObject(object);

                String stringBytes = MiscUtils.bytesToString(array);
                stringObjects.add(stringBytes);
            }

            getDatabaseValues().put(key, stringObjects);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getField(String key) {
        return getDatabaseValues().getOrDefault(key, null);
    }

    @Override
    public List<Object> getFieldOrDefault(String key, Object... object) {

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
                    int Result2 = s2.executeUpdate("CREATE TABLE IF NOT EXISTS " + getName() +  " (keyVal VARCHAR(64), value VARCHAR(512));");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
