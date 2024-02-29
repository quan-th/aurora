import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int capacity;

    private int countGetByIdHit;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }


    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    /**
     * Retrieves the count of how many times the get method has been called successfully.
     *
     * @return the count of successful get method invocations
     */
    public int getCountGetByIdHit() {
        return countGetByIdHit;
    }

    /**
     * Retrieves the value to which the specified key is mapped, incrementing the count if the key is found.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value != null) {
            countGetByIdHit++;
        }
        return value;
    }
}
