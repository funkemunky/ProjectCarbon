package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.json.JSONException;
import cc.funkemunky.carbon.utils.json.JSONObject;
import cc.funkemunky.carbon.utils.json.JSONTokener;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
/* NOTE: Do not use the character sequence of :@@@: when using this database system!!!
This character string is used to separate different fields and will corrupt data if you use it.
 */
public class FlatfileDatabase extends Database {

    //We declare this static since it is likely to be unchanging and will improve performance.
    private static String userHome = System.getProperty("user.home");
    private File directory;
    private Map<String, JSONObject> jsonEntries = new ConcurrentHashMap<>();

    public FlatfileDatabase(String name) {
        super(name, DatabaseType.FLATFILE);
        this.directory = new File(userHome + File.separator + "databases" + File.separator + name);

        if(directory.exists()) {
            Arrays.stream(directory.listFiles()).parallel()
                    .filter(file -> file.getName().endsWith(".json"))
                    .forEach(file -> {
                        try {
                            FileReader reader = new FileReader(file);

                            jsonEntries.put(
                                    file.getName().replace(".json", ""),
                                    new JSONObject(new JSONTokener(reader)));

                            reader.close();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    });
        } else directory.mkdirs();
    }

    @Override
    public void loadDatabase() {
        try {
            for (String fileId : jsonEntries.keySet()) {
                JSONObject object = jsonEntries.get(fileId);

                StructureSet set = new StructureSet(object.getString("id"));
                set.inputField("fileId", fileId);
                for (String key : object.keySet()) {
                    if(key.equals("id")) continue;
                    set.inputField(key, object.get(key));
                }

                updateObject(set);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDatabase() {
        try {
            for (StructureSet struct : getDatabaseValues()) {
                String fileId = struct.getField("fileId");

                JSONObject object = jsonEntries.getOrDefault(fileId, new JSONObject());

                object.put("id", struct.id);

                struct.removeField("fileId");

                for (String key : struct.getObjects().keySet()) {
                    object.put(key, struct.getObjects().get(key));
                }

                File file = new File(directory, fileId + ".json");

                if(!file.exists()) {
                    file.createNewFile();
                }

                FileWriter writer = new FileWriter(file);

                writer.write(object.toString());
                writer.close();
            }
        } catch(JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    //WARNING: This will change the formatting of the database. A backup will be saved just in case.
    public void convertFromLegacy(File oldFile) {
        //Saving backup.
        FunkeFile old = new FunkeFile(oldFile);
        FunkeFile file = new FunkeFile(oldFile.getParentFile().getPath(), getName() + "-backup-" + System.currentTimeMillis() + ".txt");
        old.getLines().forEach(file::addLine);
        file.write();
        old.getLines().clear();
        old.write();

        AtomicInteger lineCount = new AtomicInteger();
        file.getLines().forEach(line -> {
            lineCount.getAndIncrement();
            String[] splitLine = line.split(":@@@:");

            if (splitLine.length == 3) {

                String id = splitLine[0], name = splitLine[1], objectString = splitLine[2];

                StructureSet structSet;

                if (contains(id)) {
                    structSet = get(id);
                    getDatabaseValues().remove(structSet);
                } else structSet = new StructureSet(id);

                byte[] array = GeneralUtils.bytesFromString(objectString);
                Object toInsert = null;
                try {
                    toInsert = MiscUtils.objectFromBytes(array);
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error on line: " + lineCount.get());
                    e.printStackTrace();
                }

                structSet.inputField(name, toInsert);

                getDatabaseValues().add(structSet);
            } else System.out.println("Line " + lineCount.get() + " is not length of 3.");
        });

        saveDatabase();

        if(directory.exists()) {
            Arrays.stream(directory.listFiles()).parallel()
                    .filter(file2 -> file2.getName().endsWith(".json"))
                    .forEach(file2 -> {
                        try {
                            FileReader reader = new FileReader(file2);

                            jsonEntries.put(
                                    file2.getName().replace(".json", ""),
                                    new JSONObject(new JSONTokener(reader)));
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    });
        } else directory.mkdirs();
    }
}
