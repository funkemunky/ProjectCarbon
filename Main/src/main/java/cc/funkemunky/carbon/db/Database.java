package cc.funkemunky.carbon.db;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.utils.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Database {
    private String name;
    private DatabaseType type;
    private List<StructureSet> databaseValues;
    protected long lastLocalSave, lastGlobalSave;

    public Database(String name, DatabaseType type) {
        this.name = name;
        this.type = type;

        databaseValues = new CopyOnWriteArrayList<>();
        Carbon.INSTANCE.getDatabases().put(name, this);
    }

    public abstract void loadDatabase();

    public abstract void saveDatabase();

    public abstract void updateDatabase();

    public synchronized StructureSet createStructure(String name, Pair<String, Object>... pairs) {
        StructureSet set = new StructureSet(name);

        for (Pair<String, Object> pair : pairs) {
            set.inputField(pair.key, pair.value);
        }

        databaseValues.add(set);

        return set;
    }

    public synchronized StructureSet updateObject(StructureSet set) {
        if(contains(set.id)) {
            databaseValues.remove(get(name));
        }
        databaseValues.add(set);
        return set;
    }

    public synchronized StructureSet get(String id) {
        Optional<StructureSet> getSet = databaseValues
                .parallelStream().filter(set -> set.id.equals(id)).findFirst();

        return getSet.orElse(null);
    }

    public synchronized boolean contains(String id) {
        return databaseValues
                .parallelStream().anyMatch(set -> set.id.equals(id));
    }

    public synchronized boolean remove(String id) {
        List<StructureSet> sets = databaseValues.parallelStream()
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
