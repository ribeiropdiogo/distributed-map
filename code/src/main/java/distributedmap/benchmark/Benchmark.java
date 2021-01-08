package distributedmap.benchmark;

import distributedmap.impl.DistributedMap;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public class Benchmark {
    public static void main(String[] args) throws IOException, ExecutionException {

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
                    //dm.put();
                    break;
                case 2:
                    //dm.get();
                    break;
            }

            long current = System.currentTimeMillis();
            trtt += (current - start)/60F;
            elapsed = (current - start)/60F;
            operations++;
        }

        if (operations > 0) {
            // Print Results
            System.out.println("> Benchmark results after "+options.execution_time+" minutes : ");
            throughput = operations/elapsed;
            System.out.println("Throughput: " + throughput + "operations/second");
            artt = trtt/operations;
            System.out.println("Average Response Time: " + artt + "seconds");
        } else {
            System.out.println("> Error running benchmark");
        }


    }
}
