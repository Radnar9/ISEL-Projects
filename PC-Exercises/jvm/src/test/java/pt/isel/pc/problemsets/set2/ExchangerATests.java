package pt.isel.pc.problemsets.set2;

import org.junit.Test;
import pt.isel.pc.problemsets.utils.TestHelper;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExchangerATests {
    private static final Duration TEST_DURATION = Duration.ofSeconds(10);

    @Test
    public void Throw_exception_if_exchange_value_is_null() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean didThrowException = new AtomicBoolean();
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> {
            try {
                ex.exchange(null);
            } catch (IllegalArgumentException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If exchange value is null an IllegalArgumentException is thrown", didThrowException.get());
    }

    @Test
    public void Exchange_two_values() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicReference<String> valueFromFirstThread = new AtomicReference<>();
        AtomicReference<String> valueFromSecondThread = new AtomicReference<>();
        String value0 = "Let's be naughty and save Santa the trip.";
        String value1 = "Ho ho ho";
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> valueFromFirstThread.set(ex.exchange(value0)));
        helper.createAndStart(1, (index, isDone) -> valueFromSecondThread.set(ex.exchange(value1)));
        helper.join();

        assertEquals("Value stored by thread 0, was returned through thread 1", value0, valueFromSecondThread.get());
        assertEquals("Value stored by thread 1, was returned through thread 0", value1, valueFromFirstThread.get());
    }

    @Test
    public void Exchange_two_values_between_different_methods() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicReference<String> valueFromFirstThread = new AtomicReference<>();
        AtomicReference<String> valueFromSecondThread = new AtomicReference<>();
        String value0 = "Let's be naughty and save Santa the trip.";
        String value1 = "Ho ho ho";
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> valueFromFirstThread.set(ex.exchange(value0)));
        helper.createAndStart(1, (index, isDone) -> valueFromSecondThread.set(ex.exchange(value1, 5000, TimeUnit.MILLISECONDS)));
        helper.join();

        assertEquals("Value stored by thread 0, was returned through thread 1", value0, valueFromSecondThread.get());
        assertEquals("Value stored by thread 1, was returned through thread 0", value1, valueFromFirstThread.get());
    }

    @Test
    public void Exchange_values_between_2_pairs() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<String> valueFromFirstThread = new AtomicReference<>();
        AtomicReference<String> valueFromThirdThread = new AtomicReference<>();
        AtomicReference<String> valueFromFourthThread = new AtomicReference<>();
        String value1 = "Imprisonment. What a curious principle. We confine the physical body, yet the mind is still free.";
        String value2 = "When people look up to you, you don't get to be selfish.";
        String value3 = "When you're going to change the world, don't ask for permission.";
        String value4 = "Loneliness is often the byproduct of a gifted mind.";
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> valueFromFirstThread.set(ex.exchange(value1)));
        helper.createAndStart(1, (index, isDone) -> {
            ex.exchange(value2);
            countDownLatch.countDown();
        });
        countDownLatch.await();
        helper.createAndStart(2, (index, isDone) -> valueFromThirdThread.set(ex.exchange(value3)));
        helper.createAndStart(3, (index, isDone) -> valueFromFourthThread.set(ex.exchange(value4)));
        helper.join();

        assertEquals("Value stored by thread 1, was returned through thread 0", value2, valueFromFirstThread.get());
        assertEquals("Value stored by thread 2, was returned through thread 3", value4, valueFromThirdThread.get());
        assertEquals("Value stored by thread 3, was returned through thread 2", value3, valueFromFourthThread.get());
    }

    @Test
    public void Exchanges_between_50_pairs() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicInteger value = new AtomicInteger();
        ConcurrentHashMap<Integer, Integer> valuesReturned = new ConcurrentHashMap<>();
        ExchangerA<Integer> ex = new ExchangerA<>();

        for (int i = 0; i < 2; i++) {
            helper.createAndStartMultiple(50, (index, isDone) -> {
                Integer val = value.incrementAndGet();
                valuesReturned.put(val, ex.exchange(val));
            });
        }
        helper.join();

        for (int i = 1; i <= 100; i++) {
            assertTrue("The key and the value returned don't match", i != valuesReturned.get(i));
        }
    }

    @Test
    public void Throw_exception_when_the_arguments_are_not_the_expected_in_timeout_version() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicInteger exceptionsThrown = new AtomicInteger();
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> {
            try {
                ex.exchange(null, 20, TimeUnit.MILLISECONDS);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.createAndStart(1, (index, isDone) -> {
            try {
                ex.exchange("Teste", -1, TimeUnit.MILLISECONDS);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.createAndStart(2, (index, isDone) -> {
            try {
                ex.exchange("Teste", 0, TimeUnit.MILLISECONDS);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.createAndStart(3, (index, isDone) -> {
            try {
                ex.exchange("Teste", 20, null);
            } catch (IllegalArgumentException e) {
                exceptionsThrown.incrementAndGet();
            }
        });
        helper.join();

        assertEquals("When an argument is not inserted correctly an IllegalArgumentException is thrown", 4, exceptionsThrown.get());
    }

    @Test
    public void Throws_TimeoutException_when_timeout_is_reached() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicBoolean didThrowException = new AtomicBoolean();
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) -> {
            try {
                ex.exchange("Teste", 5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                didThrowException.set(true);
            }
        });
        helper.join();

        assertTrue("If timeout is reached, a TimeoutException is thrown", didThrowException.get());
    }

    @Test
    public void Test_exchange_in_timout_version() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<String> valueFromFirstThread = new AtomicReference<>();
        AtomicReference<String> valueFromThirdThread = new AtomicReference<>();
        AtomicReference<String> valueFromFourthThread = new AtomicReference<>();
        String value1 = "Imprisonment. What a curious principle. We confine the physical body, yet the mind is still free.";
        String value2 = "When people look up to you, you don't get to be selfish.";
        String value3 = "When you're going to change the world, don't ask for permission.";
        String value4 = "Loneliness is often the byproduct of a gifted mind.";
        ExchangerA<String> ex = new ExchangerA<>();

        helper.createAndStart(0, (index, isDone) ->
                valueFromFirstThread.set(ex.exchange(value1, 5000, TimeUnit.MILLISECONDS))
        );
        helper.createAndStart(1, (index, isDone) -> {
            ex.exchange(value2, 5000, TimeUnit.MILLISECONDS);
            countDownLatch.countDown();
        });
        countDownLatch.await();
        helper.createAndStart(2, (index, isDone) ->
                valueFromThirdThread.set(ex.exchange(value3, 5000, TimeUnit.MILLISECONDS))
        );
        helper.createAndStart(3, (index, isDone) ->
                valueFromFourthThread.set(ex.exchange(value4, 5000, TimeUnit.MILLISECONDS))
        );
        helper.join();

        assertEquals("Value stored by thread 1, was returned through thread 0", value2, valueFromFirstThread.get());
        assertEquals("Value stored by thread 2, was returned through thread 3", value4, valueFromThirdThread.get());
        assertEquals("Value stored by thread 3, was returned through thread 2", value3, valueFromFourthThread.get());
    }

    @Test
    public void Exchanges_between_50_pairs_with_timeout() throws InterruptedException {
        TestHelper helper = new TestHelper(TEST_DURATION);
        AtomicInteger value = new AtomicInteger();
        ConcurrentHashMap<Integer, Integer> valuesReturned = new ConcurrentHashMap<>();
        ExchangerA<Integer> ex = new ExchangerA<>();

        for (int i = 0; i < 2; i++) {
            helper.createAndStartMultiple(50, (index, isDone) -> {
                Integer val = value.incrementAndGet();
                valuesReturned.put(val, ex.exchange(val, 1, TimeUnit.SECONDS));
            });
        }
        helper.join();

        for (int i = 1; i <= 100; i++) {
            assertTrue("The key and the value returned don't match", i != valuesReturned.get(i));
        }
    }
}
