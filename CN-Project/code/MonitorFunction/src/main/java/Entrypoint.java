import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import java.util.Optional;
import java.util.logging.Logger;

public class Entrypoint implements BackgroundFunction<PubSubMessage> {
    private static final Logger logger = Logger.getLogger(Entrypoint.class.getName());
    private static final Firestore db = FirestoreOperations.initFirestore();
    private static final int MIN_RUNNING_INSTANCES = 1;
    private static final int MAX_RUNNING_INSTANCES = 4;
    private static final String PROJECT = "cn2122-t2-g09";
    private static final String INSTANCE_GROUP_NAME = "detect-objects-app-instance-group";
    private static final String INSTANCE_GROUP_ZONE = "europe-west1-b";

    @Override
    public void accept(PubSubMessage message, Context context) {
        if (db == null) {
            logger.severe("Error connecting to Firestore. Exiting function.");
            throw new RuntimeException("Error connecting to Firestore");
        }

        // Get from the Firestore the monitor document mapped to Monitor class
        Optional<Monitor> monitor = FirestoreOperations.getMonitor(db);
        if (monitor.isPresent()) {
            resizeInstanceGroup(monitor.get(), context.timestamp());
            return;
        }
        FirestoreOperations.initMonitor(db, context.timestamp());
    }

    private static void resizeInstanceGroup(Monitor monitor, String currentMessageTimestampStr) {
        // Verify if the currentMessage is less than one minute away from the firstMessage used as reference
        Timestamp currentMessageTimestamp = Timestamp.parseTimestamp(currentMessageTimestampStr);
        long currentMessageTime = currentMessageTimestamp.toDate().getTime();
        long firstMessageTime = monitor.getFirstMessageTimestamp().toDate().getTime();
        if (currentMessageTime - firstMessageTime < monitor.getRefMilliseconds()) {
            FirestoreOperations.incrementCurrentMessages(db, monitor.getCurrentMessages() + 1);
            return;
        }

        int instancesToAdd = 0;
        double reference = (double) monitor.getRefMessages() / monitor.getRefSeconds();
        double currentRef = (double) monitor.getCurrentMessages() / monitor.getRefSeconds();
        if (currentRef > reference + monitor.getThreshold() && monitor.getRunningInstances() < MAX_RUNNING_INSTANCES) {
            instancesToAdd = 1;
        } else if (currentRef < reference - monitor.getThreshold() && monitor.getRunningInstances() > MIN_RUNNING_INSTANCES) {
            instancesToAdd = -1;
        } else {
            String message = monitor.getRunningInstances() == MAX_RUNNING_INSTANCES ? "maximum" : "minimum";
            logger.info("The number of running instances are at its " + message + ", running: " + monitor.getRunningInstances());
        }

        int newNumberOfRunningInstances = monitor.getRunningInstances() + instancesToAdd;
        if (instancesToAdd != 0) {
            // Resize instance group
            logger.info("Resizing instance group from " + monitor.getRunningInstances() + " to " +
                    newNumberOfRunningInstances + " instances...");
            CloudComputeOperations.resizeInstaceGroup(PROJECT, INSTANCE_GROUP_ZONE, INSTANCE_GROUP_NAME, newNumberOfRunningInstances);
        }

        // Update monitor document in Firestore with the new values: running instances, currentMessages and firstMessageTimestamp
        FirestoreOperations.updateMonitor(db, newNumberOfRunningInstances, currentMessageTimestamp);
    }
}
