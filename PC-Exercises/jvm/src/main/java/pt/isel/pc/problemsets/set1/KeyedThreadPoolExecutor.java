package pt.isel.pc.problemsets.set1;

import pt.isel.pc.problemsets.utils.NodeLinkedList;
import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KeyedThreadPoolExecutor {

    private static class Work {
        private final Runnable runnable;
        private final Object key;

        public Work(Runnable runnable, Object key) {
            this.runnable = runnable;
            this.key = key;
        }
    }

    private final NodeLinkedList<Work> workItems = new NodeLinkedList<>();
    private final Lock lock = new ReentrantLock();
    private final Condition waitForWork = lock.newCondition();
    private final Condition waitForShutdown = lock.newCondition();
    private final Set<Object> objectSet = Collections.synchronizedSet(new HashSet<>());
    private final int maxPoolSize;
    private final int keepAliveTime;

    private int currentPoolSize;
    private int inactiveThreadsCounter;
    private int threadsWaitingForShutdownCounter;

    private enum State {
        SHUTTING_DOWN,
        SHUTDOWN_COMPLETED,
        NORMAL
    }
    private State state;

    public KeyedThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be > 0");
        }
        if (keepAliveTime < 0) {
            throw new IllegalArgumentException("keepAliveTime must be >= 0");
        }
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        state = State.NORMAL;
    }

    /**
     * Deliveries work to available threads. If there are inactive threads or if all worker threads are busy, then the
     * work is placed in the list workItems to be run by the next available thread. A thread is never executing a task
     * with the same key of other task that is already executing
     * @param runnable corresponds to the work to be done by a designated thread
     * @param key that each runnable must have, so that is not possible to have executing runnables with the same key
     */
    public void execute(Runnable runnable, Object key) {
        lock.lock();
        try {
            if (runnable == null || key == null) {
                throw new IllegalArgumentException("Arguments cannot be null!");
            }
            if (state == State.SHUTTING_DOWN) {
                throw new RejectedExecutionException("ThreadPool is shutting down.");
            }

            Work newWork = new Work(runnable, key);
            if (currentPoolSize < maxPoolSize && inactiveThreadsCounter == 0 && objectSet.add(key)) {
                currentPoolSize += 1;
                var th = new Thread(() -> threadLoop(newWork));
                th.start();
            } else {
                workItems.enqueue(newWork);
                if (inactiveThreadsCounter > 0) {
                    waitForWork.signal();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Changes the state of the thread pool to the shutdown state. All tasks submitted before shutdown started
     * are processed normally.
     */
    public void shutdown() {
        lock.lock();
        try {
            if (currentPoolSize == 0) {
                state = State.SHUTDOWN_COMPLETED;
                waitForShutdown.signalAll();
            } else {
                state = State.SHUTTING_DOWN;
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Synchronizes threads with the shutdown of the executor. The shutdown process is completed when all accepted
     * tasks have been executed and all worker threads have terminated.
     * @param timeout maximum time a thread can be waiting
     * @return true if shutdown was completed, false if timeout was reached before shutdown is completed
     * @throws InterruptedException if a thread was interrupted while it was waiting
     */
    public boolean awaitTermination(int timeout) throws InterruptedException {
        lock.lock();
        try {
            threadsWaitingForShutdownCounter += 1;
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be >= 0");
            }
            if (Timeouts.noWait(timeout)) {
                return state == State.SHUTDOWN_COMPLETED;
            }

            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            while (true) {
                try {
                    waitForShutdown.await(remaining, TimeUnit.MILLISECONDS);
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
            lock.unlock();
        }
    }

    /**
     * Gets work if available, if the key of the head node is already executing and the list workItems is greater than
     * one, then we iterate through this list to find a Runnable which key isn't already executing
     * @return the work value to be done, or empty if there isn't one
     */
    private Optional<Work> getNextWorkItem() {
        if (workItems.isNotEmpty()) {
            if (objectSet.add(workItems.getHeadValue().key)) {
                return Optional.of(workItems.pull().value);
            } else if (workItems.getCount() > 1) {
                NodeLinkedList.Node<Work> head = workItems.getHeadNode();
                NodeLinkedList.Node<Work> next = workItems.next(head);
                while (head != next) {
                    if (objectSet.add(next.value.key)) {
                        workItems.remove(next);
                        return Optional.of(next.value);
                    }
                    next = workItems.next(next);
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if there is work to be done by the working thread that finished its previous work, otherwise waits for
     * timeout or another work to be available
     * @return an Optional with the task or an empty one
     */
    private Optional<Work> getWork() {
        lock.lock();
        try {
            // Fast path
            Optional<Work> work = getNextWorkItem();
            if (work.isPresent()) {
                return work;
            }
            if (Timeouts.noWait(keepAliveTime)) {
                currentPoolSize -= 1;
                return Optional.empty();
            }

            // Wait path
            inactiveThreadsCounter += 1;
            while (true) {
                long deadline = Timeouts.deadlineFor(keepAliveTime);
                long remaining = Timeouts.remainingUntil(deadline);
                try {
                    waitForWork.await(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    currentPoolSize -= 1;
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }

                work = getNextWorkItem();
                if (work.isPresent()) {
                    return work;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    currentPoolSize -= 1;
                    return Optional.empty();
                }
            }
        } finally {
            inactiveThreadsCounter -= 1;
            if (state == State.SHUTTING_DOWN && currentPoolSize == 0) {
                if (threadsWaitingForShutdownCounter > 0) {
                    state = State.SHUTDOWN_COMPLETED;
                    waitForShutdown.signalAll();
                } else {
                    state = State.NORMAL;
                }
            }
            lock.unlock();
        }
    }

    /**
     * A thread keeps executing this method until reach keepAliveTime, during its inactive time.
     * @param work task to be done
     */
    private void threadLoop(Work work) {
        work.runnable.run();
        objectSet.remove(work.key);

        while (true) {
            var maybeWork = getWork();
            maybeWork.ifPresent(newWork -> {
                newWork.runnable.run();
                objectSet.remove(newWork.key);
            });
            if (maybeWork.isEmpty()) {
                return;
            }
        }
    }
}
