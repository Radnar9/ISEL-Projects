package pt.isel.pc.problemsets.set2;

import java.util.concurrent.atomic.AtomicReference;

public class MessageBox<M> {

    private static class MsgHolder<M> {
        public M msg;
        public int lives;
        public MsgHolder(M msg, int lives) {
            this.msg = msg;
            this.lives = lives;
        }
    }

    private final AtomicReference<MsgHolder<M>> msgHolder = new AtomicReference<>(new MsgHolder<>(null, 0));

    /**
     * Publishes a new message in the system, replacing the previous message with the new one
     * @param m corresponds to the new message
     * @param lvs are the lives of the new message, namely the number of times that this message can be consumed
     */
    public void publish(M m, int lvs) {
        if (m == null) {
            throw new IllegalArgumentException("Message cannot be null");
        } else if (lvs <= 0) {
            throw new IllegalArgumentException("Lives must be > 0");
        }
        msgHolder.set(new MsgHolder<>(m, lvs));
    }

    /**
     * Tries to consume the message in case there are still lives available
     * @return the message if lives are greater than 0 and null otherwise
     */
    public M tryConsume() {
        MsgHolder<M> observedHolder = msgHolder.get();
        while (observedHolder.msg != null && observedHolder.lives > 0) {
            MsgHolder<M> newHolder = new MsgHolder<>(observedHolder.msg, observedHolder.lives - 1);
            if (msgHolder.compareAndSet(observedHolder, newHolder)) {
                return newHolder.msg;
            }
            observedHolder = msgHolder.get();
        }
        return null;
    }
}
