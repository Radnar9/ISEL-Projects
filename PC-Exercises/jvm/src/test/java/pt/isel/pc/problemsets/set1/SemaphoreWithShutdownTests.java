package pt.isel.pc.problemsets.set1;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.CustomSync;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class SemaphoreWithShutdownTests {

    private static final int N_OF_THREADS = 10;
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void Fast_path_of_acquire() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);

        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) ->
                syncList.add(semaphore.acquireSingle(Long.MAX_VALUE))
        );
        helper.join();
        assertTrue("All threads return true", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads created are the same number of threads that returned true",
                N_OF_THREADS, syncList.size());
    }

    @Test (expected = IllegalStateException.class)
    public void Throw_exception_when_current_units_are_going_to_be_greater_than_initial_units() {
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);
        semaphore.releaseSingle();
    }

    @Test
    public void Throw_exception_if_timeout_is_less_than_zero() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);
        AtomicBoolean didThrowException = new AtomicBoolean();

        helper.createAndStart(1, (ignore, isDone) -> {
            try {
                semaphore.acquireSingle(-1);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If timeout is negative it throws the right exception", didThrowException.get());
    }

    @Test
    public void Zero_units_and_timeout_equal_to_zero() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean bool = new AtomicBoolean();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(1);

        helper.createAndStartMultiple(2, (ignore, isDone) -> bool.set(semaphore.acquireSingle(0)));
        helper.join();

        assertFalse("When timeout is reached, it is returned false", bool.get());
    }

    @Test
    public void Waiting_thread_leave_through_timeout() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean bool = new AtomicBoolean();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(1);

        helper.createAndStartMultiple(2, (ignore, isDone) -> bool.set(semaphore.acquireSingle(5000)));
        helper.join();

        assertFalse("When timeout is reached, it is returned false", bool.get());
    }

    @Test
    public void Waiting_threads_are_released() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(5);

        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> {
                sync.inc();
                syncList.add(semaphore.acquireSingle(Long.MAX_VALUE));
            }
        );
        while (!sync.checkCounter(N_OF_THREADS));
        helper.createAndStartMultiple(5, ((ignore, isDone) -> semaphore.releaseSingle()));
        helper.join();

        assertTrue("All threads return true", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads returned are the same number of threads created",
                N_OF_THREADS, syncList.size());
    }

    @Test
    public void Waiting_threads_throw_exception_in_shutdown() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(5);
        AtomicInteger exceptionCounter = new AtomicInteger(0);

        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> {
            sync.inc();
            try {
                semaphore.acquireSingle(Long.MAX_VALUE);
            } catch (CancellationException e) {
                exceptionCounter.incrementAndGet();
            }
        });
        while (!sync.checkCounter(N_OF_THREADS));
        semaphore.startShutdown();
        helper.join();

        assertEquals("The number of CancellationExceptions thrown are equal to the number of threads waiting",
                5, exceptionCounter.get());
    }

    @Test
    public void Waiting_for_shutdown_threads_return_false_when_timeout_is_reached() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);

        helper.createAndStart(1, (ignore, isDone) -> semaphore.acquireSingle(Long.MAX_VALUE));
        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> syncList.add(semaphore.waitShutdownCompleted(5000)));
        helper.join();

        assertFalse("All threads return false because timeout was reached before shutdown is completed",
                syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads created to be waiting are the same number of threads that returned",
                N_OF_THREADS, syncList.size());
    }

    @Test
    public void Waiting_for_shutdown_threads_return_true_when_shutdown_is_completed() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);

        helper.createAndStart(1, (ignore, isDone) -> semaphore.acquireSingle(Long.MAX_VALUE));
        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> {
            sync.inc();
            syncList.add(semaphore.waitShutdownCompleted(Long.MAX_VALUE));
        });
        while (!sync.checkCounter(N_OF_THREADS));
        helper.createAndStart(1, (index, isDone) -> {
            semaphore.startShutdown();
            semaphore.releaseSingle();
        });
        helper.join();

        assertTrue("All threads return true since shutdown is completed", syncList.stream().allMatch(elem -> elem));
        assertEquals("The list contains the number of threads that were waiting", N_OF_THREADS, syncList.size());
    }

    @Test
    public void Throw_exception_in_waitShutdownCompleted_if_timeout_is_less_than_zero() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(10);
        AtomicBoolean didThrowException = new AtomicBoolean();

        helper.createAndStart(1, (ignore, isDone) -> {
            try {
                semaphore.waitShutdownCompleted(-1);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If timeout is negative the exception is thrown", didThrowException.get());
    }

    @Test
    public void waitShutdownCompleted_timeout_equal_to_zero() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean bool = new AtomicBoolean();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(5);

        helper.createAndStart(1, (ignore, isDone) -> bool.set(semaphore.waitShutdownCompleted(0)));
        helper.join();

        assertFalse("When timeout is reached, it is returned false", bool.get());
    }

    @Test
    public void In_shutdown_new_acquires_are_cancelled() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(5);
        AtomicInteger exceptionCounter = new AtomicInteger();

        helper.createAndStart(1, (ignore, isDone) -> {
            semaphore.acquireSingle(Long.MAX_VALUE);
            semaphore.startShutdown();
        });
        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            try {
                semaphore.acquireSingle(Long.MAX_VALUE);
            } catch (CancellationException e) {
                exceptionCounter.incrementAndGet();
            }
        });
        helper.join();

        assertEquals("All threads that try to acquire throw CancellationException", N_OF_THREADS, exceptionCounter.get());
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_initialUnits_are_less_than_zero() {
        new SemaphoreWithShutdown(-1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_initialUnits_are_equal_to_zero() {
        new SemaphoreWithShutdown(0);
    }

    @Test
    public void Throw_exception_if_acquire_is_interrupted() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(1);
        AtomicReference<Thread> thread = new AtomicReference<>();
        AtomicBoolean didThrowException = new AtomicBoolean();

        helper.createAndStart(0, (index, isDone) -> {
            sync.inc();
            semaphore.acquireSingle(Long.MAX_VALUE);
        });
        while (!sync.checkCounter(1));
        sync.reset();
        helper.createAndStart(1, (index, isDone) -> {
            thread.set(Thread.currentThread());
            try {
                sync.inc();
                semaphore.acquireSingle(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                didThrowException.set(true);
            }
        });
        while (!sync.checkCounter(1));
        helper.createAndStart(1, (index, isDone) -> thread.get().interrupt());
        helper.join();

        assertTrue("If a waiting thread is interrupted, the exception is thrown", didThrowException.get());
    }

    @Test
    public void Throw_exception_if_waitShutdownCompleted_is_interrupted() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        SemaphoreWithShutdown semaphore = new SemaphoreWithShutdown(1);
        AtomicReference<Thread> thread = new AtomicReference<>();
        AtomicBoolean didThrowException = new AtomicBoolean();

        helper.createAndStart(0, (index, isDone) -> {
            sync.inc();
            thread.set(Thread.currentThread());
            try {
                semaphore.waitShutdownCompleted(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                didThrowException.set(true);
            }
        });
        while (!sync.checkCounter(1));
        helper.createAndStart(1, (index, isDone) -> thread.get().interrupt());
        helper.join();

        assertTrue("If a waiting thread is interrupted, the exception is thrown", didThrowException.get());
    }
}
