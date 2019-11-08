package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.Database;
import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.reflection.Reflections;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import com.sun.jna.Structure;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
/* NOTE: Do not use the character sequence of :@@@: when using this database system!!!
This character string is used to separate different fields and will corrupt data if you use it.
 */
public class FlatfileDatabase extends Database {
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
        file.getLines().stream().forEach(line -> {
            lineCount.getAndIncrement();
            String[] splitLine = line.split(":@@@:");

            if(splitLine.length == 4) {

                String id = splitLine[0], name = splitLine[1], className = splitLine[2], objectString = splitLine[3];

                if(!Reflections.classExists(className)) {
                    System.out.println("Class \"" + className + "\" does not exist. (line=" + lineCount.get() + ")");
                    return;
                }

                StructureSet structSet;

                if (contains(id)) {
                    structSet = get(id);
                    getDatabaseValues().remove(structSet);
                } else structSet = new StructureSet(id);

                Object toInsert = MiscUtils.parseObjectFromString(objectString, Reflections.getClass(className));

                structSet.inputField(name, toInsert);

                getDatabaseValues().add(structSet);
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
                    file.addLine(structSet.id + ":@@@:" + key + ":@@@:" + object.getClass().getName() + ":@@@:" + objectString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        file.write();
    }
}