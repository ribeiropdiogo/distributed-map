package parallel_hashmap.comm;

import spullara.nio.channels.FutureSocketChannel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;


public class FutureSocketChannelWriter {

    public static CompletableFuture<Void> write(FutureSocketChannel socket, Message msg) {
        CompletableFuture<Void> acceptor = new CompletableFuture<>();

        /* Conversão de Message para ByteBuffer */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            try { baos.close(); } catch (IOException ignored) {}
            acceptor.completeExceptionally(e);
            return acceptor;
        }
        byte[] bytes = baos.toByteArray();
        try { oos.close(); } catch (IOException ignored) {}
        ByteBuffer buf = ByteBuffer
                .allocate(4 + bytes.length)
                .putInt(bytes.length)
                .put(bytes)
                .flip();

        /* Escrita */
        writeAll(socket, buf, acceptor);

        return acceptor;
    }

    private static void writeAll(FutureSocketChannel socket,
                                 ByteBuffer buf,
                                 CompletableFuture<Void> acceptor) {
        socket.write(buf).thenAccept(i -> {
            if (buf.hasRemaining())
                writeAll(socket, buf, acceptor);
            else
                acceptor.complete(null);
        });
    }
}
