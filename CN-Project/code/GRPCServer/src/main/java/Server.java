import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.*;
import grpcserver.*;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import models.DetectedObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static models.DetectedObject.getDetectedObjectsAppearances;
import static utils.OnErrorTemplate.sendInternalError;
import static utils.OnErrorTemplate.sendNotFoundError;

public class Server extends ServerGrpc.ServerImplBase {
    private static Storage storage;
    private static String bucket;
    private static Firestore db;
    private static String projectId;
    private static final String REQUESTS_COLLECTION = "Requests";
    private static final String DETECTED_OBJECTS_COLLECTION = "DetectedObjects";

    @Override
    public StreamObserver<ImageUploadDownload> uploadImage(StreamObserver<ImageResponse> responseObserver) {
        System.out.println("Request to upload image received.");
        return new ServerStreamObserverUpload(responseObserver, storage, projectId);
    }

    @Override
    public void getImageDetectedObjects(ImageIdentifier request, StreamObserver<ImageObjects> responseObserver) {
        try {
            System.out.println("Request to get the objects list of the image with id '" + request.getId() + "' received.");
            DocumentReference docRef = db.collection(REQUESTS_COLLECTION).document(request.getId());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                String message = "Request id not found, the image might be processing, please wait a few seconds and" +
                        " make sure you're inserting the right id.";
                System.out.println("\t* WARNING * Request id '" + request.getId() + "' not found.");
                sendNotFoundError(responseObserver, message);
                return;
            }

            // Gets all the objects detected in the image of the request id inserted
            Query query = db.collection(DETECTED_OBJECTS_COLLECTION).whereEqualTo("requestId", request.getId());
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            String imageName = document.getString("imageName");
            String imageType = document.getString("imageType");

            List<DetectedObject> detectedObjectsList = new LinkedList<>();
            documents.forEach(doc -> detectedObjectsList.add(doc.toObject(DetectedObject.class)));

            ImageObjects imageObjects = ImageObjects.newBuilder()
                    .setId(request.getId())
                    .setImageName(imageName + '.' + imageType)
                    .putAllObjectsNames(getDetectedObjectsAppearances(detectedObjectsList))
                    .build();

            responseObserver.onNext(imageObjects);
            responseObserver.onCompleted();
            System.out.println("\t- Response to get the objects list of the image with id '" + request.getId() + "' sent.");
        } catch (InterruptedException | ExecutionException e) {
            sendInternalError(responseObserver, "Internal error, please try again later.");
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void downloadAnnotatedImage(ImageIdentifier request, StreamObserver<ImageUploadDownload> responseObserver) {
        try {
            System.out.println("Request to download annotated image with id '" + request.getId() + "' received.");
            DocumentReference docRef = db.collection(REQUESTS_COLLECTION).document(request.getId());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                String message = "Image id not found, please verify if you're inserting the right id.";
                System.out.println("\t* WARNING * Image id '" + request.getId() + "' not found.");
                sendNotFoundError(responseObserver, message);
                return;
            }

            String annotatedBlob = document.getString("annotatedBlob");
            if (annotatedBlob == null) {
                String message = "Annotated image not found, probably because it was not processed yet." +
                        " Please try again later.";
                System.out.println("\t* WARNING * Annotated image for id '" + request.getId() + "' not found.");
                sendNotFoundError(responseObserver, message);
                return;
            }

            BlobId blobId = BlobId.of(bucket, annotatedBlob);
            Blob blob = storage.get(blobId);

            String annotatedImageName = document.getString("imageName") + "-annotated";
            String imageType = document.getString("imageType");

            // Sends to the client the annotated image metadata
            DownloadAnnotatedImage.sendImageMetaData(annotatedImageName, imageType, blob.getSize(), responseObserver);

            // Stream the annotated image content in blocks of 32KB
            DownloadAnnotatedImage.sendImageContent(blob, responseObserver);
            responseObserver.onCompleted();

            System.out.println("\t- Image '" + annotatedImageName + "' with id '" + request.getId() + "' sent with success.\n");
        } catch (InterruptedException | ExecutionException | IOException e) {
            sendInternalError(responseObserver, e.getMessage());
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void searchForFiles(SearchProperties request, StreamObserver<FilesResponse> responseObserver) {
        System.out.println("Request to search for files received.");
        CollectionReference collection = db.collection(DETECTED_OBJECTS_COLLECTION);

        Timestamp initial = Timestamp.fromProto(request.getInitialTimestamp());
        Timestamp last = Timestamp.fromProto(request.getLastTimestamp());

        Query query = collection
                .whereGreaterThanOrEqualTo("creationTimestamp", initial)
                .whereLessThanOrEqualTo("creationTimestamp", last)
                .whereEqualTo("objectName", request.getObjectName());

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        try {
            CollectionReference requestsCollection = db.collection(REQUESTS_COLLECTION);
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            List<ImageResponse> responses = new LinkedList<>();
            // Can be retrieved duplicated files if they have the same object more than once, so the set is used to prevent that.
            Set<String> filteredFiles = new HashSet<>(); // Key = Request id

            for (QueryDocumentSnapshot doc: documents) {
                String requestId = doc.getString("requestId");
                double score = doc.getDouble("score");
                if (filteredFiles.add(requestId) && score > request.getScore()) {
                    DocumentSnapshot document = requestsCollection.document(requestId).get().get();
                    ImageResponse imgResponse = ImageResponse.newBuilder()
                            .setId(document.getId())
                            .setName(document.getString("imageName") + '.' + document.getString("imageType"))
                            .setObjectsFound(document.getLong("detectedObjects").intValue())
                            .build();
                    responses.add(imgResponse);
                }
            }
            responseObserver.onNext(FilesResponse.newBuilder().addAllResponses(responses).build());
            responseObserver.onCompleted();
            System.out.format("\t- Response with %o files found from the search properties provided.\n", responses.size());
        } catch (InterruptedException | ExecutionException e) {
            sendInternalError(responseObserver, "Internal error, please try again later.");
            System.out.println("* ERROR * " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void getAllFiles(Pagination pagination, StreamObserver<FilesResponse> responseObserver) {
        try {
            System.out.println("Request to get all the files with limit = " + pagination.getLimit() + " and offset = " +
                        pagination.getOffset() + " received.");
            CollectionReference collection = db.collection(REQUESTS_COLLECTION);
            Query query = collection.limit(pagination.getLimit()).offset(pagination.getOffset());
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            List<ImageResponse> responses = new LinkedList<>();
            for (QueryDocumentSnapshot doc : documents) {
                responses.add(ImageResponse.newBuilder()
                        .setId(doc.getId())
                        .setName(doc.getString("imageName") + '.' + doc.getString("imageType"))
                        .setObjectsFound(doc.getLong("detectedObjects").intValue())
                        .build()
                );
            }
            responseObserver.onNext(FilesResponse.newBuilder().addAllResponses(responses).build());
            responseObserver.onCompleted();
            System.out.println("\t- Response with all the files within limit = " + pagination.getLimit() + " and offset = " +
                    pagination.getOffset() + " sent.");
        } catch (InterruptedException | ExecutionException e) {
            sendInternalError(responseObserver, "Internal error, please try again later.");
            System.out.println("* ERROR * " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(ImageIdentifier request, StreamObserver<ImageResponse> responseObserver) {
        try {
            System.out.println("Request to delete the file with id '" + request.getId() + "' received.");
            DocumentReference docRef = db.collection(REQUESTS_COLLECTION).document(request.getId());
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                String message = "Request id not found, please verify if you're inserting the right id.";
                System.out.println("\t* WARNING * Request id '" + request.getId() + "' not found.");
                sendNotFoundError(responseObserver, message);
                return;
            }

            // Get image detected objects to be deleted
            Query query = db.collection(DETECTED_OBJECTS_COLLECTION).whereEqualTo("requestId", request.getId());
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            // Delete Firestore documents
            WriteBatch firestoreBatch = db.batch();
            documents.forEach(doc -> {
                DocumentReference objectsDocRef = db.collection(DETECTED_OBJECTS_COLLECTION).document(doc.getId());
                firestoreBatch.delete(objectsDocRef);
            });
            firestoreBatch.delete(docRef);
            firestoreBatch.commit().get();

            // Delete Cloud Storage blob images
            String originalBlob = document.getString("originalBlob");
            String annotatedBlob = document.getString("annotatedBlob");
            StorageBatch storageBatch = storage.batch();
            CloudStorage.deleteBlob(bucket, storageBatch, originalBlob);
            CloudStorage.deleteBlob(bucket, storageBatch, annotatedBlob);
            storageBatch.submit();

            String imageName = document.getString("imageName");
            responseObserver.onNext(ImageResponse.newBuilder().setId(request.getId()).setName(imageName).build());
            responseObserver.onCompleted();
            System.out.println("\t- File with id '" + request.getId() + "' successfully deleted.");
        } catch (ExecutionException | InterruptedException e) {
            sendInternalError(responseObserver, "Internal error, please try again later.");
            System.out.println("* ERROR * " + e);
            e.printStackTrace();
        }
    }

    private static void initStorage(String[] args) {
        // Get GOOGLE_APPLICATION_CREDENTIALS environment variable
        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        storage = storageOptions.getService();
        projectId = storageOptions.getProjectId();
        if (projectId != null) System.out.println("Current Project ID: " + projectId);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!");
            System.exit(-1);
        }
        // Verifies if it was provided a bucket, otherwise creates a new bucket in case the default one isn't defined
        bucket = CloudStorage.initBucket(args, storage);
    }

    public static void initFirestore() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions.newBuilder().setCredentials(credentials).build();
            db = options.getService();
        } catch (IOException e) {
            System.out.println("Error initializing Firestore: " + e);
            System.exit(-1);
        }
    }

    public static void closeFirestore() {
        try {
            db.close();
        } catch (Exception e) {
            System.out.println("Error closing Firestore: " + e);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length > 2) {
                System.out.println("Usage: java -jar GRPCServer.jar port bucket (bucket is *optional*)");
                System.exit(-1);
            }
            int svcPort = Integer.parseInt(args[0]);

            initFirestore();
            initStorage(args);
            PubSub.initTopic(projectId);

            io.grpc.Server svc = ServerBuilder
                    .forPort(svcPort)
                    .addService(new Server())
                    .build();
            svc.start();
            System.out.println("Server started, listening on port " + svcPort + "...\n");
            svc.awaitTermination();
        } catch (Exception e) {
            closeFirestore();
            System.out.println("* ERROR * " + e);
            e.printStackTrace();
        }
    }
}