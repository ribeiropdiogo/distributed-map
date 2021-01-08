package parallel_hashmap.util;


public class Counter {

    private int count;


    public Counter() {
        count = 0;
    }

    public Counter(int initialValue) {
        count = initialValue;
    }

    public synchronized int inc() {
        return ++count;
    }

    public synchronized int dec() {
        return --count;
    }

    public synchronized int get() {
        return count;
    }
}
