import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class FirestoreOperations {
    private static final Logger logger = Logger.getLogger(FirestoreOperations.class.getName());
    private static final String MONITOR_COLLECTION = "Monitor";
    private static final String MONITOR_DOCUMENT_ID = "properties";
    private static final int DEFAULT_REF_MESSAGES = 3;
    private static final int DEFAULT_REF_SECONDS = 60;
    private static final double DEFAULT_THRESHOLD = 0.02;
    private static final int DEFAULT_RUNNING_INSTANCES = 1;
    private static final int DEFAULT_CURRENT_MESSAGES = 1;


    public static Firestore initFirestore() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
            return options.getService();
        } catch (IOException e) {
            logger.severe(e.toString());
        }
        return null;
    }

    public static Optional<Monitor> getMonitor(Firestore db) {
        try {
            DocumentReference docRef = db.collection(MONITOR_COLLECTION).document(MONITOR_DOCUMENT_ID);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) return Optional.ofNullable(document.toObject(Monitor.class));
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void initMonitor(Firestore db, String timestamp) {
        DocumentReference docRef = db.collection(MONITOR_COLLECTION).document(MONITOR_DOCUMENT_ID);
        try {
            Map<String, Object> map = new HashMap<>() {{
                put("refMessages", DEFAULT_REF_MESSAGES);
                put("refSeconds", DEFAULT_REF_SECONDS);
                put("threshold", DEFAULT_THRESHOLD);
                put("firstMessageTimestamp", Timestamp.parseTimestamp(timestamp));
                put("runningInstances", DEFAULT_RUNNING_INSTANCES);
                put("currentMessages", DEFAULT_CURRENT_MESSAGES);
            }};

            ApiFuture<WriteResult> result = docRef.set(map);
            logger.info("Added the default monitor document to Firestore at " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void incrementCurrentMessages(Firestore db, int updatedCurrentMessages) {
        DocumentReference docRef = db.collection(MONITOR_COLLECTION).document(MONITOR_DOCUMENT_ID);
        Map<String, Object> map = new HashMap<>() {{
           put("currentMessages", updatedCurrentMessages);
        }};
        ApiFuture<WriteResult> result = docRef.update(map);
        try {
            logger.info("Current messages updated to " + updatedCurrentMessages + " at " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static void updateMonitor(Firestore db, int newNumberOfRunningInstances, Timestamp firstMessageTimestamp) {
        DocumentReference docRef = db.collection(MONITOR_COLLECTION).document(MONITOR_DOCUMENT_ID);
        Map<String, Object> map = new HashMap<>() {{
            put("currentMessages", DEFAULT_CURRENT_MESSAGES);
            put("runningInstances", newNumberOfRunningInstances);
            put("firstMessageTimestamp", firstMessageTimestamp);
        }};
        ApiFuture<WriteResult> result = docRef.update(map);
        try {
            logger.info("Monitor updated at " + result.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            logger.severe(e.toString());
            throw new RuntimeException(e);
        }
    }
}
