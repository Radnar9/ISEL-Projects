package pt.isel.pc.problemsets.set1;

import pt.isel.pc.problemsets.utils.NodeLinkedList;
import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreWithShutdown {

    public static class Request {
        public final Condition condition;
        public boolean isDone;

        public Request(Lock monitor) {
            condition = monitor.newCondition();
            isDone = false;
        }
    }

    private final Lock monitor = new ReentrantLock();
    private final Condition waitShutdown = monitor.newCondition();
    private final NodeLinkedList<Request> queue = new NodeLinkedList<>();
    private final int initialUnits;

    private int currentUnits;
    private int threadsToShutdown;
    private int threadsWaitingForShutdownCounter;

    private enum State {
        SHUTTING_DOWN,
        SHUTDOWN_COMPLETED,
        NORMAL
    }
    private State state;

    public SemaphoreWithShutdown(int initialUnits) {
        if (initialUnits <= 0) {
            throw new IllegalArgumentException("initial units must be > 0");
        }
        this.initialUnits = initialUnits;
        currentUnits = initialUnits;
        state = State.NORMAL;
    }

    /**
     * Calls to this method remove a unit if available, otherwise wait for one
     * @param timeout is the maximum time that a thread can be waiting for a unit
     * @return true if a unit was acquired, false if the timeout was reached
     * @throws InterruptedException when the thread is interrupted
     * @throws CancellationException when the semaphore is shutting down
     */
    public boolean acquireSingle(long timeout) throws InterruptedException, CancellationException {
        monitor.lock();
        try {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be >= 0");
            }

            // Fast path
            if (state == State.SHUTTING_DOWN) {
                throw new CancellationException("Shutting down! Operation cancelled.");
            }
            if (queue.isEmpty() && currentUnits > 0) {
                currentUnits -= 1;
                return true;
            }

            if (Timeouts.noWait(timeout)) {
                return false;
            }
            // Wait path
            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            var myNode = queue.enqueue(new Request(monitor));

            while (true) {
                try {
                    myNode.value.condition.await(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (myNode.value.isDone) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    queue.remove(myNode);
                    completePossibleRequest();
                    throw e;
                }

                if (state == State.SHUTTING_DOWN) {
                    threadsToShutdown -= 1;
                    checkIfAllThreadsHaveBeenCancelled();
                    throw new CancellationException("Shutting down! Operation cancelled.");
                }

                if (myNode.value.isDone) {
                    return true;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    queue.remove(myNode);
                    completePossibleRequest();
                    return false;
                }
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     *  Adds a single unit and releases possible waiting requests.
     *  In shutdown process, if the condition to be completed is verified then it is completed, therefore all
     *  threads waiting for this process to be completed are released.
     */
    public void releaseSingle() {
        monitor.lock();
        try {
            if (currentUnits + 1 > initialUnits) {
                throw new IllegalStateException("Current units limit exceed! Current units need to be <= Initial units");
            }

            currentUnits += 1;
            if (state == State.SHUTTING_DOWN && currentUnits == initialUnits) {
                checkIfAllThreadsHaveBeenCancelled();
            } else if (state != State.SHUTTING_DOWN) {
                completePossibleRequest();
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     *  Put semaphore in a shutdown state.
     *  If the condition to complete the shutdown process is verified then it is completed, therefore all
     *  threads waiting for this process to be completed are released.
     */
    public void startShutdown() {
        monitor.lock();
        try {
            state = State.SHUTTING_DOWN;
            threadsToShutdown = queue.getCount();
            releaseBlockedThreads();
            if (currentUnits == initialUnits) {
                checkIfAllThreadsHaveBeenCancelled();
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Calls to this method wait for the shutdown process to be concluded.
     * @param timeout is the maximum time that a thread can be waiting
     * @return false if the timeout was reached, true otherwise.
     * @throws InterruptedException in case the thread is interrupted
     */
    public boolean waitShutdownCompleted(long timeout) throws InterruptedException {
        monitor.lock();
        try {
            threadsWaitingForShutdownCounter += 1;
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be >= 0");
            }
            if (Timeouts.noWait(timeout)) {
                return false;
            }

            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            while (true) {
                try {
                    waitShutdown.await(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (state == State.SHUTDOWN_COMPLETED) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    throw e;
                }

                if (state == State.SHUTDOWN_COMPLETED) {
                    return true;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    return false;
                }
            }
        } finally {
            if (--threadsWaitingForShutdownCounter == 0 && state == State.SHUTDOWN_COMPLETED) {
                state = State.NORMAL;
            }
            monitor.unlock();
        }
    }

    /**
     * Completes requests in a FIFO order
     */
    private void completePossibleRequest() {
        // As it's unary, it's impossible to complete more than one request per release or acquire.
        if (queue.isNotEmpty() && currentUnits > 0) {
            Request headRequest = queue.pull().value;
            headRequest.isDone = true;
            currentUnits -= 1;
            headRequest.condition.signal();
        }
    }

    /**
     * Releases the threads waiting for a unit in FIFO order during the shutdown process
     */
    private void releaseBlockedThreads() {
        while (queue.isNotEmpty()) {
            queue.pull().value.condition.signal();
        }
    }

    /**
     * Auxiliary function to check if all threads have been already cancelled, if so turns off isShuttingDown flag and
     * releases all threads waiting for the shutdown process to be completed.
     */
    private void checkIfAllThreadsHaveBeenCancelled() {
        if (threadsToShutdown == 0) {
            if (threadsWaitingForShutdownCounter > 0) {
                state = State.SHUTDOWN_COMPLETED;
                waitShutdown.signalAll();
            } else {
                state = State.NORMAL;
            }
        }
    }
}