package distributedmap.communication;

import java.io.Serializable;
import java.util.Map;


public class Response implements Message, Serializable {

    public final Map<Long, byte[]> map;


    public Response() {
        this.map = null;
    }

    public Response(Map<Long, byte[]> map) {
        this.map = map;
    }
}
