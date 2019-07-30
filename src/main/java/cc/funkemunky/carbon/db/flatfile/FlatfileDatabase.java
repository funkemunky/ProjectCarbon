package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlatfileDatabase extends Database {
    private FunkeFile file;
    public FlatfileDatabase(String name) {
        super(name, DatabaseType.FLATFILE);

        file = new FunkeFile(System.getProperty("user.home") + "/dbs", name + ".txt");
    }

    public FlatfileDatabase(String directory, String name) {
        super(name, DatabaseType.FLATFILE);

        file = new FunkeFile(directory, name + ".txt");
    }

    @Override
    public void loadDatabase() {
        file.readFile();
        file.getLines().parallelStream().forEach(line -> {
            String[] info = line.split(":@@@:");

            if(info.length >= 3) {
                String key = info[0];
                StringBuilder valueString = new StringBuilder();

                for (int i = 2; i < info.length; i++) {
                    valueString.append(info[i]).append(":");
                }

                valueString.deleteCharAt(valueString.length() - 1);

                try {
                    Class<?> classObject = Class.forName(info[1]);
                    Object value = classObject.getSimpleName().equals("String") ? valueString.toString() : MiscUtils.parseObjectFromString(valueString.toString(), classObject);
                    getDatabaseValues().put(key, value);
                } catch(Exception e) {
                    System.out.println("Error parsing " + key + " value from string!");
                }
            }
        });
    }

    @Override
    public void saveDatabase() {
        file.clear();
        getDatabaseValues().keySet().forEach(key -> {
            Object value = getDatabaseValues().get(key);

            file.addLine(key + ":@@@:" + value.getClass().getName() + ":@@@:" + value.toString());
        });
        file.write();
    }

    @Override
    public void inputField(String string, Object object) {
        getDatabaseValues().put(string, object);
    }

    @Override
    public Object getField(String key) {
        return getDatabaseValues().getOrDefault(key, null);
    }

    @Override
    public Object getFieldOrDefault(String key, Object object) {
        return getDatabaseValues().getOrDefault(key, object);
    }
}
