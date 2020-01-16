package dev.brighten.db.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public abstract class Database {

    private Set<String> mappings = new HashSet<>();

    private final String name;

    public abstract void loadMappings();

    public abstract List<StructureSet> get(String... id);

    public abstract List<StructureSet> get(Predicate<StructureSet> predicate);

    public boolean contains(String id) {
        return mappings.contains(id);
    }

    public abstract int remove(String... id);

    public abstract int remove(Predicate<StructureSet> predicate);

    public abstract void connect(String... args);

    public abstract void disconnect();
}
