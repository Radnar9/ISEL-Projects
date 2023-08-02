import com.google.cloud.Timestamp;

public class Monitor {
    private int refMessages;
    private int refSeconds;
    private double threshold;
    private Timestamp firstMessageTimestamp;
    private int runningInstances;
    private int currentMessages;

    public Monitor() {}

    public Monitor(
            int refMessages,
            int refSeconds,
            double threshold,
            Timestamp firstMessageTimestamp,
            int runningInstances,
            int currentMessages
    ) {
        this.refMessages = refMessages;
        this.refSeconds = refSeconds;
        this.threshold = threshold;
        this.firstMessageTimestamp = firstMessageTimestamp;
        this.runningInstances = runningInstances;
        this.currentMessages = currentMessages;
    }

    public int getRefMessages() {
        return refMessages;
    }
    public int getRefSeconds() {
        return refSeconds;
    }
    public int getRefMilliseconds() {
        return refSeconds * 1000;
    }
    public double getThreshold() {
        return threshold;
    }
    public Timestamp getFirstMessageTimestamp() {
        return firstMessageTimestamp;
    }
    public int getRunningInstances() {
        return runningInstances;
    }
    public int getCurrentMessages() {
        return currentMessages;
    }
}
