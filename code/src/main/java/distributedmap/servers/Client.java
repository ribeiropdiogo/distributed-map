package distributedmap.servers;

import spullara.nio.channels.FutureSocketChannel;


public class Client {

    public final FutureSocketChannel socket;


    public Client(FutureSocketChannel socket) {
        this.socket = socket;
    }
}