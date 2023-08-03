package pt.isel.pc.problemsets.set2;

import pt.isel.pc.problemsets.utils.Timeouts;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of a message queue, which queue is lock-free and was implemented
 * using the algorithm of Michael & Scott
 * @param <E> type of the messages inside the queue
 */
public class MessageQueue<E> {

    private static class Node<E> {
        E value;
        final AtomicReference<Node<E>> next;
        Node(E value) {
            this.value = value;
            next = new AtomicReference<>();
        }
    }

    private final AtomicReference<Node<E>> head;
    private final AtomicReference<Node<E>> tail;
    private final Object lock;
    private volatile int waiters;

    public MessageQueue() {
        Node<E> sentinel = new Node<>(null);
        head = new AtomicReference<>(sentinel);
        tail = new AtomicReference<>(sentinel);
        lock = new Object();
    }

    /**
     * Deliveries the message to the queue, and notifies a thread waiting for a message if there is one waiting
     * This method uses a FIFO criteria while queueing messages
     * @param message to be queued and then to be delivered to the consumer
     */
    public void enqueue(E message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        Node<E> newMessage = new Node<>(message);
        while (true) {
            Node<E> observedTail = tail.get();
            Node<E> observedNext = observedTail.next.get();

            if (observedNext != null) {
                tail.compareAndSet(observedTail, observedNext);
            } else if (observedTail.next.compareAndSet(null, newMessage)) {
                tail.compareAndSet(observedTail, newMessage);
                break;
            }
        }
        if (waiters != 0) {
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    /**
     * Auxiliary method to try to dequeue the head node
     * @return an Optional with the value within the node or in case of failure an empty one
     */
    private Optional<E> tryDequeue() {
        while (true) {
            Node<E> observedHead = head.get();
            Node<E> observedNext = observedHead.next.get();
            if (observedNext != null) {
                if (head.compareAndSet(observedHead, observedNext)) {
                    observedHead.next.set(null);
                    E valueToReturn = observedNext.value;
                    observedNext.value = null;
                    return Optional.of(valueToReturn);
                }
            } else {
                return Optional.empty();
            }
        }
    }

    /**
     * Dequeues the head message from the queue when one is available, otherwise waits until there is one
     * @param timeout is maximum time that a thread can wait for a message
     * @return an Optional with the message when there is one available before the timeout runs out,
     * or empty Optional otherwise
     * @throws InterruptedException if the thread was interrupted while it's waiting
     */
    public Optional<E> dequeue(long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be >= 0");
        }

        // Fast path
        Optional<E> message = tryDequeue();
        if (message.isPresent()) {
            return message;
        }

        if (Timeouts.noWait(timeout)) {
            return Optional.empty();
        }

        // Wait path
        synchronized (lock) {
            message = tryDequeue();
            if (message.isPresent()) {
                return message;
            }
            long deadline = Timeouts.deadlineFor(timeout);
            long remaining = Timeouts.remainingUntil(deadline);
            waiters += 1;
            while (true) {
                try {
                    lock.wait(remaining);
                } catch (InterruptedException e) {
                    waiters -= 1;
                    message = tryDequeue();
                    if (message.isPresent()) {
                        Thread.currentThread().interrupt();
                        return message;
                    }
                    throw e;
                }

                message = tryDequeue();
                if (message.isPresent()) {
                    break;
                }

                remaining = Timeouts.remainingUntil(deadline);
                if (Timeouts.isTimeout(remaining)) {
                    message = Optional.empty();
                    break;
                }
            }
            waiters -= 1;
            return message;
        }
    }
}
