package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.*;
import cc.funkemunky.carbon.exceptions.InvalidDecryptionKeyException;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
/* NOTE: Do not use the character sequence of :@@@: when using this database system!!!
This character string is used to separate different fields and will corrupt data if you use it.
 */
public class EncryptedFlatfileDatabase extends EncryptedDatabase {
    private FunkeFile file;
    public EncryptedFlatfileDatabase(String name, String decryptionKey, HashType type) throws InvalidDecryptionKeyException {
        super(name, DatabaseType.FLATFILE, decryptionKey, type);

        file = new FunkeFile(System.getProperty("user.home") + "/dbs", name + ".txt");
    }

    public EncryptedFlatfileDatabase(String directory, String name, String decryptionKey, HashType type) throws InvalidDecryptionKeyException {
        super(name, DatabaseType.FLATFILE, decryptionKey, type);

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

                if(name.equals("decryptKey")) continue;

                StructureSet structSet;

                if(containsStructure(id)) {
                    structSet = getStructureSet(id, false);
                    getDatabaseValues().remove(structSet);
                } else structSet = new StructureSet(id);

                byte[] array = GeneralUtils.bytesFromString(objectString);
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
            for (Structure struct : structSet.structures) {
                String object = (String) struct.object;
                file.addLine(structSet.id + ":@@@:" + struct.name + ":@@@:" + object);
            }
        }

        file.write();
    }

    @Override
    public boolean getDecryptionKey(String key) {
        file.readFile();

        for (String line : file.getLines()) {
            String[] splitLine = line.split(":@@@:");

            if(splitLine.length != 3) continue;

            String name = splitLine[1], objectString = splitLine[2];

            if(!name.equals("decryptKey")) continue;

            return hash.hashEqualsKey(objectString, key);
        }
        return false;
    }

    @Override
    public void createDecryptKey(String key) {
        file.readFile();

        int lineCount = 0;
        for (String line : file.getLines()) {
            lineCount++;
            String[] splitLine = line.split(":@@@:");

            if(splitLine.length != 3) continue;

            String name = splitLine[1];

            if(!name.equals("decryptKey")) continue;
            return;
        }

        file.addLine(MiscUtils.randomString(30, false) + ":@@@:decryptKey:@@@:" + hash.hash(key));
        file.write();
    }
}
