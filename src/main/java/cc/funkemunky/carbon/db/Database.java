package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.utils.MiscUtils;
import cc.funkemunky.carbon.utils.Pair;
import cc.funkemunky.carbon.utils.json.JSONObject;
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

    public StructureSet createStructure(String name, Pair<String, Object>... pairs) {
        StructureSet set = new StructureSet(name);

        for (Pair<String, Object> pair : pairs) {
            set.inputField(pair.key, pair.value);
        }

        databaseValues.add(set);

        return set;
    }

    public StructureSet updateObject(StructureSet set) {
        if(contains(set.id)) {
            databaseValues.remove(get(name));
            databaseValues.add(set);
        }
        return set;
    }

    public StructureSet get(String id) {
        Optional<StructureSet> getSet = databaseValues
                .stream().filter(set -> set.id.equals(id)).findFirst();

        return null;
    }

    public boolean contains(String id) {
        return databaseValues
                .stream().anyMatch(set -> set.id.equals(id));
    }

    public boolean remove(String id) {
        List<StructureSet> sets = databaseValues.stream()
                .filter(set -> set.id.equals(id))
                .collect(Collectors.toList());

        if(sets.size() > 0) {
            for (StructureSet set : sets) {
                databaseValues.remove(set);
            }
            return true;
        }
        return false;
    }
}
