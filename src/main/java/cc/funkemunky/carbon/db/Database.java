package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;

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
    public Optional<StructureSet> getFieldByStructure(Structure... structure) {
        return databaseValues.parallelStream()
                .filter(set -> set.structures.stream()
                        .anyMatch(struct -> Arrays.stream(structure).anyMatch(arg -> struct.name.equals(arg.name))))
                .findFirst();
    }

    //People can use this to set their own parameters for how they want to get a StructureSet.
    public Optional<StructureSet> getFieldByStructure(PredicateLogic<Structure>... logicArray) {
        return databaseValues
                .parallelStream()
                .filter(set -> set.structures.stream().anyMatch(struct ->
                        Arrays.stream(logicArray).anyMatch(logic -> {
                            if(logic.type.equals(PredicateLogic.LogicType.OR)) {
                                return Arrays.stream(logic.predicates).anyMatch(predicate -> predicate.test(struct));
                            } else {
                                return Arrays.stream(logic.predicates).allMatch(predicate -> predicate.test(struct));
                            }
                        })))
                .findFirst();
    }

    //Getting multiple StructureSets (if it exists) by the name of the structure.
    public List<StructureSet> getFieldsByStructure(Structure... structure) {
        return databaseValues.parallelStream()
                .filter(set -> set.structures.stream()
                        .anyMatch(struct -> Arrays.stream(structure).anyMatch(arg -> struct.name.equals(arg.name))))
                .collect(Collectors.toList());
    }

    public boolean containsStructure(String id) {
        return databaseValues.stream().anyMatch(set -> set.id.equals(id));
    }

    //People can use this to set their own parameters for how they want to get multiple StructureSets.
    public List<StructureSet> getFieldsByStructure(PredicateLogic<Structure>... logicArray) {
        return databaseValues
                .parallelStream()
                .filter(set -> set.structures.stream().anyMatch(struct ->
                        Arrays.stream(logicArray).anyMatch(logic -> {
                            if(logic.type.equals(PredicateLogic.LogicType.OR)) {
                                return Arrays.stream(logic.predicates).anyMatch(predicate -> predicate.test(struct));
                            } else {
                                return Arrays.stream(logic.predicates).allMatch(predicate -> predicate.test(struct));
                            }
                        })))
                .collect(Collectors.toList());
    }
}
