package parallel_hashmap.util;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;


public class LockableHashMap<K, V> extends HashMap<K, V> {

    private final ReentrantLock lock = new ReentrantLock();


    public LockableHashMap() {
        super();
    }

    public LockableHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public LockableHashMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }
}
