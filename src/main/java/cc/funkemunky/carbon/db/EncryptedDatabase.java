package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.exceptions.InvalidDecryptionKeyException;
import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.encryption.AES;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class EncryptedDatabase {

    public Hash hash;
    public String decryptionKey;

    private String name;
    private DatabaseType type;
    private List<StructureSet> databaseValues;

    public EncryptedDatabase(String name, DatabaseType type, String decryptionKey, HashType hashType)
            throws InvalidDecryptionKeyException {
        this.name = name;
        this.type = type;

        this.hash = Hash.getHashByType(hashType);

        createDecryptKey(decryptionKey);
        if(getDecryptionKey(decryptionKey)) {
            this.decryptionKey = decryptionKey;
        } else throw new InvalidDecryptionKeyException(decryptionKey);

        databaseValues = new CopyOnWriteArrayList<>();
    }

    public abstract void loadDatabase();

    public abstract void saveDatabase();

    public abstract boolean getDecryptionKey(String key);

    public abstract void createDecryptKey(String key);

    public void inputField(StructureSet sSet) {
        if (containsStructure(sSet)) {
            StructureSet set = getStructureSet(sSet.id, false);

            databaseValues.remove(set);
        }

        try {
            for (Structure structure : sSet.structures) {
                encryptObject(structure, decryptionKey);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        databaseValues.add(sSet);
    }

    public boolean containsStructure(StructureSet sSet) {
        return databaseValues.stream().anyMatch(set -> set.id.equals(sSet.id));
    }

    public StructureSet getStructureSet(String id, boolean parallel) {
        Optional<StructureSet> optional = (parallel ? databaseValues.stream() : databaseValues.parallelStream())
                .filter(set -> set.id.equals(id)).findFirst();

        if(optional.isPresent()) {
            StructureSet set = optional.get();
            try {
                for (Structure struct : set.structures) {
                    decryptObject(struct.object, decryptionKey);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return set;
        } else throw new NullPointerException("Structure set with ID \"" + id
                + "\" is not present in database \"" + name + "\".");
    }

    public StructureSet createStructureSet(String id, Structure... structures) {
        return new StructureSet(id, Arrays.stream(structures)
                .map(struct -> {
                    try {
                        struct.object= encryptObject(struct, decryptionKey);
                        return struct;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public StructureSet createStructureSet(Structure... structures) {
        return createStructureSet(MiscUtils.randomString(20, false), structures);
    }

    //Getting a StructureSet (if it exists) by the name of the structure.
    public StructureSet getFieldByStructure(Structure structure, boolean parallel, boolean encrypted) {
        if(!encrypted) {
            try {
                structure.object = encryptObject(structure, decryptionKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        val optional = (parallel ? databaseValues.stream() : databaseValues.parallelStream())
                .filter(set -> set.structures.stream()
                        .anyMatch(struct ->
                                struct.name.equals(structure.name)
                                        && struct.object.equals(structure.object)))
                .findFirst();

        if(optional.isPresent()) {
            return optional.get();
        } else throw new NullPointerException("Structure set with structure \"" + structure.name
                + "\" is not present in database \"" + name + "\".");
    }

    //People can use this to set their own parameters for how they want to get a StructureSet.
    public Optional<StructureSet> getFieldByStructure(Predicate<Structure> predicate,
                                                      boolean parallel, boolean encrypted) {
        return (parallel ? databaseValues.stream() : databaseValues.parallelStream())
                .filter(set -> set.structures
                        .stream()
                        .peek(structure -> {
                            if(!encrypted) {
                                try {
                                    structure.object = encryptObject(structure, decryptionKey);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .anyMatch(predicate))
                .findFirst();
    }

    //Getting multiple StructureSets (if it exists) by the name of the structure.
    public List<StructureSet> getFieldsByStructure(Structure structure, boolean parallel, boolean encrypted) {
        if(!encrypted) {
            try {
                structure.object = encryptObject(structure, decryptionKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (parallel ? databaseValues.stream() : databaseValues.parallelStream())
                .filter(set -> set.structures.stream()
                        .anyMatch(struct -> struct.name.equals(structure.name)))
                .collect(Collectors.toList());
    }

    public boolean containsStructure(String id) {
        return databaseValues.stream().anyMatch(set -> set.id.equals(id));
    }

    //People can use this to set their own parameters for how they want to get multiple StructureSets.
    public List<StructureSet> getFieldsByStructure(Predicate<Structure> predicate, boolean parallel, boolean encrypted) {
        return (parallel ? databaseValues.stream() : databaseValues.parallelStream())
                .filter(set -> set.structures
                        .stream()
                        .peek(structure -> {
                            if(!encrypted) {
                                try {
                                    structure.object = encryptObject(structure, decryptionKey);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .anyMatch(predicate))
                .collect(Collectors.toList());
    }

    private String encryptObject(Structure struct, String key) throws IOException {
        byte[] bytes = MiscUtils.getBytesOfObject(struct.object);

        return GeneralUtils.bytesToString(AES.encrypt(bytes, key));
    }

    private Object decryptObject(Object object, String key) throws IOException, ClassNotFoundException {
        if(object instanceof String) {
            String toDecrypt = (String) object;

            return MiscUtils.objectFromBytes(AES.decrypt(GeneralUtils.bytesFromString(toDecrypt), key));
        } else throw new ClassCastException("The object inputted is not a String");
    }
}
