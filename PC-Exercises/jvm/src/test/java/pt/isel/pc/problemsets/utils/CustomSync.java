package pt.isel.pc.problemsets.utils;

public class CustomSync {

    private final Object lock;
    private int counter;

    public CustomSync() {
        counter = 0;
        lock = new Object();
    }

    public void inc() {
        synchronized (lock) {
            counter++;
        }
    }

    public void dec() {
        synchronized (lock) {
            counter--;
        }
    }

    public boolean checkCounter(int max) {
        synchronized (lock) {
            return counter >= max;
        }
    }

    public void reset() {
        synchronized (lock) {
            counter = 0;
        }
    }

    public int getCounter() {
        synchronized (lock) {
            return counter;
        }
    }
}
