package distributedmap.benchmark;

import distributedmap.api.DistributedMap;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class Benchmark {

    // Impede a instanciação
    private Benchmark() {}

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Options options = Options.parse(args);
        if (options == null) return;

        // Denining variables
        DistributedMap dm = new DistributedMap();
        int operations = 0;
        long elapsed_ms = 0, trt_ms = 0;
        final long execution_time_ms = options.execution_time * 1000;
        final long start_ms = System.currentTimeMillis();

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
            trt_ms += recv_ms - send_ms;
            ++operations;
        }

        if (operations > 0) {
            // Print Results
            double elapsed_s = elapsed_ms / 1000.0;
            double trt_s = trt_ms / 1000.0;

            System.out.println("> Benchmark results after " + elapsed_s + " seconds :");
            System.out.println("    Operations: " + operations);
            double throughput = operations/elapsed_s;
            System.out.printf("    Throughput: %.2f operations/second\n", throughput);
            double art = trt_s/operations;
            System.out.printf("    Average Response Time: %f seconds\n", art);
        } else {
            System.out.println("> Error running benchmark");
        }
    }
}
