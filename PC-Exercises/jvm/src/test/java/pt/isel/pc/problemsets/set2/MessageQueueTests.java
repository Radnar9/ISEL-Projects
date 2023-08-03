package pt.isel.pc.problemsets.set2;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageQueueTests {
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void Wait_for_message() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageQueue<String> messageQueue = new MessageQueue<>();
        String msg = "Of course I talk to myself, sometimes I need expert advice.";
        AtomicReference<Optional<String>> optionalReturned = new AtomicReference<>();

        helper.createAndStart(0, (index, isDone) -> optionalReturned.set(messageQueue.dequeue(5000)));
        helper.createAndStart(1, (index, isDone) -> messageQueue.enqueue(msg));
        helper.join();

        String messageReturned = optionalReturned.get().isPresent() ? optionalReturned.get().get() : null;
        assertEquals("The message in the queue is returned", msg, messageReturned);
    }

    @Test
    public void Timeout_is_reached() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageQueue<String> messageQueue = new MessageQueue<>();
        AtomicReference<Optional<String>> optionalReturned = new AtomicReference<>();

        helper.createAndStart(0, (index, isDone) -> optionalReturned.set(messageQueue.dequeue(5000)));
        helper.join();

        assertTrue("When timeout is reached an empty Optional is returned", optionalReturned.get().isEmpty());
    }

    @Test
    public void Throw_exception_if_message_is_null_or_if_timeout_is_negative() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageQueue<String> messageQueue = new MessageQueue<>();
        AtomicInteger exceptionsThrown = new AtomicInteger();

        helper.createAndStart(0, (index, isDone) -> {
            try {
                messageQueue.enqueue(null);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.createAndStart(1, (index, isDone) -> {
            try {
                messageQueue.dequeue(-1);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.join();

        assertEquals("If message is null or if timeout is negative an IllegalArgumentException is thrown",
                2, exceptionsThrown.get());
    }

    @Test
    public void Dequeue_various_messages() throws InterruptedException {
        final int numberOfMessages = 10;
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageQueue<Integer> messageQueue = new MessageQueue<>();
        AtomicInteger value = new AtomicInteger();
        ConcurrentLinkedQueue<Optional<Integer>> resultsQueue = new ConcurrentLinkedQueue<>();

        helper.createAndStartMultiple(10, (index, isDone) -> messageQueue.enqueue(value.incrementAndGet()));
        helper.createAndStartMultiple(numberOfMessages, (index, isDone) -> resultsQueue.add(messageQueue.dequeue(5000)));
        helper.join();

        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < numberOfMessages; i++) {
           assertTrue("All values are inserted, meaning there are no repeated values", set.add(resultsQueue.poll().get()));
        }
    }
}
