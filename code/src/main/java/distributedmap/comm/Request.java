package distributedmap.comm;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static distributedmap.Constants.TOTAL_SERVERS;


public class Request implements Message, Serializable {

    public final Method method;
    public final Map<Long, byte[]> map;
    public final Collection<Long> col;
    public int[] vectorClock;


    public static enum Method { PUT, GET }


    public Request(Map<Long, byte[]> map) {
        this.method = Method.PUT;
        this.map = map;
        this.col = null;
        this.vectorClock = new int[TOTAL_SERVERS];
    }

    public Request(Collection<Long> col) {
        this.method = Method.GET;
        this.map = null;
        this.col = col;
        this.vectorClock = new int[TOTAL_SERVERS];
    }
}
