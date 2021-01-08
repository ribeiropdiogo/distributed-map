package parallel_hashmap.comm;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;


public class Request implements Message, Serializable {

    public final Method method;
    public final Map<Long, byte[]> map;
    public final Collection<Long> col;


    public static enum Method { PUT, GET }


    public Request(Map<Long, byte[]> map) {
        this.method = Method.PUT;
        this.map = map;
        this.col = null;
    }

    public Request(Collection<Long> col) {
        this.method = Method.GET;
        this.map = null;
        this.col = col;
    }
}
