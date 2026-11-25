import java.util.concurrent.CountDownLatch;

public class ConcurrencyAndThreads {
    static final class Counter {
        private int value;

        synchronized void increment() {
            value++;
        }

        synchronized int getValue() {
            return value;
        }
    }

    static final class OneItemBuffer<T> {
        private T item;
        private boolean available;

        synchronized void put(T next) throws InterruptedException {
            while (available) {
                wait();
            }
            item = next;
            available = true;
            notifyAll();
        }

        synchronized T take() throws InterruptedException {
            while (!available) {
                wait();
            }
            T next = item;
            item = null;
            available = false;
            notifyAll();
            return next;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Thread states: " + java.util.Arrays.toString(Thread.State.values()));

        Runnable task = () -> System.out.println("Running on " + Thread.currentThread().getName());
        Thread direct = new Thread(task, "direct-run");
        System.out.println("Before start: " + direct.getState());
        direct.run();
        System.out.println("Calling run() leaves thread state: " + direct.getState());

        Thread worker = new Thread(task, "started-worker");
        worker.setPriority(Thread.NORM_PRIORITY);
        worker.start();
        worker.join();
        System.out.println("After join: " + worker.getState());

        CountDownLatch sleeping = new CountDownLatch(1);
        Thread sleeper = new Thread(() -> {
            sleeping.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException error) {
                System.out.println("Sleep interrupted; restoring interrupt status.");
                Thread.currentThread().interrupt();
            }
        }, "daemon-sleeper");
        sleeper.setDaemon(true);
        sleeper.start();
        sleeping.await();
        sleeper.interrupt();
        sleeper.join();

        Counter counter = new Counter();
        Runnable incrementTask = () -> {
            for (int i = 0; i < 10_000; i++) {
                counter.increment();
            }
        };
        Thread first = new Thread(incrementTask, "counter-1");
        Thread second = new Thread(incrementTask, "counter-2");
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println("Synchronized counter: " + counter.getValue());

        OneItemBuffer<String> buffer = new OneItemBuffer<>();
        Thread producer = new Thread(() -> {
            try {
                buffer.put("Java record");
                System.out.println("Produced one record.");
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }, "producer");
        Thread consumer = new Thread(() -> {
            try {
                System.out.println("Consumed: " + buffer.take());
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }, "consumer");
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
