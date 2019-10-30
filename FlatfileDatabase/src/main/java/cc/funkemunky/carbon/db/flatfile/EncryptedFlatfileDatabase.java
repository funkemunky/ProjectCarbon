package cc.funkemunky.carbon.db.flatfile;

import cc.funkemunky.carbon.db.DatabaseType;
import cc.funkemunky.carbon.db.EncryptedDatabase;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.exceptions.InvalidDecryptionKeyException;
import cc.funkemunky.carbon.utils.FunkeFile;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import com.sun.jna.Structure;
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

    }

    @Override
    public void saveDatabase() {

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
