package distributedmap.utils;


public final class Constants {

    public static final int CLOCK_SERVER_PORT = 12200;

    public static final int SERVER_PORT_BASE = 12300;
    public static final int N_SERVERS = 4;

    // Número de threads por servidor (incluindo o Clock Server)
    public static final int N_THREADS = 5;

    public static final int BUF_SIZE = 1000;


    // Impede a instanciação
    private Constants() {
    }
}
