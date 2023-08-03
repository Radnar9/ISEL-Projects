package pt.isel.pc.problemsets.set1;

import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBox<T> {

    private static class ThreadsWaiting<T> {
        public int threadCounter;
        public T messageReceived;
        public final Condition waitMessage;

        public ThreadsWaiting(Lock lock) {
            waitMessage = lock.newCondition();
        }
    }

    private final Lock lock = new ReentrantLock();
    private ThreadsWaiting<T> waiter = new ThreadsWaiting<>(lock);

    /**
     * Calls to this method wait until a message is sent by sendToAll method
     * @param timeout is the maximum time that a thread can be waiting for a message
     * @return Optional containing the message sent or an Optional empty if timeout was reached
     * @throws InterruptedException if the thread was interrupted while it was waiting
     */
    public Optional<T> waitForMessage(long timeout) throws InterruptedException {
        lock.lock();
        try {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be >= 0");
            }

            if (Timeouts.noWait(timeout)) {
                return Optional.empty();
            }

            ThreadsWaiting<T> currentWaiter = waiter;
            currentWaiter.threadCounter += 1;

            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            while (true) {
                try {
                    currentWaiter.waitMessage.await(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (currentWaiter.messageReceived != null) {
                        Thread.currentThread().interrupt();
                        return Optional.of(completeTask(currentWaiter));
                    }
                    currentWaiter.threadCounter =- 1;
                    throw e;
                }

                if (currentWaiter.messageReceived != null) {
                    return Optional.of(completeTask(currentWaiter));
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    currentWaiter.threadCounter =- 1;
                    return Optional.empty();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sends a message to all the waiting threads
     * @param message to be sent to waiting threads
     * @return the number of threads that received the message
     */
    public int sendToAll(T message) {
        lock.lock();
        try {
            waiter.messageReceived = message;
            waiter.waitMessage.signalAll();
            int currentThreads = waiter.threadCounter;
            waiter = new ThreadsWaiting<>(lock);
            return currentThreads;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Completes the delivery of the message
     * @return the message sent
     */
    private T completeTask(ThreadsWaiting<T> waiter) {
        waiter.threadCounter--;
        return waiter.messageReceived;
    }
}