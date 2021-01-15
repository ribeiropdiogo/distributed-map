import distributedmap.api.DistributedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class ConcurrentTest {

    public static void main(String[] args) throws Exception {

        int total = 5;
        Thread[] threads = new Thread[total];

        for (int i = 0; i < total; i++){
            int id = i;
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    System.out.println("> Starting thread " + id);
                    try {
                        DistributedMap dm = new DistributedMap();
                        Map<Long, byte[]> values = new HashMap<>();

                        byte[] array = ("thread"+id).getBytes();
                        values.put(Integer.toUnsignedLong(0),array);
                        values.put(Integer.toUnsignedLong(1),array);
                        /*values.put(Integer.toUnsignedLong(2),array);
                        values.put(Integer.toUnsignedLong(3),array);
                        values.put(Integer.toUnsignedLong(4),array);
                        values.put(Integer.toUnsignedLong(5),array);*/

                        Collection<Long> l = new ArrayList<>();
                        l.add(Integer.toUnsignedLong(0));
                        l.add(Integer.toUnsignedLong(1));
                        /*l.add(Integer.toUnsignedLong(2));
                        l.add(Integer.toUnsignedLong(3));
                        l.add(Integer.toUnsignedLong(4));
                        l.add(Integer.toUnsignedLong(5));*/

                        dm.put(values).thenAccept(v -> {
                            System.out.println("> Thread "+id+" finished put...");
                            try {
                                dm.get(l).thenAccept(map -> {
                                    System.out.println("> Thread "+id+" reads:");
                                    for (Map.Entry<Long, byte[]> entry : map.entrySet()) {
                                        System.out.println("> T"+id+": " + entry.getKey().toString() + " = " + new String(entry.getValue()));
                                    }
                                    dm.close();
                                    System.out.println("> Thread "+id+" finished get's...");
                                });
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        for (int i = 0; i < total; i++){
            threads[i].start();
            Thread.sleep(10);
        }

        while (true) {
            try { Thread.sleep(Long.MAX_VALUE); } catch (Exception ignored) {}
        }
    }
}
