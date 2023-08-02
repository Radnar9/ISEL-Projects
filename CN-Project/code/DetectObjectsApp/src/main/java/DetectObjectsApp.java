import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;

public class DetectObjectsApp {
    private static Storage storage;
    private static String projectId;

    private static void initStorage() {
        // Get GOOGLE_APPLICATION_CREDENTIALS environment variable
        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        storage = storageOptions.getService();

        projectId = storageOptions.getProjectId();
        if (projectId != null) System.out.println("Current Project ID: " + projectId);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!");
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws IOException {
        initStorage();
        FirestoreOperations.initFirestore();
        Subscriber subscriber = PubSub.subscribeMessages(projectId, storage);
        System.out.println("Detect Objects App running...\n");
        subscriber.awaitTerminated();
    }
}
