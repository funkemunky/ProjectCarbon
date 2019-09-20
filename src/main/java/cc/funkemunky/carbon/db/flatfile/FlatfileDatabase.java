package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.Structure;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

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
        int lineCount = 0;
        try {
            for (String line : file.getLines()) {
                lineCount++;
                String[] splitLine = line.split(":@@@:");

                if(splitLine.length != 3) continue;

                String id = splitLine[0], name = splitLine[1], objectString = splitLine[2];

                StructureSet structSet;

                if(containsStructure(id)) {
                    structSet = getStructureSet(id);
                    getDatabaseValues().remove(structSet);
                } else structSet = new StructureSet(id);

                byte[] array = MiscUtils.bytesFromString(objectString);
                Object toInsert = MiscUtils.objectFromBytes(array);

                structSet.addStructure(new Structure(name, toInsert));

                getDatabaseValues().add(structSet);
            }
        } catch(IOException | ClassNotFoundException e) {
            System.out.println("Error on line: " + lineCount);
            e.printStackTrace();
        }
    }

    @Override
    public void saveDatabase() {
        file.clear();

        //Adding lines.
        for (StructureSet structSet : getDatabaseValues()) {
            structSet.structures.forEach(struct -> {
                try {
                    String object = MiscUtils.bytesToString(MiscUtils.getBytesOfObject(struct.object));
                    file.addLine(structSet.id + ":@@@:" + struct.name + ":@@@:" + object);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        file.write();
    }
}
