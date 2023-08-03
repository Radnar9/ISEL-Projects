package pt.isel.pc.problemsets.set2;

import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Synchronized exchanger using monitors
 * @param <V> corresponds to the type of the value
 */
public class ExchangerA<V> {

    private static class Request<V> {
        public V value;
        public Request(V value) {
            this.value = value;
        }
    }

    private final Object lock = new Object();
    private Request<V> request;

    /**
     * Auxiliar method to verify if there is a thread waiting to exchange
     * @param value to exchange
     * @return an Optional with the value of the waiting thread, or an empty Optional if there isn't a thread waiting
     */
    private Optional<V> isThreadWaiting(V value) {
        if (request != null) {
            V valueToReturn = request.value;
            request.value = value;
            request = null;
            lock.notify();
            return Optional.of(valueToReturn);
        }
        return Optional.empty();
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless the current thread is interrupted),
     * and then transfers the given object to it, receiving its object in return.
     * If another thread is already waiting at the exchange point then it is resumed for thread scheduling purposes
     * and receives the object passed in by the current thread. The current thread returns immediately, receiving the
     * object passed to the exchange by that other thread.
     * @param value to exchange
     * @return object provided by the other thread
     * @throws InterruptedException if thread was interrupted while waiting
     */
    public V exchange(V value) throws InterruptedException {
        synchronized (lock) {
            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null");
            }

            Optional<V> returnedValue = isThreadWaiting(value);
            if (returnedValue.isPresent()) {
                return returnedValue.get();
            }

            request = new Request<>(value);
            Request<V> currentRequest = request;
            while (true) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    if (currentRequest != request) {
                        Thread.currentThread().interrupt();
                        return currentRequest.value;
                    }
                    throw e;
                }
                if (currentRequest != request) {
                    return currentRequest.value;
                }
            }
        }
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless the current thread is interrupted or the
     * specified waiting time elapses), and then transfers the given object to it, receiving its object in return.
     * @param value to exchange
     * @param timeout is the maximum time to wait
     * @param unit corresponds to the time unit of the timeout argument
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the specified waiting time elapses before another thread enters the exchange
     */
    public V exchange(V value, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be > 0");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        synchronized (lock) {
            Optional<V> returnedValue = isThreadWaiting(value);
            if (returnedValue.isPresent()) {
                return returnedValue.get();
            }

            request = new Request<>(value);
            Request<V> currentRequest = request;
            long deadline = Timeouts.deadlineFor(timeout, unit);
            long remaining = Timeouts.remainingUntil(deadline);
            while (true) {
                try {
                    lock.wait(remaining);
                } catch (InterruptedException e) {
                    if (currentRequest != request) {
                        Thread.currentThread().interrupt();
                        return currentRequest.value;
                    }
                    throw e;
                }

                if (currentRequest != request) {
                    return currentRequest.value;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    throw new TimeoutException();
                }
            }
        }
    }
}