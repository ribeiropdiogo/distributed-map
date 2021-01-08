package parallel_hashmap.comm;

import java.io.Serializable;
import java.util.Map;


public class Response implements Message, Serializable {

    public final boolean success;
    public final Map<Long, byte[]> map;


    public Response(boolean success) {
        this.success = success;
        this.map = null;
    }

    public Response(boolean success, Map<Long, byte[]> map) {
        this.success = success;
        this.map = map;
    }
}
