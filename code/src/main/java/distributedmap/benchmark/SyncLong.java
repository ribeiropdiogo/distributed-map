package distributedmap.benchmark;


public class SyncLong {

    private long count;


    public SyncLong() {
        count = 0L;
    }

    public synchronized long inc(long value) {
        return count += value;
    }

    public synchronized long get() {
        return count;
    }
}
