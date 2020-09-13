package dev.brighten.db.db;

import dev.brighten.db.utils.Pair;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlatfileDatabase extends Database {

    @Setter
    private static File directory = new File(System.getProperty("user.home")
            + File.separator + "CarbonFFDBs");

    private File dbDirectory;
    private Map<String, File> fileMappings = new HashMap<>();
    private SortedMap<String, FileSet> cachedSets = new ConcurrentSkipListMap<>(Comparator
            .comparing(k -> System.currentTimeMillis() - fileMappings.get(k).lastModified()));
    @Setter
    private int cacheSizeLimit = 50000;

    public FlatfileDatabase(String name) {
        super(name);
        if(!directory.exists()) directory.mkdirs();
        dbDirectory = new File(directory.getPath() + File.separator + name);

        if(!dbDirectory.exists()) dbDirectory.mkdirs();
    }

    @Override
    public void loadMappings() {
        val files = dbDirectory.listFiles(file -> file.getName().toLowerCase().endsWith(".json"));

        if(files == null) return;

        for (File file : files) {
            String id = file.getName().replace(".json", "");
            getMappings().add(id);
            fileMappings.put(id, file);
        }
    }

    @Override
    public List<StructureSet> get(boolean parallel, String... id) {

        return (parallel ? Arrays.stream(id).parallel() : Arrays.stream(id))
                .filter(fileMappings::containsKey)
                .map(key -> fileMappings.get(key))
                .map(FileSet::new)
                .filter(set -> Arrays.asList(id).contains(set.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StructureSet> get(String... id) {
        return get(false, id);
    }

    @Override
    public List<StructureSet> get(boolean parallel, Predicate<StructureSet> predicate) {
        return (parallel ? fileMappings.values().parallelStream() : fileMappings.values().stream())
                .map(FileSet::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }


    @Override
    public List<StructureSet> get(Predicate<StructureSet> predicate) {
        return get(false, predicate);
    }

    @SneakyThrows
    @Override
    public StructureSet create(String id) {
        if(getMappings().contains(id)) return get(id).get(0);
        File file = new File(dbDirectory.getPath() + File.separator + id + ".json");

        fileMappings.put(file.getName().replace(".json", ""), file);
        return new FileSet(file);
    }

    @Override
    public int remove(String... id) {
        AtomicInteger count = new AtomicInteger(0);
        Arrays.stream(id).filter(fileMappings::containsKey)
                .map(key -> new Pair<>(key, fileMappings.get(key)))
                .forEach(pair -> {
                    String key = pair.key;
                    File file = pair.value;

                    fileMappings.remove(key);
                    cachedSets.remove(key);
                    getMappings().remove(key);

                    if(file.delete()) count.incrementAndGet();
                });
        return 0;
    }

    @Override
    public int remove(Predicate<StructureSet> predicate) {
        AtomicInteger count = new AtomicInteger(0);
        fileMappings.keySet().stream()
                .map(key -> new Pair<>(key, fileMappings.get(key)))
                .forEach(pair -> {
                    String key = pair.key;
                    File file = pair.value;

                    fileMappings.remove(key);
                    cachedSets.remove(key);
                    getMappings().remove(key);

                    if(file.delete()) count.incrementAndGet();
                });
        return 0;
    }

    @Override
    public void connect(String... args) {
        //Empty method.
    }

    @Override
    public void disconnect() {
        getMappings().clear();
        fileMappings.clear();
        cachedSets.clear();
    }
}
