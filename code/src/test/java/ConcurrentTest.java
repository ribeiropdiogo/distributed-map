import distributedmap.api.DistributedMap;
import distributedmap.utils.SyncCounter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class ConcurrentTest {

    public static void main(String[] args) {

        final int N_THREADS = 25;
        final SyncCounter counter = new SyncCounter();
        Thread[] threads = new Thread[N_THREADS];

        for (int i = 0; i < N_THREADS; ++i) {
            final int id = i;
            threads[i] = new Thread(() -> {
                System.out.println("> Starting thread " + id);

                DistributedMap dm;
                try {
                    dm = new DistributedMap();
                } catch (IOException | ExecutionException e) {
                    e.printStackTrace();
                    return;
                }
                Map<Long, byte[]> pairs = new HashMap<>();

                byte[] array = ("thread" + id).getBytes();
                pairs.put(0L, array);
                pairs.put(1L, array);
                pairs.put(2L, array);
                pairs.put(3L, array);

                Collection<Long> keys = new ArrayList<>();
                keys.add(0L);
                keys.add(1L);
                keys.add(2L);
                keys.add(3L);

                dm.put(pairs).thenAccept(v -> {
                    System.out.println("> Thread " + id + " finished put");

                    dm.get(keys).thenAccept(map -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("> Thread ").append(id).append(" finished get:");
                        for (Map.Entry<Long, byte[]> entry : map.entrySet())
                            sb.append("\n    T").append(id).append(": ").append(entry.getKey()).append(" = ").append(new String(entry.getValue()));
                        System.out.println(sb.toString());

                        dm.close();

                        if (counter.inc() >= N_THREADS)
                            System.out.println("> Finished");
                    });
                });
            });
        }

        for (int i = 0; i < N_THREADS; i++){
            threads[i].start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }

        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (Exception ignored) {
            }
        }
    }
}
