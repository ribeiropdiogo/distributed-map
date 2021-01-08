import parallel_hashmap.impl.DistributedMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class Test {

    public static void main(String[] args) throws Exception {
        DistributedMap dm = new DistributedMap();
        Map<Long, byte[]> values = new HashMap<>();

        byte[] array = "wassaaaa".getBytes();

        /*
        values.put(Integer.toUnsignedLong(1),array);
        values.put(Integer.toUnsignedLong(2),array);
        values.put(Integer.toUnsignedLong(3),array);
        values.put(Integer.toUnsignedLong(4),array);
        values.put(Integer.toUnsignedLong(5),array);
        values.put(Integer.toUnsignedLong(6),array);
        values.put(Integer.toUnsignedLong(7),array);
        values.put(Integer.toUnsignedLong(8),array);
        values.put(Integer.toUnsignedLong(9),array);
        values.put(Integer.toUnsignedLong(10),array);
        values.put(Integer.toUnsignedLong(11),array);
        values.put(Integer.toUnsignedLong(13),array);
        values.put(Integer.toUnsignedLong(14),array);
        values.put(Integer.toUnsignedLong(15),array);
        values.put(Integer.toUnsignedLong(16),array);
        values.put(Integer.toUnsignedLong(17),array);
        values.put(Integer.toUnsignedLong(18),array);
        values.put(Integer.toUnsignedLong(19),array);
        values.put(Integer.toUnsignedLong(20),array);
        values.put(Integer.toUnsignedLong(21),array);
        values.put(Integer.toUnsignedLong(22),array);
        values.put(Integer.toUnsignedLong(23),array);
        values.put(Integer.toUnsignedLong(24),array);
        */
        values.put(Integer.toUnsignedLong(25),array);

        Collection<Long> l = new ArrayList<>();
        l.add(Integer.toUnsignedLong(25));

        dm.put(values).thenAccept(v -> {
            System.out.println("sent...");
            dm.get(l).thenAccept(map -> {
                System.out.println("received...");
                for (Map.Entry<Long, byte[]> entry : map.entrySet()) {
                    System.out.println(entry.getKey().toString() + " = " + new String(entry.getValue()));
                }

                dm.close();
                System.out.println("closed...");
            });
        });

        while (true) {
            try { Thread.sleep(Long.MAX_VALUE); } catch (Exception ignored) {}
        }
    }
}
