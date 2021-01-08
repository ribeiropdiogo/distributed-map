package parallel_hashmap.comm;

import spullara.nio.channels.FutureSocketChannel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;


public class FutureSocketChannelReader {

    public static CompletableFuture<Message> read(FutureSocketChannel socket, ByteBuffer buf) {
        CompletableFuture<Message> acceptor = new CompletableFuture<>();

        /* Leitura */
        firstRead(socket, buf, acceptor);

        return acceptor;
    }

    private static void firstRead(FutureSocketChannel socket,
                                ByteBuffer buf,
                                CompletableFuture<Message> acceptor) {
        socket.read(buf).thenAccept(i -> {
            if (i < 0) {
                acceptor.completeExceptionally(new SocketException("Socket closed"));
                return;
            }

            /* Retira os primeiros 4 bytes do buffer */
            buf.flip();
            int msgLength = buf.getInt();
            buf.compact();

            int bytesRead = i - 4;

            if (bytesRead < msgLength)
                readAll(socket, buf, acceptor, bytesRead, msgLength);
            else
                end(buf, acceptor);
        });
    }

    private static void readAll(FutureSocketChannel socket,
                                ByteBuffer buf,
                                CompletableFuture<Message> acceptor,
                                int _bytesRead,
                                int msgLength) {
        socket.read(buf).thenAccept(i -> {
            if (i < 0) {
                acceptor.completeExceptionally(new SocketException("Socket closed"));
                return;
            }

            int bytesRead = _bytesRead + i;

            if (bytesRead < msgLength)
                readAll(socket, buf, acceptor, bytesRead, msgLength);
            else
                end(buf, acceptor);
        });
    }

    private static void end(ByteBuffer buf, CompletableFuture<Message> acceptor) {
        buf.flip();

        /* Conversão de ByteBuffer para Message */
        ByteArrayInputStream bais = new ByteArrayInputStream(buf.array());
        ObjectInputStream oi;
        Message msg;
        try {
            oi = new ObjectInputStream(bais);
            msg = (Message) oi.readObject();
        } catch (ClassNotFoundException | IOException e) {
            try { bais.close(); } catch (IOException ignored) {}
            acceptor.completeExceptionally(e);
            return;
        }
        try { oi.close(); } catch (IOException ignored) {}

        acceptor.complete(msg);
    }
}
