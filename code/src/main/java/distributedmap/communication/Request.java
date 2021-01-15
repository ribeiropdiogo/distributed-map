package distributedmap.communication;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static distributedmap.utils.Constants.TOTAL_SERVERS;


public class Request implements Message, Serializable {

    public final Method method;
    public final Map<Long, byte[]> map;
    public final Collection<Long> col;
    public final int clock;


    public static enum Method { PUT, GET }


    public Request(Map<Long, byte[]> map, int clock) {
        this.method = Method.PUT;
        this.map = map;
        this.col = null;
        this.clock = clock;
    }

    public Request(Collection<Long> col, int clock) {
        this.method = Method.GET;
        this.map = null;
        this.col = col;
        this.clock = clock;
    }
}
