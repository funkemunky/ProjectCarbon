package cc.funkemunky.carbon.db.sql;

import cc.funkemunky.carbon.db.Database;;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.utils.MiscUtils;
import lombok.val;

import java.sql.*;

public class MySQLDatabase extends Database {
    private Connection connection;

    private String ip = "localhost", username = "root", password = "password", database;
    private int port = 3306;

    public MySQLDatabase(String name) {
        super(name, DatabaseType.SQL);

        database = name;
        connectIfDisconected();
    }

    public MySQLDatabase(String name, String ip, String username, String password, int port) {
        super(name, DatabaseType.SQL);

        this.ip = ip;
        this.username = username;
        this.password = password;
        this.port = port;
        database = name;
        connectIfDisconected();
    }

    @Override
    public void loadDatabase() {
        //TODO Load
    }

    @Override
    public void saveDatabase() {
        //TODO save.
    }

    @Override
    public void inputField(String string, Object object) {
        try {
            connectIfDisconected();
            PreparedStatement statement2 = connection.prepareStatement("delete ignore from data where keyVal='" + string + "'");
            statement2.executeUpdate();
            statement2.close();
            PreparedStatement statement = connection.prepareStatement("insert into data (keyVal, value)\nVALUES ('" + string + "', '" + object.getClass().getName() + "-" + object.toString() + "');");

            statement.executeUpdate();
            statement.close();

            getDatabaseValues().put(string, object);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getField(String key) {
        try {
            connectIfDisconected();
            PreparedStatement statement = connection.prepareStatement("select value from data where keyVal='" + key + "';");

            ResultSet set = statement.executeQuery();

            if(set.next()) {
                String value = set.getString("value");

                String[] splitValue = value.split("-");

                Class<?> className = Class.forName(splitValue[0]);
                return MiscUtils.parseObjectFromString(splitValue[1], className);
            }
        } catch(Exception e) {
            e.printStackTrace();;
        }
        return getDatabaseValues().get(key);
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
                    int Result2 = s2.executeUpdate("CREATE TABLE IF NOT EXISTS data (keyVal VARCHAR(64), value VARCHAR(128));");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
