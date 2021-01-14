package distributedmap.benchmark;

import distributedmap.API.DistributedMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Benchmark {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        Options options = Options.parse(args);
        if (options == null) return;

        // Denining variables
        DistributedMap dm = new DistributedMap();
        int operations = 0;
        long start = System.currentTimeMillis();
        float elapsed = 0, throughput, artt, trtt = 0;

        // Execute Load
        while (elapsed < options.execution_time){
            float begin = 0;

            Random random = new Random();
            int option = random.nextInt(2) + 1;
            switch (option){
                case 1:
                    int key = random.nextInt(10000) + 1;
                    byte[] array = "This benchmark is really fancy...".getBytes();
                    Map<Long, byte[]> values = new HashMap<>();
                    values.put(Integer.toUnsignedLong(key),array);
                    dm.put(values);
                    break;
                case 2:
                    int get_key = random.nextInt(10000) + 1;
                    Collection<Long> l = new ArrayList<>();
                    l.add(Integer.toUnsignedLong(get_key));
                    dm.get(l);
                    break;
            }

            long current = System.currentTimeMillis();
            trtt += TimeUnit.MILLISECONDS.toMinutes(current - start);
            elapsed = TimeUnit.MILLISECONDS.toMinutes(current - start);
            operations++;
            Thread.sleep(10);
        }

        if (operations > 0) {
            // Print Results
            System.out.println("> Benchmark results after "+options.execution_time+" minutes : ");
            throughput = operations/elapsed;
            System.out.println("    Throughput: " + throughput + " operations/second");
            artt = trtt/operations;
            System.out.println("    Average Response Time: " + artt + " seconds");
        } else {
            System.out.println("> Error running benchmark");
        }


    }
}
