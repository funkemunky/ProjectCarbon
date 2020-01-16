package dev.brighten.db.utils;

public class Pair<K, V> {

    public Pair(K key, V val) {
        this.key = key;
        this.value = val;
    }

    public K key;
    public V value;
}
