package distributedmap.benchmark;

import distributedmap.api.DistributedMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Benchmark {

    // Impede a instanciação
    private Benchmark() {
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Options options = Options.parse(args);
        if (options == null) return;
        Thread[] threads = new Thread[options.concurrent_users];
        final int[] operations = {0};
        final long execution_time_ms = options.execution_time * 1000;
        final long[] trt_ms = { 0 };

        for (int i = 0; i < options.concurrent_users; i++){
            int id = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Denining variables
                        DistributedMap dm = new DistributedMap();
                        final long start_ms = System.currentTimeMillis();
                        long elapsed_ms = 0;

                        // Execute Load
                        while (elapsed_ms < execution_time_ms) {
                            Random random = new Random();
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

                            long current_ms = System.currentTimeMillis();
                            elapsed_ms = current_ms - start_ms;
                            trt_ms[0] += recv_ms - send_ms;
                            ++operations[0];
                            Thread.sleep(10);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        for (int i = 0; i < options.concurrent_users; i++){
            threads[i].start();
            Thread.sleep(10);
        }

        TimeUnit.SECONDS.sleep(options.execution_time+10);

        if (operations[0] > 0) {
            // Print Results
            double trt_s = trt_ms[0] / 1000.0;

            System.out.println("> Benchmark results after " + options.execution_time + " seconds :");
            System.out.println("    Operations: " + operations[0]);
            double throughput = operations[0] / options.execution_time;
            System.out.printf("    Throughput: %.2f operations/second\n", throughput);
            double art = trt_s / operations[0];
            System.out.printf("    Average Response Time: %f seconds\n", art);
        } else {
            System.out.println("> Error running benchmark");
        }

        for (int i = 0; i < options.concurrent_users; i++){
            threads[i].join();
        }
    }
}
