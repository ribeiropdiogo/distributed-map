package distributedmap.utils;


public class SyncCounter {

    private int count;


    public SyncCounter() {
        count = 0;
    }

    public SyncCounter(int initialValue) {
        count = initialValue;
    }

    public synchronized int inc() {
        return ++count;
    }

    public synchronized int inc(int value) {
        return count += value;
    }

    public synchronized int dec() {
        return --count;
    }

    public synchronized int dec(int value) {
        return count -= value;
    }

    public synchronized int get() {
        return count;
    }
}
