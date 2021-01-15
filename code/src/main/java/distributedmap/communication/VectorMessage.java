package distributedmap.communication;

import java.io.Serializable;


public class VectorMessage implements Message, Serializable {

    public final int[] vector;

    public VectorMessage(int[] vector) {
        this.vector = vector;
    }
}
