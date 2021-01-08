import static java.lang.System.out;
import parallel_hashmap.impl.DistributedMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;


/*
=== PUT:
put\n
long0 value0\n
long1 value1\n
long2 value2\n
...
\n

=== GET:
get long0 long1 long2 long3 ...\n
*/


public class InteractiveClient {
    private final DistributedMap map;
    private final BufferedReader reader;
    private final ReentrantLock lock;


    private InteractiveClient() throws IOException, ExecutionException {
        map = new DistributedMap();
        reader = new BufferedReader(new InputStreamReader(System.in));
        lock = new ReentrantLock();
    }

    private void close() {
        map.close();
    }

    private void start() throws IOException {
        out.println("> Ready!");
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                lock.lock();
                out.println("> Error reading");
                lock.unlock();
                return;
            } else if (line.isBlank()) {
                lock.lock();
                out.println("> Quiting");
                lock.unlock();
                return;
            }
            line = line.strip();

            if (line.equals("put"))
                put();

            else if (line.startsWith("get "))
                get(line);

            else {
                lock.lock();
                out.println("> Invalid parameter: " + line.split(" ")[0]);
                lock.unlock();
            }
        }
    }

    private void put() throws IOException {
        Map<Long, byte[]> req = new HashMap<>();

        lock.lock();
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    out.println("> Error reading");
                    return;
                } else if (line.isBlank()) {
                    break;
                }
                line = line.strip();

                String[] args = line.split(" +", 2);

                req.put(Long.parseLong(args[0]), args[1].getBytes());
            }
        } finally {
            lock.unlock();
        }

        map.put(req).thenAccept(_v_ -> {
            lock.lock();
            out.println("> PUT executed successfully");
            lock.unlock();
        });
    }

    private void get(String line) {
        String[] _keys = line.substring(4).strip().split(" +");

        Collection<Long> req = new ArrayList<>();

        for (String _key : _keys)
            req.add(Long.parseLong(_key));

        map.get(req).thenAccept(pairs -> {
            lock.lock();

            out.println("> GET response received:");
            for (Map.Entry<Long, byte[]> pair : pairs.entrySet())
                out.println("> " + pair.getKey() + " = " + new String(pair.getValue()));

            lock.unlock();
        });
    }

    public static void main(String[] args) throws IOException, ExecutionException {
        InteractiveClient cli = null;

        try {
            cli = new InteractiveClient();
            cli.start();

        } finally {
            if (cli != null)
                cli.close();
        }
    }
}
