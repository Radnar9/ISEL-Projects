package pt.isel.pc.problemsets.set1;

import pt.isel.pc.problemsets.utils.NodeLinkedList;
import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingMessageQueue<E> {
    private enum State {
        DONE,
        CANCELLED,
        NORMAL
    }

    public static class DequeueRequest<E> {
        public final Condition condition;
        public E message;
        public State state;

        public DequeueRequest(Lock monitor) {
            condition = monitor.newCondition();
            state = State.NORMAL;
        }
    }

    public static class EnqueueMessage<E> {
        public final Condition condition;
        public final E message;

        public EnqueueMessage(Lock monitor, E message) {
            condition = monitor.newCondition();
            this.message = message;
        }
    }

    private final Lock monitor = new ReentrantLock();

    /**
     * messageQueue - for messages if queue is not full
     */
    private final NodeLinkedList<E> messagesQueue = new NodeLinkedList<>();

    /**
     * messagesWaitingEnqueue - if the queue is full then goes to this list
     */
    private final NodeLinkedList<EnqueueMessage<E>> messagesWaitingEnqueue = new NodeLinkedList<>();

    /**
     * requestQueue - stores the consumer's requests
     */
    private final NodeLinkedList<DequeueRequest<E>> requestQueue = new NodeLinkedList<>();

    private final int maxCapacity;

    public BlockingMessageQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        maxCapacity = capacity;
    }

    /**
     * Deliveries a message to the queue if there is not a request waiting, or if the queue is full: waits until
     * timeout is reached or until there is an available space in the queue
     * @param message to be delivered to the consumer
     * @param timeout is the maximum time that a message can wait to be placed in the queue
     * @return true if the message was enqueued or delivered to the consumer, false if timeout was reached
     * @throws InterruptedException if a thread was interrupted during its wait
     */
    public boolean enqueue(E message, long timeout) throws InterruptedException {
        monitor.lock();
        try {
            if (timeout < 0) {
                throw new IllegalArgumentException("timeout must be >= 0");
            } else if (message == null) {
                throw new IllegalArgumentException("message cannot be null");
            }

            // Fast path
            if (requestQueue.isNotEmpty()) {
                var request = requestQueue.pull();
                request.value.message = message;
                request.value.state = State.DONE;
                request.value.condition.signal();
                return true;
            }

            if (messagesQueue.getCount() < maxCapacity) {
                messagesQueue.enqueue(message);
                return true;
            }

            if(Timeouts.noWait(timeout)) {
                return false;
            }

            // Wait path
            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            var messageNode = messagesWaitingEnqueue.enqueue(new EnqueueMessage<>(monitor, message));
            while (true) {
                try {
                    messageNode.value.condition.await(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    if (messagesQueue.getCount() < maxCapacity) {
                        messagesQueue.enqueue(messageNode.value.message);
                        messagesWaitingEnqueue.remove(messageNode);
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    messagesWaitingEnqueue.remove(messageNode);
                    throw e;
                }

                if (messagesQueue.getCount() < maxCapacity) {
                    messagesQueue.enqueue(messageNode.value.message);
                    messagesWaitingEnqueue.remove(messageNode);
                    return true;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    messagesWaitingEnqueue.remove(messageNode);
                    return false;
                }
            }
        } finally {
            monitor.unlock();
        }
    }

    /**
     * Removes a message from the queue if available, otherwise waits for one
     * @return a Future object representing an operation to be completed
     */
    public Future<E> dequeue() {
        monitor.lock();
        try {
            // Fast path
            if (messagesQueue.isNotEmpty()) {
                var message = messagesQueue.pull();
                if (messagesWaitingEnqueue.isNotEmpty()) {
                    messagesWaitingEnqueue.getHeadValue().condition.signal();
                }
                return new CompletedFuture<>(message.value);
            }

            // Wait path
            var requestNode = requestQueue.enqueue(new DequeueRequest<>(monitor));
            return new NodeFuture(requestNode);
        } finally {
            monitor.unlock();
        }
    }

    /**
     * A Future that is already completed when created.
     */
    private static class CompletedFuture<E> implements Future<E> {

        private final E message;

        CompletedFuture(E message) {
            this.message = message;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public E get() {
            return message;
        }

        @Override
        public E get(long timeout, TimeUnit unit) {
            return message;
        }
    }

    /**
     * A Future that refers to a node in the requestQueue.
     * This is an inner class, so that it can access the fields of the creating object, namely the monitor.
     * All methods acquire the lock of the associated operation object.
     */
    private class NodeFuture implements Future<E> {

        private final NodeLinkedList.Node<DequeueRequest<E>> requestNode;

        NodeFuture(NodeLinkedList.Node<DequeueRequest<E>> requestNode) {
            this.requestNode = requestNode;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            monitor.lock();
            try {
                if (requestNode.value.state != State.NORMAL) {
                    return false;
                }
                requestNode.value.state = State.CANCELLED;
                requestQueue.remove(requestNode);
                requestNode.value.condition.signal();
                return true;
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public boolean isCancelled() {
            monitor.lock();
            try {
                return requestNode.value.state == State.CANCELLED;
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public boolean isDone() {
            monitor.lock();
            try {
                return requestNode.value.state == State.DONE;
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public E get() throws InterruptedException {
            monitor.lock();
            try {
                while (true) {
                    if (requestNode.value.state == State.CANCELLED) {
                        throw new CancellationException("Task cancelled!");
                    }
                    if (requestNode.value.state == State.DONE) {
                        return requestNode.value.message;
                    }
                    requestNode.value.condition.await();
                    // no catch because there are no actions needed on withdrawal due to InterruptedException
                }
            } finally {
                monitor.unlock();
            }
        }

        @Override
        public E get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            monitor.lock();
            try {
                long deadline = Timeouts.deadlineFor(timeout);
                while (true) {
                    if (timeout < 0) {
                        throw new IllegalArgumentException("timeout must be >= 0");
                    }

                    if (requestNode.value.state == State.CANCELLED) {
                        throw new CancellationException("Task cancelled!");
                    }
                    if (requestNode.value.state == State.DONE) {
                        return requestNode.value.message;
                    }

                    long remaining = Timeouts.remainingUntil(deadline);
                    if (Timeouts.isTimeout(remaining)) {
                        throw new TimeoutException("The wait timed out.");
                    }
                    requestNode.value.condition.await(remaining, unit);
                    // no catch because there are no actions needed on withdrawal due to InterruptedException
                }
            } finally {
                monitor.unlock();
            }
        }
    }
}
