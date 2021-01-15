package distributedmap.benchmark;

import distributedmap.api.DistributedMap;
import distributedmap.utils.SyncCounter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class Benchmark {

    // Impede a instanciação
    private Benchmark() {
    }

    public static void main(String[] args) throws InterruptedException {

        Options options = Options.parse(args);
        if (options == null) return;

        Thread[] threads = new Thread[options.concurrent_users];
        final SyncCounter terminatedThreads = new SyncCounter();
        final SyncLong operations = new SyncLong();
        final SyncLong trt_ms = new SyncLong();

        System.out.println("> Starting benchmark with parameters: " + options.concurrent_users + " users, " +
                options.execution_time + " seconds");

        final long execution_time_ms = options.execution_time * 1000L;

        for (int i = 0; i < options.concurrent_users; ++i) {
            threads[i] = new Thread(() -> {
                try {
                    // Denining variables
                    DistributedMap dm = new DistributedMap();
                    long elapsed_ms = 0L;
                    long local_trt_ms = 0L, local_operations = 0L;
                    Random random = new Random();
                    final long start_ms = System.currentTimeMillis();

                    // Execute Load
                    while (elapsed_ms < execution_time_ms) {
                        long send_ms, recv_ms;

                        boolean option = random.nextBoolean();
                        long key = random.nextLong();
                        if (option) {
                            byte[] array = "This benchmark is really fancy...".getBytes();
                            Map<Long, byte[]> values = new HashMap<>();
                            values.put(key, array);
                            send_ms = System.currentTimeMillis();
                            dm.put(values).get();
                        } else {
                            Collection<Long> l = new ArrayList<>();
                            l.add(key);
                            send_ms = System.currentTimeMillis();
                            dm.get(l).get();
                        }
                        recv_ms = System.currentTimeMillis();

                        elapsed_ms = recv_ms - start_ms;
                        local_trt_ms += recv_ms - send_ms;
                        ++local_operations;
                        Thread.sleep(10);
                    }

                    trt_ms.inc(local_trt_ms);
                    operations.inc(local_operations);
                    terminatedThreads.inc();
                } catch (IOException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < options.concurrent_users; ++i) {
            threads[i].start();
            Thread.sleep(10);
        }

        // Waits termination
        Thread.sleep(execution_time_ms);
        while (terminatedThreads.get() < options.concurrent_users)
            Thread.sleep(1000);

        if (operations.get() > 0) {
            // Print Results
            double ops = operations.get();
            double throughput = ops / ((double) options.execution_time);
            double art = ((double) trt_ms.get()) / ops;

            System.out.println("> Benchmark results:");
            System.out.println("    Operations: " + operations.get());
            System.out.printf("    Throughput: %.2f operations/s\n", throughput);
            System.out.printf("    Average Response Time: %f ms\n", art);
        } else {
            System.out.println("> Error running benchmark");
        }

        for (int i = 0; i < options.concurrent_users; ++i)
            threads[i].join();
    }
}
