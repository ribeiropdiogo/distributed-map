package distributedmap.servers;

import static distributedmap.utils.Constants.*;
import distributedmap.communication.FutureSocketChannelReader;
import distributedmap.communication.FutureSocketChannelWriter;
import distributedmap.communication.VectorMessage;
import spullara.nio.channels.FutureServerSocketChannel;
import spullara.nio.channels.FutureSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.Executors.defaultThreadFactory;


public class ClockServer {

    private static FutureServerSocketChannel fssc;
    private static ArrayList<FutureSocketChannel> clients;
    private static int[] clocks;


    /* Impede a instanciação */
    private ClockServer() {}

    private static void getClocks(VectorMessage vm) {
        // c = 0 -> server at position c isn't used by the request
        // c = 1 -> server at position c is used by the request
        for (int i = 0; i < TOTAL_SERVERS; ++i) {
            vm.vector[i] =
                    vm.vector[i] == 0 ? clocks[i] : ++clocks[i];
        }
    }

    private static void serveRec(FutureSocketChannel socket) {
        ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);

        FutureSocketChannelReader.read(socket, buf).thenAccept(msg -> {
            VectorMessage vm = (VectorMessage) msg;

            getClocks(vm);
            System.out.println("> " + Arrays.toString(vm.vector));

            // Send response
            FutureSocketChannelWriter.write(socket, vm)
                    .thenAccept(_void_ -> serveRec(socket));

        }).exceptionally(e -> {
            clients.remove(socket);
            socket.close();
            System.out.println("> Client disconnected (" + clients.size() + " clients connected)");
            return null;
        });
    }

    private static void acceptRec() {
        fssc.accept().thenAccept(socket -> {
            clients.add(socket);
            System.out.println("> Connection accepted (" + clients.size() + " clients connected)");
            serveRec(socket);
            acceptRec();
        });
    }

    public static void main(String[] args) throws IOException {
        System.out.println("> Clock Server started...");

        clients = new ArrayList<>();
        clocks = new int[TOTAL_SERVERS];
        for (int i = 0; i < TOTAL_SERVERS; i++)
            clocks[i] = 0;

        AsynchronousChannelGroup acg =
                AsynchronousChannelGroup.withFixedThreadPool(1, defaultThreadFactory());
        fssc = FutureServerSocketChannel.open(acg);
        fssc.bind(new InetSocketAddress(CLOCK_SERVER_PORT));

        System.out.println("> Listening on port " + CLOCK_SERVER_PORT + "...");

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
