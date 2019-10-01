package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.security.GeneralUtils;
import cc.funkemunky.carbon.utils.security.encryption.AES;
import cc.funkemunky.carbon.utils.security.hash.Hash;
import cc.funkemunky.carbon.utils.security.hash.HashType;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Database {
    private String name;
    private DatabaseType type;
    private List<StructureSet> databaseValues;

    public Database(String name, DatabaseType type) {
        this.name = name;
        this.type = type;

        databaseValues = new CopyOnWriteArrayList<>();
    }

    public abstract void loadDatabase();

    public abstract void saveDatabase();

    public void inputField(StructureSet sSet) {
        if (containsStructure(sSet)) {
            StructureSet set = getStructureSet(sSet.id);

            databaseValues.remove(set);
        }
        databaseValues.add(sSet);
    }

    public boolean containsStructure(StructureSet sSet) {
        return databaseValues.stream().anyMatch(set -> set.id.equals(sSet.id));
    }

    public StructureSet getStructureSet(String id) {
        return databaseValues.stream().filter(set -> set.id.equals(id)).findFirst().orElse(null);
    }

    public StructureSet createStructureSet(String id, Structure... structures) {
        return new StructureSet(id, Arrays.asList(structures));
    }

    public StructureSet createStructureSet(Structure... structures) {
        return createStructureSet(MiscUtils.randomString(20, false), structures);
    }

    //Getting a StructureSet (if it exists) by the name of the structure.
    public Optional<StructureSet> getFieldByStructure(Structure structure) {
        return databaseValues.stream()
                .filter(set -> set.structures.stream()
                        .anyMatch(struct -> struct.name.equals(structure.name)))
                .findFirst();
    }

    //People can use this to set their own parameters for how they want to get a StructureSet.
    public Optional<StructureSet> getFieldByStructure(Predicate<Structure> predicate) {
        return databaseValues
                .stream()
                .filter(set -> set.structures.stream()
                        .anyMatch(predicate))
                .findFirst();
    }

    //Getting multiple StructureSets (if it exists) by the name of the structure.
    public List<StructureSet> getFieldsByStructure(Structure structure) {
        return databaseValues.stream()
                .filter(set -> set.structures.stream()
                        .anyMatch(struct -> struct.name.equals(structure.name)))
                .collect(Collectors.toList());
    }

    public boolean containsStructure(String id) {
        return databaseValues.stream().anyMatch(set -> set.id.equals(id));
    }

    //People can use this to set their own parameters for how they want to get multiple StructureSets.
    public List<StructureSet> getFieldsByStructure(Predicate<Structure> predicate) {
        return databaseValues.stream()
                .filter(set -> set.structures.stream().anyMatch(predicate))
                .collect(Collectors.toList());
    }
}
