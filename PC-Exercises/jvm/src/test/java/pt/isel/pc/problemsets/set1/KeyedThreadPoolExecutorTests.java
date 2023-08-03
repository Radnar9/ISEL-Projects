package pt.isel.pc.problemsets.set1;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.CustomSync;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class KeyedThreadPoolExecutorTests {

    private static final int N_OF_THREADS = 10;
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    private static class ThreadWork implements Runnable {
        private CustomSync sync;
        private List<Integer> syncList;
        private Integer key;
        ThreadWork(CustomSync sync) {
            this.sync = sync;
        }

        ThreadWork() {}

        ThreadWork(List<Integer> syncList, Integer key) {
            this.syncList = syncList;
            this.key = key;
        }

        @Override
        public void run() {
            if (sync != null) {
                sync.inc();
            }
            System.out.println("I had a dream where an evil queen forced me to eat a gigantic marshmallow.\n"
                    + "When I woke up, my pillow was gone.");
            if (syncList != null) {
                syncList.add(key);
            }
        }
    }

    @Test
    public void Throw_exception_if_runnable_is_null() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean didThrowException = new AtomicBoolean();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStart(1, (index, isDone) -> {
            try {
                threadPool.execute(null, 1);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If runnable is null it throws the right exception", didThrowException.get());
    }

    @Test
    public void Throw_exception_if_key_is_null() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean didThrowException = new AtomicBoolean();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStart(1, (index, isDone) -> {
            try {
                threadPool.execute(new ThreadWork(), null);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If key is null it throws the right exception", didThrowException.get());
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_maxPoolSize_is_negative_or_equal_to_zero() {
        new KeyedThreadPoolExecutor(-1, 5000);
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_keepAliveTime_is_negative() {
        new KeyedThreadPoolExecutor(10, -5000);
    }

    @Test
    public void Throw_exception_if_awaitTermination_timeout_is_negative() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean didThrowException = new AtomicBoolean();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStart(1, (index, isDone) -> {
            try {
                threadPool.awaitTermination(-5000);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If timeout is negative it throws the right exception", didThrowException.get());
    }

    @Test
    public void Return_false_if_awaitTermination_timeout_is_zero_and_there_is_no_shutdown() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean returnValue = new AtomicBoolean();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStart(1, (index, isDone) -> {
            returnValue.set(threadPool.awaitTermination(0));
        });
        helper.join();

        assertFalse("When there is no shutdown and timeout is zero return false", returnValue.get());
    }

    @Test
    public void Shutdown_with_zero_threads_in_pool() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            sync.inc();
            syncList.add(threadPool.awaitTermination(Integer.MAX_VALUE));
        });
        while (!sync.checkCounter(N_OF_THREADS));
        helper.createAndStart(1, (index, isDone) -> threadPool.shutdown());
        helper.join();

        assertTrue("All threads returned true since shutdown was completed", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads created to be waiting are the same number of threads that returned",
                N_OF_THREADS, syncList.size());
    }

    @Test
    public void Waiting_threads_return_false_after_a_shutdown_has_been_completed() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            sync.inc();
            threadPool.awaitTermination(Integer.MAX_VALUE);
            sync.inc();
        });
        while (!sync.checkCounter(N_OF_THREADS));
        sync.reset();
        helper.createAndStart(1, (index, isDone) -> threadPool.shutdown());
        while (!sync.checkCounter(N_OF_THREADS));
        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> syncList.add(threadPool.awaitTermination(5000)));
        helper.join();

        assertFalse("All threads returned false since there wasn't a new shutdown", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads created to be waiting are the same number of threads that returned",
                N_OF_THREADS, syncList.size());
    }

    @Test
    public void Waiting_for_shutdown_threads_return_true_after_executing_threads_finish_their_task_and_keepAliveTime()
            throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            sync.inc();
            threadPool.execute(new ThreadWork(), index);
        });
        while(!sync.checkCounter(N_OF_THREADS));
        helper.createAndStart(1, (index, isDone) -> threadPool.shutdown());
        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> syncList.add(threadPool.awaitTermination(Integer.MAX_VALUE)));
        helper.join();

        assertTrue("All threads returned true since shutdown was completed", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads created to be waiting are the same number of threads that returned",
                N_OF_THREADS, syncList.size());
    }

    @Test
    public void Check_if_runnables_are_executed_only_once() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);

        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> threadPool.execute(new ThreadWork(sync), index));
        helper.join();
        while (!sync.checkCounter(N_OF_THREADS));
        assertEquals("The number of Runnables created is the same number of Runnables that ran", N_OF_THREADS,
                sync.getCounter());
    }

    private static class WaitRunnable implements Runnable {
        private final CustomSync sync;
        private final List<Integer> syncList;
        private final Integer key;
        WaitRunnable(CustomSync sync, List<Integer> syncList, Integer key) {
            this.sync = sync;
            this.syncList = syncList;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                sync.inc();
                Thread.sleep(5000);
                String msg = "When I see lovers' names carved in a tree, I don't think it's sweet. \n"
                        + "I just think it's surprising how many people bring a knife on a date.";
                System.out.println(msg);
                sync.inc();
                syncList.add(key);
            } catch (InterruptedException e) { /* Nothing to do */ }
        }
    }

    @Test
    public void Two_work_items_with_the_same_key_cannot_simultaneously_be_executing() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        KeyedThreadPoolExecutor threadPool = new KeyedThreadPoolExecutor(10, 5000);
        List<Integer> syncList = Collections.synchronizedList(new LinkedList<>());
        Integer key1 = 1;
        Integer key2 = 2;

        helper.createAndStart(0, (index, isDone) -> {
            threadPool.execute(new WaitRunnable(sync, syncList, key1), key1);
        });
        while (!sync.checkCounter(1));
        sync.reset();
        helper.createAndStart(1, (index, isDone) -> {
            threadPool.execute(new ThreadWork(syncList, key1), key1);
        });
        helper.createAndStart(2, (index, isDone) -> {
            threadPool.execute(new ThreadWork(syncList, key2), key2);
        });
        int listSize;
        do {
            listSize = syncList.size();
        } while (listSize < 3);
        helper.join();

        List<Integer> expectedList = Collections.synchronizedList(new LinkedList<>(List.of(key2, key1, key1)));
        List<Integer> actualList = syncList;
        assertEquals("List should return 3 elements corresponding to the three Runnables created",
                3, actualList.size());
        assertEquals("The order of the elements in the returned list must be equal to the expected so that keys"
                        + " are working correctly",
                expectedList, actualList);
    }
}
