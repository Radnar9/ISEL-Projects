import com.google.cloud.BatchResult;
import com.google.cloud.storage.*;

import java.util.UUID;

public class CloudStorage {
    private static final String DEFAULT_BUCKET = "cn2122tf";

    public static void deleteBlob(String bucket, StorageBatch batch, String blobName) {
        BlobId blobId = BlobId.of(bucket, blobName);
        batch.delete(blobId).notify(new BatchResult.Callback<>() {
            @Override
            public void success(Boolean aBoolean) {
                System.out.println("\t- Blob '" + blobName + "' successfully deleted.");
            }

            @Override
            public void error(StorageException e) {
                System.out.println("\t- Error deleting blob '" + blobName + "': " + e.getMessage());
            }
        });
    }

    /**
     * Verifies if a bucket name was provided, otherwise uses the default bucket name 'cn2122tf'.
     * Then it's verified if the bucket name exists, if it doesn't, a new bucket is created with the name of the
     * default bucket plus a random UUID, because the name of the bucket must be globally unique, and it's not checked
     * if the bucket name already exists globally, so it just creates a unique one.
     * @param args    - to verify if a bucket name was provided
     * @param storage - object to make changes on the cloud storage
     * @return the name of the bucket to be used in the system
     */
    public static String initBucket(String[] args, Storage storage) {
        String bucketName = args.length == 2 ? args[1] : DEFAULT_BUCKET;

        for (Bucket bucket : storage.list().iterateAll()) {
            if (bucket.getName().compareTo(bucketName) == 0) return bucketName;
        }

        bucketName = DEFAULT_BUCKET + '-' + UUID.randomUUID();
        System.out.println("\t- Creating bucket with name '" + bucketName + "'...");
        storage.create(BucketInfo.newBuilder(bucketName)
                        .setStorageClass(StorageClass.STANDARD)
                        .setLocation("EUROPE-WEST1")
                        .build());
        System.out.println("\t- Bucket '" + bucketName + "' successfully created.");
        return bucketName;
    }
}
