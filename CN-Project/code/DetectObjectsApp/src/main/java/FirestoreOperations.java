import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import models.DetectedObject;
import models.PubSubMessage;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirestoreOperations {

    private static Firestore db;
    private static final String REQUESTS_COLLECTION = "Requests";
    private static final String DETECTED_OBJECTS_COLLECTION = "DetectedObjects";

    public static void initFirestore() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
            db = options.getService();
        } catch (IOException e) {
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            db.close();
        } catch (Exception e) {
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setDetectedObjectsInfo(
            PubSubMessage message,
            String annotatedBlob,
            List<DetectedObject> detectedObjects
    ) throws ExecutionException, InterruptedException {
        WriteBatch batch = db.batch();

        // Create request document
        CollectionReference requestsCollectionRef = db.collection(REQUESTS_COLLECTION);
        DocumentReference requestsDocRef = requestsCollectionRef.document(message.getId());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        HashMap<String, Object> requestMap = new HashMap<>() {
            {
                put("id", message.getId());
                put("imageName", message.getImageName());
                put("imageType", message.getImageType());
                put("bucket", message.getBucket());
                put("originalBlob", message.getBlob());
                put("annotatedBlob", annotatedBlob);
                put("detectedObjects", detectedObjects.size());
                put("creationTimestamp", timestamp);
            }
        };
        batch.set(requestsDocRef, requestMap);

        // Create detected objects documents
        CollectionReference objectsCollectionRef = db.collection(DETECTED_OBJECTS_COLLECTION);

        detectedObjects.forEach(obj -> {
            DocumentReference objectsDocRef = objectsCollectionRef.document();
            HashMap<String, Object> objectMap = new HashMap<>() {
                {
                    put("objectName", obj.getName());
                    put("score", obj.getScore());
                    put("vertices", obj.getObjectVertices());
                    put("requestId", message.getId());
                    put("creationTimestamp", timestamp);
                }
            };
            batch.set(objectsDocRef, objectMap);
        });

        // Waits if necessary for the computation to complete
        batch.commit().get();
    }
}
