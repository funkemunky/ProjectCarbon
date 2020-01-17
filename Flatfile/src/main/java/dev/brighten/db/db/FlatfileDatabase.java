package dev.brighten.db.db;

import dev.brighten.db.utils.Pair;
import dev.brighten.db.utils.json.JSONObject;
import dev.brighten.db.utils.json.JsonReader;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlatfileDatabase extends Database {

    @Setter
    private static File directory = new File(System.getProperty("user.home")
            + File.separator + "CarbonFFDBs");

    public FlatfileDatabase(String name) {
        super(name);
    }

    private Map<String, File> fileMappings = new HashMap<>();

    @Override
    public void loadMappings() {
        directory = new File(directory.getPath() + File.separator + getName());

        if(!directory.exists()) {
            if(!directory.mkdirs()) return;
        } else if(!directory.isDirectory()) {
            if(directory.delete()) {
                if(!directory.mkdirs()) return;
            } else return;
        }

        val files = directory.listFiles(file -> file.getName().toLowerCase().endsWith(".json"));

        if(files == null) return;

        for (File file : files) {
            String id = file.getName().replace(".json", "");
            getMappings().add(id);
            fileMappings.put(id, file);
        }
    }

    @Override
    public List<StructureSet> get(String... id) {
        return Arrays.stream(id).filter(fileMappings::containsKey)
                .map(key -> fileMappings.get(key))
                .map(FileSet::new)
                .filter(set -> Arrays.asList(id).contains(set.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StructureSet> get(Predicate<StructureSet> predicate) {
        return fileMappings.values().stream()
                .map(FileSet::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public StructureSet create(String id) {
        if(getMappings().contains(id)) return get(id).get(0);
        File file = new File(directory.getPath() + File.separator + id + ".json");

        System.out.println("File: " + file.getPath());
        if(!file.exists()) {
            if(!file.createNewFile()) return null;
        }

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
    }
}
