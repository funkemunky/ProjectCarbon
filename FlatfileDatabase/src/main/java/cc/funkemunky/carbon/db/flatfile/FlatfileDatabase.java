package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.reflection.Reflections;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
/* NOTE: Do not use the character sequence of :@@@: when using this database system!!!
This character string is used to separate different fields and will corrupt data if you use it.
 */
public class FlatfileDatabase extends Database {
    @Setter
    private FunkeFile file;
    private static String userHome = System.getProperty("user.home");

    public FlatfileDatabase(String name) {
        super(name, DatabaseType.FLATFILE);

        file = new FunkeFile(userHome + File.separator + "databases" + File.separator + name + ".txt");
    }

    public FlatfileDatabase(String directory, String name) {
        super(name, DatabaseType.FLATFILE);

        file = new FunkeFile(directory, name + ".txt");
    }

    @Override
    public void loadDatabase() {
        file.readFile();
        //Clearing after read file to prevent data loss of cache if error occurs.
        getDatabaseValues().clear();
        AtomicInteger lineCount = new AtomicInteger();
        file.getLines().parallelStream()
                .filter(line -> !line.startsWith("deleted"))
                .forEach(line -> {
                    lineCount.getAndIncrement();
                    String[] splitLine = line.split(":@@@:");

                    if (splitLine.length >= 4) {

                        String id = splitLine[0], name = splitLine[1],
                                className = splitLine[2], objectString = splitLine[3];
                        long lastUpdate;

                        if (splitLine.length > 4) { //Done so it can update legacy formats.
                            lastUpdate = Long.parseLong(splitLine[4]);
                        } else lastUpdate = 0;

                        if (!Reflections.classExists(className)) {
                            System.out.println("Class \"" + className + "\" does not exist."
                                    + "(line=" + lineCount.get() + ")");
                            return;
                        }

                        StructureSet structSet;

                        if (contains(id)) {
                            structSet = get(id);
                            getDatabaseValues().remove(structSet);
                        } else structSet = new StructureSet(id);

                        byte[] fromString = GeneralUtils.bytesFromString(objectString);

                        Object toInsert = null;
                        try {
                            toInsert = MiscUtils.objectFromBytes(fromString);
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        structSet.inputField(name, lastUpdate, toInsert);

                        updateObject(structSet);
                    } else System.out.println("Line " + lineCount.get() + " is not length of 4.");
                });
    }

    @Override
    public void saveDatabase() {
        file.clear();

        //Adding lines.
        for (StructureSet structSet : getDatabaseValues()) {
            structSet.getObjects().forEach((key, object) -> {
                try {
                    String objectString = GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(object));
                    file.addLine(structSet.id + ":@@@:" + key + ":@@@:" + object.getClass().getName()
                            + ":@@@:" + objectString + ":@@@:" + object.key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        lastLocalSave = System.currentTimeMillis();

        file.write();
    }

    @Override
    public void updateDatabase() {
        file.readFile();

        List<StructureSet> latestUpdates = Collections.synchronizedList(new CopyOnWriteArrayList<>());

        AtomicInteger lineCount = new AtomicInteger();
        file.getLines().parallelStream()
                .filter(line -> !line.startsWith("deleted"))
                .forEach(line -> {
                    lineCount.getAndIncrement();
                    String[] splitLine = line.split(":@@@:");

                    if (splitLine.length >= 4) {

                        String id = splitLine[0], name = splitLine[1],
                                className = splitLine[2], objectString = splitLine[3];
                        long lastUpdate;

                        if (!Reflections.classExists(className)) {
                            System.out.println("Class \"" + className + "\" does not exist."
                                    + "(line=" + lineCount.get() + ")");
                            return;
                        }

                        if (splitLine.length > 4) { //Done so it can update legacy formats.
                            lastUpdate = Long.parseLong(splitLine[4]);
                            StructureSet structSet;

                            if (latestUpdates.stream().anyMatch(set -> set.id.equals(id))) {
                                structSet = latestUpdates.stream().filter(set -> set.id.equals(id))
                                        .findFirst().get();
                                getDatabaseValues().remove(structSet);
                            } else structSet = new StructureSet(id);

                            byte[] fromString = GeneralUtils.bytesFromString(objectString);

                            Object toInsert = null;
                            try {
                                toInsert = MiscUtils.objectFromBytes(fromString);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }

                            structSet.inputField(name, lastUpdate, toInsert);

                            latestUpdates.add(structSet);
                        }
                    } else System.out.println("Line " + lineCount.get() + " is not length of 4.");
                });

        latestUpdates.parallelStream().filter(set -> !contains(set.id)).sequential().forEach(set -> {
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
}