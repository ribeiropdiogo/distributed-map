package distributedmap.communication;

import java.io.Serializable;


public class VectorMessage implements Message, Serializable {

    public final int[] vectorClock;

    public VectorMessage(int[] vector) {
        this.vectorClock = vector;
    }
}
