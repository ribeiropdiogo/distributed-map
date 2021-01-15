package distributedmap.servers;

import static distributedmap.utils.Constants.*;
import distributedmap.communication.*;
import distributedmap.utils.Pair;
import distributedmap.utils.SyncCounter;
import distributedmap.utils.LockableHashMap;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import static java.util.concurrent.Executors.defaultThreadFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Server {

    private static FutureServerSocketChannel fssc;
    private static HashMap<Long, byte[]> map;
    private static LockableHashMap<Integer, Pair<FutureSocketChannel, Request>> queue;
    private static SyncCounter clock;


    // Impede a instanciação
    private Server() {}

    private static void serveQueueRec(int next) {
        queue.lock();
        Pair<FutureSocketChannel, Request> pair = queue.remove(next);
        queue.unlock();

        if (pair != null) {
            switch (pair.snd.method) {
                case PUT:
                    put(pair.fst, pair.snd.map);
                    break;
                case GET:
                    get(pair.fst, pair.snd.col);
                    break;
            }
        }
    }

    private static void put(FutureSocketChannel socket, Map<Long, byte[]> nmap) {
        // Put the key/values in the map
        map.putAll(nmap);

        // Send response
        Response res = new Response();

        FutureSocketChannelWriter.write(socket, res)
                .thenAccept(_void_ -> {
                    int c = clock.inc();
                    System.out.println("Request(clock=" + c + ") served");
                    serveQueueRec(c + 1);
                });
    }

    private static void get(FutureSocketChannel socket, Collection<Long> keys) {
        // Get the key/values in the map
        Map<Long, byte[]> rmap = new HashMap<>();
        for (Long key : keys) {
            byte[] value = map.get(key);
            if (value != null)
                rmap.put(key, value);
        }

        // Send response
        Response res = new Response(rmap);

        FutureSocketChannelWriter.write(socket, res)
                .thenAccept(_void_ -> {
                    int c = clock.inc();
                    System.out.println("Request(clock=" + c + ") served");
                    serveQueueRec(c + 1);
                });
    }

    private static void serveRec(FutureSocketChannel socket) {
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

        FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
            Request req = (Request) msg;

            System.out.println("Received request(clock=" + req.clock + ")");

            if (req.clock == clock.get() + 1) {
                switch (req.method) {
                    case PUT:
                        put(socket, req.map);
                        break;
                    case GET:
                        get(socket, req.col);
                        break;
                }
            } else {
                queue.lock();
                queue.put(req.clock, new Pair<>(socket, req));
                queue.unlock();
            }
            serveRec(socket);

        }).exceptionally(e -> {
            System.out.println("Client disconnected");
            socket.close();
            return null;
        });
    }

    private static void acceptRec() {
        fssc.accept().thenAccept(socket -> {
            System.out.println("Connection accepted");
            serveRec(socket);
            acceptRec();
        });
    }

    public static void main(String[] args) throws IOException {
        Options options = Options.parse(args);
        if (options == null) return;

        System.out.println("Server '" + options.number + "' started...");

        final int port = SERVER_PORT_BASE + options.number;

        map = new HashMap<>();
        queue = new LockableHashMap<>();
        clock = new SyncCounter();

        AsynchronousChannelGroup acg =
                AsynchronousChannelGroup.withFixedThreadPool(N_THREADS, defaultThreadFactory());
        fssc = FutureServerSocketChannel.open(acg);
        fssc.bind(new InetSocketAddress(port));

        System.out.println("Listening on port " + port + "...");

        acceptRec();

        // Await
        boolean awaitRet = false;
        do {
            try {
                awaitRet = acg.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ignored) {}
        } while (!awaitRet);
    }
}
