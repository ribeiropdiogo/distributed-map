package distributedmap.server;

import distributedmap.comm.FutureSocketChannelReader;
import distributedmap.comm.FutureSocketChannelWriter;
import distributedmap.comm.Response;
import distributedmap.comm.VectorMessage;
import spullara.nio.channels.FutureServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static distributedmap.Constants.*;
import static java.util.concurrent.Executors.defaultThreadFactory;

public class ClockServer {

    private static FutureServerSocketChannel fssc;
    private static ArrayList<Client> clients;
    private static ReentrantLock vector_lock;
    private static int[] clocks;


    /* Impede a instanciação */
    private ClockServer() {}

    private static void initVectorClock(){
        clocks = new int[TOTAL_SERVERS];
        for (int i = 0; i < TOTAL_SERVERS; i++)
            clocks[i] = 0;
    }

    private static int incrementClock(int p){
        clocks[p]++;
        return clocks[p];
    }

    private static int getClock(int p){
        return clocks[p];
    }

    private static void getVector(VectorMessage vr){
        vector_lock.lock();
        // c = 0 -> server at position c isn't used by the request
        // c = 1 -> server at position c is used by the request
        for (int p = 0; p < TOTAL_SERVERS; p++){
            if (vr.vectorClock[p] == 1)
                vr.vectorClock[p] = incrementClock(p);
            else if (vr.vectorClock[p] == 0)
                vr.vectorClock[p] = getClock(p);
        }
        vector_lock.unlock();
    }

    private static void serveRec(Client c) {
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

        FutureSocketChannelReader.read(c.socket, buf).thenAccept(msg -> {
            VectorMessage vr = (VectorMessage) msg;
            getVector(vr);
            System.out.println("> " + Arrays.toString(vr.vectorClock));
            // Send response
            FutureSocketChannelWriter.write(c.socket, vr)
                    .thenAccept(_void_ -> {
                        serveRec(c);
                    });
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

        System.out.println("> Clock Server started...");

        final int port = CLOCK_SERVER_PORT;

        clients = new ArrayList<>();
        vector_lock = new ReentrantLock();
        initVectorClock();

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
