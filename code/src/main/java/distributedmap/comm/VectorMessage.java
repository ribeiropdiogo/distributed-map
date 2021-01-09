package distributedmap.comm;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static distributedmap.Constants.TOTAL_SERVERS;


public class VectorMessage implements Message, Serializable {

    public final int[] vectorClock;

    public VectorMessage(int[] vector) {
        this.vectorClock = vector;
    }
}
