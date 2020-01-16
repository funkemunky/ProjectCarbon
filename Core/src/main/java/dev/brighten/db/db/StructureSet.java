package dev.brighten.db.db;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@Getter
public abstract class StructureSet {
    private final String id;

    public abstract <T> T getObject(String key);

    public abstract boolean save(Database database);

    public abstract boolean input(String key, Object object);

    public abstract boolean contains(String key);

    public abstract Set<String> getKeys();
}
