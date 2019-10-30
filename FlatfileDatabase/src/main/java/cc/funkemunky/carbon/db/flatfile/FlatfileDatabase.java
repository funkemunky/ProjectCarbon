package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
/* NOTE: Do not use the character sequence of :@@@: when using this database system!!!
This character string is used to separate different fields and will corrupt data if you use it.
 */
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
        //Clearing after read file to prevent data loss of cache if error occurs.
        getDatabaseValues().clear();
        AtomicInteger lineCount = new AtomicInteger();
        file.getLines().stream().forEach(line -> {
            lineCount.getAndIncrement();
            String[] splitLine = line.split(":@@@:");

            if(splitLine.length == 3) {

                String id = splitLine[0], name = splitLine[1], objectString = splitLine[2];

                StructureSet structSet;

                if (containsStructure(id)) {
                    structSet = getStructureSet(id);
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

                structSet.addStructure(new Structure(name, toInsert));

                getDatabaseValues().add(structSet);
            } else System.out.println("Line " + lineCount.get() + " is not length of 3.");
        });
    }

    @Override
    public void saveDatabase() {
        file.clear();

        //Adding lines.
        for (StructureSet structSet : getDatabaseValues()) {
            structSet.structures.forEach(struct -> {
                try {
                    String object = GeneralUtils.bytesToString(MiscUtils.getBytesOfObject(struct.object));
                    file.addLine(structSet.id + ":@@@:" + struct.name + ":@@@:" + object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        file.write();
    }
}
