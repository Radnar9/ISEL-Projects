package pt.isel.pc.problemsets.set2;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Synchronized exchanger using lock-free technique
 * @param <V> corresponds to the type of the value
 */
public class ExchangerB<V> {

    private static class Holder<V> {
        public V firstValue;
        public V secondValue;
        public Holder(V value0) {
            firstValue = value0;
            secondValue = null;
        }
    }

    private final AtomicReference<Holder<V>> holder = new AtomicReference<>();

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
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        Holder<V> observedHolder = holder.get();
        Holder<V> newHolder = new Holder<>(value);
        while (true) {
            if (observedHolder != null) {
                if (holder.compareAndSet(observedHolder, null)) {
                    V valueToReturn = observedHolder.firstValue;
                    observedHolder.secondValue = value;
                    return valueToReturn;
                }
            }
            if (holder.compareAndSet(observedHolder, newHolder)) {
                while (true) {
                    if (newHolder.secondValue != null) {
                        return newHolder.secondValue;
                    }
                    Thread.yield();
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
            observedHolder = holder.get();
        }
    }
}