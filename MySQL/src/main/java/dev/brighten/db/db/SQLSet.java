package dev.brighten.db.db;

import dev.brighten.db.utils.MiscUtils;
import dev.brighten.db.utils.security.GeneralUtils;
import lombok.SneakyThrows;
import lombok.val;

import java.util.HashSet;
import java.util.Set;

public class SQLSet extends StructureSet {


    private MySQLDatabase database;

    public SQLSet(MySQLDatabase database, String id) {
        super(id);
        this.database = database;
    }

    @SneakyThrows
    @Override
    public <T> T getObject(String key) {
        val statement = database.connection.createStatement();
        val result = statement.executeQuery("select * from " + database.getName()
                + " where id = \"" + getId() + "\" AND name = \"" + key + "\"");

        while(result.next()) {
            byte[] bits = GeneralUtils.bytesFromString(result.getString("value"));

            Object object = MiscUtils.objectFromBytes(bits);

            result.close();
            statement.close();
            return (T) object;
        }

        return null;
    }

    @Override
    public boolean save(Database database) {
        return false;
    }

    @SneakyThrows
    @Override
    public boolean input(String key, Object object) {
        val contains = contains(key);

        String string = GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(object));

        if(contains) {
            val statement = database.connection.prepareStatement("update " + database.getName()
                    + " value = '" + string + "'");
            int resultInt = statement.executeUpdate();

            statement.close();
            return true;
        } else {
            val statement = database.connection.prepareStatement("insert into " + database.getName()
                    + " (id, name, value) values('" + getId() + "','" + key + "','" + string + "')");
            val result =  statement.executeQuery();

            statement.close();
            result.close();
            return true;
        }

    }

    @SneakyThrows
    @Override
    public boolean contains(String key) {
        val statement = database.connection.prepareStatement("select * from " + database.getName()
                + " where id = \"" + getId() + "\" AND name = \"" + key + "\"");
        val result = statement.executeQuery();

        boolean isNext = result.next();

        statement.close();
        result.close();

        return isNext;
    }

    @Override
    @SneakyThrows
    public Set<String> getKeys() {
        val statement = database.connection.prepareStatement("select * from " + database.getName()
                + " where id = \"" + getId() + "\"");
        val result = statement.executeQuery();

        Set<String> keys = new HashSet<>();
        while(result.next()) {
            keys.add(result.getString("name"));
        }

        result.close();
        statement.close();

        return keys;
    }
}
