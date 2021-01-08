package parallel_hashmap.server;

import static parallel_hashmap.Constants.*;
import parallel_hashmap.comm.*;
import parallel_hashmap.util.LockableHashMap;
import spullara.nio.channels.FutureServerSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import static java.util.concurrent.Executors.defaultThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Server {

    private static FutureServerSocketChannel fssc;
    private static ArrayList<Client> clients;
    private static LockableHashMap<Long, byte[]> map;


    /* Impede a instanciação */
    private Server() {}

    private static void putRec(Client c, Map<Long, byte[]> nmap) {
        // Put the key/values in the map
        map.lock();
        map.putAll(nmap);
        map.unlock();

        // Send response
        Response res = new Response(true);

        FutureSocketChannelWriter.write(c.socket, res)
                .thenAccept(_void_ -> {
                    serveRec(c);
                });
    }

    private static void getRec(Client c, Collection<Long> keys) {
        boolean success = true;

        // Get the key/values in the map
        Map<Long, byte[]> rmap = new HashMap<>();
        map.lock();
        for (Long key : keys) {
            byte[] value = map.get(key);
            if (value == null)
                success = false;
            else
                rmap.put(key, value);
        }
        map.unlock();

        // Send response
        Response res = new Response(success, rmap);

        FutureSocketChannelWriter.write(c.socket, res)
                .thenAccept(_void_ -> {
                    serveRec(c);
                });
    }

    private static void serveRec(Client c) {
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

        FutureSocketChannelReader.read(c.socket, buf).thenAccept(msg -> {
            Request req = (Request) msg;

            if (req.method == Request.Method.PUT) {
                putRec(c, req.map);

            } else if (req.method == Request.Method.GET) {
                getRec(c, req.col);

            } else {
                System.out.println("> Invalid message");
                clients.remove(c);
                c.socket.close();
                System.out.println("> Client disconnected (" + clients.size() + " clients connected)");
            }

        }).exceptionally(e -> {
            clients.remove(c);
            c.socket.close();
            System.out.println("> Client disconnected (" + clients.size() + " clients connected)");
            return null;
        });
    }

    private static void acceptRec() {
        fssc.accept().thenAccept(socket -> {
            Client c = new Client(socket);
            clients.add(c);
            System.out.println("> Connection accepted (" + clients.size() + " clients connected)");
            serveRec(c);
            acceptRec();
        });
    }

    public static void main(String[] args) throws IOException {
        Options options = Options.parse(args);
        if (options == null) return;

        System.out.println("> Server '" + options.number + "' started...");

        final int port = SERVER_PORT_BASE + options.number;

        clients = new ArrayList<>();
        map = new LockableHashMap<>();

        AsynchronousChannelGroup acg =
                AsynchronousChannelGroup.withFixedThreadPool(1, defaultThreadFactory());
        fssc = FutureServerSocketChannel.open(acg);
        fssc.bind(new InetSocketAddress(port));

        System.out.println("> Listening on port " + port + "...");

        acceptRec();

        /* Await */
        boolean awaitRet = false;
        do {
            try {
                awaitRet = acg.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException ignored) {}
        } while (!awaitRet);
    }
}
