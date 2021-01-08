package distributedmap.impl;

import static distributedmap.Constants.*;
import distributedmap.comm.FutureSocketChannelReader;
import distributedmap.comm.FutureSocketChannelWriter;
import distributedmap.comm.Request;
import distributedmap.comm.Response;
import distributedmap.util.Counter;
import distributedmap.util.LockableHashMap;
import spullara.nio.channels.FutureSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class DistributedMap {

    private final FutureSocketChannel[] sockets;


    public DistributedMap() throws IOException, ExecutionException {
        sockets = new FutureSocketChannel[TOTAL_SERVERS];
        List<CompletableFuture<Void>> wait = new ArrayList<>();

        /* É feita a ligação com cada um dos servidores */
        for (int i = 0; i < TOTAL_SERVERS; ++i) {
            sockets[i] = new FutureSocketChannel();
            wait.add(
                    sockets[i].connect(
                            new InetSocketAddress("localhost",SERVER_PORT_BASE + i)
                    )
            );
        }

        /* Espera que todas as ligações sejam feitas */
        for (int i = 0; i < TOTAL_SERVERS;) {
            try {
                wait.get(i).get();
                ++i;
            } catch (InterruptedException ignored) {}
        }
    }

    public void close() {
        for (FutureSocketChannel socket : sockets)
            socket.close();
    }

    private int hash(Long key) {
        return (int) (key % TOTAL_SERVERS);
    }

    public CompletableFuture<Void> put(Map<Long, byte[]> pairs) {
        final CompletableFuture<Void> acceptor = new CompletableFuture<>();

        /* Separação dos pares pelos servidores correspondentes */
        List<Map<Long, byte[]>> mapList = new ArrayList<>();
        for (int i = 0; i < TOTAL_SERVERS; ++i) mapList.add(null);
        int _numRequests = 0;  // número de pedidos a serem feitos (= número de servidores a serem contactados)
        for (Map.Entry<Long, byte[]> entry : pairs.entrySet()) {
            Long key = entry.getKey();
            int serverNum = hash(key);
            if (mapList.get(serverNum) == null) {
                ++_numRequests;
                mapList.set(serverNum, new HashMap<>());
            }
            mapList.get(serverNum).put(key, entry.getValue());
        }

        /* Envio */
        final int numRequests = _numRequests;
        final Counter counter = new Counter();
        for (int i = 0; i < TOTAL_SERVERS; ++i) {
            Map<Long, byte[]> map = mapList.get(i);
            if (map != null) {
                Request req = new Request(map);
                final FutureSocketChannel socket = sockets[i];

                FutureSocketChannelWriter.write(socket, req).thenAccept(_void_ -> {
                    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
                    FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
                        Response res = (Response) msg;
                        if (!res.success) {
                            // TODO wat do I do with dis
                            return;
                        }
                        if (counter.inc() >= numRequests)
                            /* Recebeu todas as respostas */
                            acceptor.complete(null);
                    });
                });
            }
        }

        return acceptor;
    }

    public CompletableFuture<Map<Long, byte[]>> get(Collection<Long> keys) {
        final CompletableFuture<Map<Long, byte[]>> acceptor = new CompletableFuture<>();
        final LockableHashMap<Long, byte[]> r = new LockableHashMap<>();

        /* Separação das keys pelos servidores correspondentes */
        List<Collection<Long>> mapList = new ArrayList<>();
        for (int i = 0; i < TOTAL_SERVERS; ++i) mapList.add(null);
        int _numRequests = 0;  // número de pedidos a serem feitos (= número de servidores a serem contactados)
        for (Long key : keys) {
            int serverNum = hash(key);
            if (mapList.get(serverNum) == null) {
                ++_numRequests;
                mapList.set(serverNum, new ArrayList<>());
            }
            mapList.get(serverNum).add(key);
        }

        /* Envio */
        final int numRequests = _numRequests;
        final Counter counter = new Counter();
        for (int i = 0; i < TOTAL_SERVERS; ++i) {
            Collection<Long> col = mapList.get(i);
            if (col != null) {
                Request req = new Request(col);
                final FutureSocketChannel socket = sockets[i];

                FutureSocketChannelWriter.write(socket, req).thenAccept(_void_ -> {
                    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
                    FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
                        Response res = (Response) msg;
                        if (!res.success || res.map == null) {
                            // TODO wat do I do with dis
                            return;
                        }
                        r.lock();
                        r.putAll(res.map);
                        r.unlock();
                        if (counter.inc() >= numRequests)
                            /* Recebeu todas as respostas */
                            acceptor.complete(r);
                    });
                });
            }
        }

        return acceptor;
    }
}
