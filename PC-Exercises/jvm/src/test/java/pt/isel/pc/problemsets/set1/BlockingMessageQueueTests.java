package pt.isel.pc.problemsets.set1;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.CustomSync;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class BlockingMessageQueueTests {

    private static final int N_OF_THREADS = 10;
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void Throw_exception_if_message_is_null() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicInteger exceptionCounter = new AtomicInteger(0);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(10);

        helper.createAndStart(1, (ignore, isDone) -> {
            try {
                messageQueue.enqueue(null, Long.MAX_VALUE);
            } catch (IllegalArgumentException e) {
                exceptionCounter.incrementAndGet();
            }
        });
        helper.join();

        assertEquals("Throws IllegalArgumentException when message is null", 1, exceptionCounter.get());
    }

    @Test
    public void Throw_exception_if_timeout_is_less_than_zero() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicInteger exceptionCounter = new AtomicInteger(0);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(10);

        helper.createAndStart(1, (ignore, isDone) -> {
            try {
                messageQueue.enqueue(9, -9);
            } catch (IllegalArgumentException e) {
                exceptionCounter.incrementAndGet();
            }
        });
        helper.join();

        assertEquals("Throws IllegalArgumentException when timeout is less than zero",
                1, exceptionCounter.get());
    }

    @Test
    public void Throw_exception_if_timeout_is_equal_to_zero_and_requestQueue_is_empty() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean bool = new AtomicBoolean();
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);

        helper.createAndStart(0, (ignore, isDone) -> messageQueue.enqueue(9, 0));
        helper.createAndStart(1, (ignore, isDone) -> bool.set(messageQueue.enqueue(2, 0)));
        helper.join();

        assertFalse("Returns false when timeout is equal to zero", bool.get());
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_capacity_is_less_than_zero() {
        new BlockingMessageQueue<Integer>(-1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void Throw_exception_if_capacity_is_equal_to_zero() {
        new BlockingMessageQueue<Integer>(0);
    }

    @Test
    public void Request_and_then_receive_the_response() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(5);
        int message = 9;
        AtomicInteger messageReturned = new AtomicInteger();

        helper.createAndStart(0, (ignore, isDone) -> messageReturned.set(messageQueue.dequeue().get()));
        helper.createAndStart(1, (ignore, isDone) -> messageQueue.enqueue(message, Long.MAX_VALUE));
        helper.join();

        assertEquals("The method get() of Future returned the message inserted", message, messageReturned.get());
    }

    @Test
    public void Leave_messageQueue_through_timeout() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean boolReturned = new AtomicBoolean();

        helper.createAndStart(0, (ignore, isDone) -> messageQueue.enqueue(9, Long.MAX_VALUE));
        helper.createAndStart(1, (ignore, isDone) -> boolReturned.set(messageQueue.enqueue(99, 5000)));
        helper.join();
        assertFalse("If timeout reaches it returns false", boolReturned.get());
    }

    @Test
    public void Return_completed_future() throws InterruptedException, ExecutionException, TimeoutException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        Integer message = 9;
        AtomicReference<Future<Integer>> futureReturned = new AtomicReference<>();

        helper.createAndStart(0, (ignore, isDone) -> {
            messageQueue.enqueue(message, Long.MAX_VALUE);
            futureReturned.set(messageQueue.dequeue());
        });
        helper.join();

        assertFalse("A completed Future is never cancelled", futureReturned.get().cancel(true));
        assertFalse("A completed Future is never cancelled", futureReturned.get().isCancelled());
        assertTrue("A completed Future is done", futureReturned.get().isDone());
        assertEquals("The get from a completed Future already has the value cause it is completed",
                message, futureReturned.get().get());
        assertEquals("The get with timeout behaves like it does not have one cause the Future is completed",
                message, futureReturned.get().get(50000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void Message_successfully_joins_the_messageQueue_when_space_is_available() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean boolReturned = new AtomicBoolean();

        helper.createAndStart(0, (ignore, isDone) -> {
            messageQueue.enqueue(9, Long.MAX_VALUE);
            boolReturned.set(messageQueue.enqueue(99, Long.MAX_VALUE));
        });
        helper.createAndStart(1, (ignore, isDone) -> messageQueue.dequeue());
        helper.join();

        assertTrue("When the waiting enqueue joins the message queue it returns true", boolReturned.get());
    }

    @Test
    public void Cancel_future_not_completed() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean successCancellation = new AtomicBoolean();
        AtomicBoolean isCancelled = new AtomicBoolean();
        AtomicBoolean exceptionWhenCalled = new AtomicBoolean();
        AtomicBoolean cancelCancelledFuture = new AtomicBoolean();

        helper.createAndStart(0, (ignore, isDone) -> {
            Future<Integer> future = messageQueue.dequeue();
            successCancellation.set(future.cancel(true));
            isCancelled.set(future.isCancelled());
            try {
                future.get();
            } catch (CancellationException e) {
                exceptionWhenCalled.set(true);
            }
            cancelCancelledFuture.set(future.cancel(true));
        });
        helper.join();

        assertTrue("Cancel future returns true cause it was successfully cancelled", successCancellation.get());
        assertTrue("Method isCancelled returns true", isCancelled.get());
        assertTrue("Since it is cancelled it throws exception when calling the get method", exceptionWhenCalled.get());
        assertFalse("When a cancelled future is cancelled again it returns false", cancelCancelledFuture.get());
    }

    @Test
    public void Future_get_timeout_method_throws_exception() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean throwsException = new AtomicBoolean();

        helper.createAndStart(0, (ignore, isDone) -> {
            Future<Integer> future = messageQueue.dequeue();
            try {
                future.get(5000, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throwsException.set(true);
            }
        });
        helper.join();

        assertTrue("When timeout expires it throws TimeoutException", throwsException.get());
    }

    @Test
    public void Throw_exception_when_waiting_thread_is_interrupted_in_enqueue() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean didThrowException = new AtomicBoolean();
        AtomicReference<Thread> thread = new AtomicReference<>();

        helper.createAndStart(0, (index, isDone) -> {
            sync.inc();
            messageQueue.enqueue(1, Long.MAX_VALUE);
        });
        while (!sync.checkCounter(1));
        sync.reset();
        helper.createAndStart(1, (index, isDone) -> {
            sync.inc();
            thread.set(Thread.currentThread());
            try {
                messageQueue.enqueue(9, Long.MAX_VALUE);
            } catch (InterruptedException e) {
                didThrowException.set(true);
            }
        });
        while (!sync.checkCounter(1));
        helper.createAndStart(0, (index, isDone) -> thread.get().interrupt());
        helper.join();

        assertTrue("When a waiting thread is interrupted, throws InterruptedException", didThrowException.get());
    }

    @Test
    public void Throw_exception_when_waiting_thread_is_interrupted_in_uncompleted_future() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(1);
        AtomicBoolean didThrowException = new AtomicBoolean();
        AtomicReference<Thread> thread = new AtomicReference<>();

        helper.createAndStart(1, (index, isDone) -> {
            thread.set(Thread.currentThread());
            try {
                sync.inc();
                messageQueue.dequeue().get();
            } catch (InterruptedException e) {
                didThrowException.set(true);
            }
        });
        while (!sync.checkCounter(1));
        helper.createAndStart(2, (index, isDone) -> thread.get().interrupt());
        helper.join();

        assertTrue("When a waiting thread is interrupted, throws InterruptedException", didThrowException.get());
    }

    @Test
    public void Dequeue_and_enqueue_multiple_threads() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        BlockingMessageQueue<Integer> messageQueue = new BlockingMessageQueue<>(5);
        List<Boolean> syncList = Collections.synchronizedList(new LinkedList<>());

        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            messageQueue.dequeue();
        });
        helper.createAndStartMultiple(N_OF_THREADS, (index, isDone) -> {
            syncList.add(messageQueue.enqueue(index, Long.MAX_VALUE));
        });
        helper.join();

        assertTrue("All enqueues return true", syncList.stream().allMatch(elem -> elem));
        assertEquals("The number of threads returned are the same number of threads created", N_OF_THREADS, syncList.size());
    }
}
