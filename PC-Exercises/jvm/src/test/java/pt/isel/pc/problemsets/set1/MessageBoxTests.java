package pt.isel.pc.problemsets.set1;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.CustomSync;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageBoxTests {

    private static final int N_OF_THREADS = 10;
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void All_waiting_threads_receive_the_message() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        final String message = "I am on a seafood diet, I see food and I eat it.";
        List<String> syncList = Collections.synchronizedList(new LinkedList<>());
        AtomicInteger threadsThatReceivedMessage = new AtomicInteger();

        MessageBox<String> messageBox = new MessageBox<>();
        helper.createAndStartMultiple(
                N_OF_THREADS,
                (ignore, isDone) -> {
                    sync.inc();
                    messageBox.waitForMessage(Long.MAX_VALUE).ifPresent(syncList::add);
                }
        );
        while (!sync.checkCounter(N_OF_THREADS));

        helper.createAndStart(
                N_OF_THREADS,
                (ignore, isDone) -> threadsThatReceivedMessage.set(messageBox.sendToAll(message))
        );
        helper.join();

        int numberOfThreads = threadsThatReceivedMessage.get();
        assertEquals("Number of threads that received the message must be 10", N_OF_THREADS, numberOfThreads);
        assertTrue("All threads returned the message delivered",
                syncList.stream().allMatch(elem -> elem.equals(message))
        );
        assertEquals("Number of threads that returned must be 10", N_OF_THREADS, syncList.size());
    }

    @Test
    public void Two_different_sendToAll_calls_deliver_different_messages() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        final String firstMessage = "I made a huge to-do list today. I just need to figure out who’s going to do it.";
        final String secondMessage = "Somebody said today that I am lazy. I nearly answered him.";
        AtomicInteger threadsThatReceivedFirstMessage = new AtomicInteger();
        AtomicInteger threadsThatReceivedSecondMessage = new AtomicInteger();
        AtomicReference<String> secondStringReceived = new AtomicReference<>();

        MessageBox<String> messageBox = new MessageBox<>();
        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> {
                sync.inc();
                messageBox.waitForMessage(Long.MAX_VALUE);
            }
        );
        while (!sync.checkCounter(N_OF_THREADS));
        sync.reset();
        helper.createAndStart(1, (ignore, isDone) -> {
            sync.inc();
            threadsThatReceivedFirstMessage.set(messageBox.sendToAll(firstMessage));
            messageBox.waitForMessage(Long.MAX_VALUE).ifPresent(secondStringReceived::set);
        });
        while (!sync.checkCounter(1));
        helper.createAndStart(1, (ignore, isDone) ->
            threadsThatReceivedSecondMessage.set(messageBox.sendToAll(secondMessage))
        );
        helper.join();

        int threadsFreed1 = threadsThatReceivedFirstMessage.get();
        int threadsFreed2 = threadsThatReceivedSecondMessage.get();
        String secondStr = secondStringReceived.get();
        assertEquals("10 threads received the first message", N_OF_THREADS, threadsFreed1);
        assertEquals("1 thread received the second message", 1, threadsFreed2);
        assertEquals("The second sendToAll delivered the secondMessage to the corresponding thread",
                secondMessage, secondStr);
    }

    @Test
    public void Call_to_sendToAll_without_waiting_threads() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CustomSync sync = new CustomSync();
        final int firstMessage = 1999;
        final int secondMessage = 2001;
        AtomicInteger threadsThatReceivedSecondMessage = new AtomicInteger();

        MessageBox<Integer> messageBox = new MessageBox<>();
        helper.createAndStartMultiple(N_OF_THREADS, (ignore, isDone) -> {
            sync.inc();
            messageBox.waitForMessage(Long.MAX_VALUE);
        });
        while (!sync.checkCounter(N_OF_THREADS));
        helper.createAndStart(1, (ignore, isDone) -> {
            messageBox.sendToAll(firstMessage);
            threadsThatReceivedSecondMessage.set(messageBox.sendToAll(secondMessage));
        });
        helper.join();

        int threadsFreed = threadsThatReceivedSecondMessage.get();
        assertEquals("0 threads received the second message", 0, threadsFreed);
    }

    @Test
    public void Timeout_reached_on_waiting_threads() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicReference<Optional<Integer>> empty = new AtomicReference<>();
        MessageBox<Integer> messageBox = new MessageBox<>();

        helper.createAndStart(1, (ignore, isDone) ->
            empty.set(messageBox.waitForMessage(5000))
        );
        helper.join();

        assertTrue("Timout returned an empty Optional", empty.get().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void Throws_exception_if_timeout_inserted_is_less_than_zero() throws InterruptedException {
        MessageBox<Integer> messageBox = new MessageBox<>();
        messageBox.waitForMessage(-1);
    }

    @Test
    public void Interrupt_thread() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicReference<Thread> thread = new AtomicReference<>();
        AtomicBoolean isInterrupted = new AtomicBoolean();
        MessageBox<Integer> messageBox = new MessageBox<>();

        helper.createAndStart(1, (index, isDone) -> {
            thread.set(Thread.currentThread());
            try {
                messageBox.waitForMessage(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                isInterrupted.set(true);
            }
        });
        helper.createAndStart(1, (index, isDone) -> thread.get().interrupt());
        helper.join();
        assertTrue("When thread is interrupted while waiting it throws InterruptedException", isInterrupted.get());
    }
}
