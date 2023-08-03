package pt.isel.pc.problemsets.set2;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class MessageBoxTests {
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void Throw_exception_if_publish_arguments_are_invalid() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean msgDidThrowException = new AtomicBoolean();
        AtomicBoolean lvsZeroDidThrowException = new AtomicBoolean();
        AtomicBoolean lvsNegativeDidThrowException = new AtomicBoolean();
        MessageBox<String> msgBox = new MessageBox<>();

        helper.createAndStart(0, (index, isDone) -> {
            try {
                msgBox.publish(null, 1);
            } catch (IllegalArgumentException e) {
                msgDidThrowException.set(true);
            }
        });
        helper.createAndStart(1, (index, isDone) -> {
            try {
                msgBox.publish("Santa is coming!", 0);
            } catch (IllegalArgumentException e) {
                lvsZeroDidThrowException.set(true);
            }
        });
        helper.createAndStart(2, (index, isDone) -> {
            try {
                msgBox.publish("Santa is coming!", -9);
            } catch (IllegalArgumentException e) {
                lvsNegativeDidThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("Throws exception when the message inserted is null", msgDidThrowException.get());
        assertTrue("Throws exception when the lives inserted are zero", lvsZeroDidThrowException.get());
        assertTrue("Throws exception when the lives inserted are negative", lvsNegativeDidThrowException.get());
    }

    @Test
    public void Receive_message_five_times_with_six_threads() throws InterruptedException {
        final int lives = 5;
        TestHelper helper = new TestHelper(TEST_DURATION);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicInteger messagesReceived = new AtomicInteger();
        AtomicReference<String> nullReturned = new AtomicReference<>();
        MessageBox<String> msgBox = new MessageBox<>();

        helper.createAndStart(0, (index, isDone) -> {
            msgBox.publish("Teste", lives);
            countDownLatch.countDown();

        });
        countDownLatch.await();
        helper.createAndStartMultiple(lives + 1, (index, isDone) -> {
            String received = msgBox.tryConsume();
            if (received != null) {
                messagesReceived.incrementAndGet();
            } else {
                nullReturned.set(received);
            }
        });
        helper.join();

        assertEquals("Five threads return the message inserted", lives, messagesReceived.get());
        assertNull("One thread returns null since the lives were all consumed", nullReturned.get());
    }

    @Test
    public void Publish_different_messages() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageBox<Integer> msgBox = new MessageBox<>();
        ConcurrentLinkedQueue<Integer> valuesReturned1 = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Integer> valuesReturned9 = new ConcurrentLinkedQueue<>();
        Integer value1 = 1;
        Integer value9 = 9;

        helper.createAndStart(0, (index, isDone) -> msgBox.publish(value1, 10));
        helper.createAndStartMultiple(5, (index, isDone) -> valuesReturned1.add(msgBox.tryConsume()));
        helper.createAndStart(1, (index, isDone) -> msgBox.publish(value9, 20));
        helper.createAndStartMultiple(20, (index, isDone) -> valuesReturned9.add(msgBox.tryConsume()));
        helper.join();

        assertTrue("All values are 1", valuesReturned1.stream().allMatch(val -> val.equals(value1)));
        assertTrue("All values are 9", valuesReturned9.stream().allMatch(val -> val.equals(value9)));
    }

    @Test
    public void Returns_null_if_there_was_no_publish() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        MessageBox<String> msgBox = new MessageBox<>();
        AtomicReference<String> valueReturned = new AtomicReference<>();

        helper.createAndStart(0, (index, isDone) -> valueReturned.set(msgBox.tryConsume()));
        helper.join();

        assertNull("If there was no publish it returns null", valueReturned.get());
    }
}
