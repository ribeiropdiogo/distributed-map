package distributedmap.api;

import static distributedmap.utils.Constants.*;

import distributedmap.communication.*;
import distributedmap.utils.SyncCounter;
import distributedmap.utils.LockableHashMap;
import spullara.nio.channels.FutureSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class DistributedMap {

    private final FutureSocketChannel[] sockets;
    private final FutureSocketChannel clock_socket;


    public DistributedMap() throws IOException, ExecutionException {
        sockets = new FutureSocketChannel[N_SERVERS];
        List<CompletableFuture<Void>> wait = new ArrayList<>();

        /* É feita a ligação com cada um dos servidores */
        for (int i = 0; i < N_SERVERS; ++i) {
            sockets[i] = new FutureSocketChannel();
            wait.add(
                    sockets[i].connect(
                            new InetSocketAddress("localhost", SERVER_PORT_BASE + i)
                    )
            );
        }
        clock_socket = new FutureSocketChannel();
        wait.add(
                clock_socket.connect(
                        new InetSocketAddress("localhost", CLOCK_SERVER_PORT)
                )
        );

        /* Espera que todas as ligações sejam feitas */
        int n = N_SERVERS + 1;
        for (int i = 0; i < n; ) {
            try {
                wait.get(i).get();
                ++i;
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void close() {
        for (FutureSocketChannel socket : sockets)
            socket.close();
        clock_socket.close();
    }

    private static int hash(Long key) {
        key = key < 0L ? -key : key;
        return (int) (key % N_SERVERS);
    }

    private static int[] initRequestVector() {
        int[] r = new int[N_SERVERS];
        for (int i = 0; i < N_SERVERS; ++i)
            r[i] = 0;
        return r;
    }

    private CompletableFuture<int[]> getClockVector(int[] v) {
        final CompletableFuture<int[]> acceptor = new CompletableFuture<>();

        VectorMessage request = new VectorMessage(v);

        FutureSocketChannelWriter.write(clock_socket, request).thenAccept(_void_ -> {
            ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
            FutureSocketChannelReader.read(clock_socket, buf).thenAccept(msg -> {
                VectorMessage response = (VectorMessage) msg;
                acceptor.complete(response.vector);
            });
        });

        return acceptor;
    }

    public CompletableFuture<Void> put(Map<Long, byte[]> pairs) {
        final CompletableFuture<Void> acceptor = new CompletableFuture<>();
        int[] requestVector = initRequestVector();

        /* Separação dos pares pelos servidores correspondentes */
        List<Map<Long, byte[]>> mapList = new ArrayList<>();
        for (int i = 0; i < N_SERVERS; ++i) mapList.add(null);
        int _numRequests = 0;  // número de pedidos a serem feitos (= número de servidores a serem contactados)
        for (Map.Entry<Long, byte[]> entry : pairs.entrySet()) {
            Long key = entry.getKey();
            int serverNum = hash(key);
            if (mapList.get(serverNum) == null) {
                requestVector[serverNum] = 1;
                ++_numRequests;
                mapList.set(serverNum, new HashMap<>());
            }
            mapList.get(serverNum).put(key, entry.getValue());
        }

        /* Obtenção dos relógios lógicos */
        int[] clockVector;
        try {
            clockVector = getClockVector(requestVector).get();
        } catch (InterruptedException | ExecutionException e) {
            acceptor.completeExceptionally(e);
            return acceptor;
        }

        /* Envio */
        final int numRequests = _numRequests;
        final SyncCounter counter = new SyncCounter();
        for (int i = 0; i < N_SERVERS; ++i) {
            Map<Long, byte[]> map = mapList.get(i);
            if (map != null) {
                Request req = new Request(map, clockVector[i]);
                final FutureSocketChannel socket = sockets[i];

                FutureSocketChannelWriter.write(socket, req).thenAccept(_void_ -> {
                    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
                    FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
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
        int[] requestVector = initRequestVector();

        /* Separação das keys pelos servidores correspondentes */
        List<Collection<Long>> colList = new ArrayList<>();
        for (int i = 0; i < N_SERVERS; ++i) colList.add(null);
        int _numRequests = 0;  // número de pedidos a serem feitos (= número de servidores a serem contactados)
        for (Long key : keys) {
            int serverNum = hash(key);
            if (colList.get(serverNum) == null) {
                requestVector[serverNum] = 1;
                ++_numRequests;
                colList.set(serverNum, new ArrayList<>());
            }
            colList.get(serverNum).add(key);
        }

        /* Obtenção dos relógios lógicos */
        int[] clockVector;
        try {
            clockVector = getClockVector(requestVector).get();
        } catch (InterruptedException | ExecutionException e) {
            acceptor.completeExceptionally(e);
            return acceptor;
        }

        /* Envio */
        final int numRequests = _numRequests;
        final SyncCounter counter = new SyncCounter();
        for (int i = 0; i < N_SERVERS; ++i) {
            Collection<Long> col = colList.get(i);
            if (col != null) {
                Request req = new Request(col, clockVector[i]);
                final FutureSocketChannel socket = sockets[i];

                FutureSocketChannelWriter.write(socket, req).thenAccept(_void_ -> {
                    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
                    FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
                        Response res = (Response) msg;
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
