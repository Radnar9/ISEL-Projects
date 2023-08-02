import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import grpcserver.ImageMetadata;
import grpcserver.ImageResponse;
import grpcserver.ImageUploadDownload;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerStreamObserverUpload implements StreamObserver<ImageUploadDownload> {
    private static final long MAX_FILE_SIZE = (long) Math.pow(1024, 4) * 5; // 5TiB -> Max size for a file in Cloud Storage
    private static final String BUCKET_NAME = "cn2122tf";
    private final String projectId;

    private static final Set<String> possibleImageTypes = new HashSet<>(){{
        add("png"); add("jpg"); add("jpeg"); add("gif"); add("bmp");
    }};

    private final StreamObserver<ImageResponse> replyStream;
    private final Storage storage;
    private static ImageMetadata imageMetadata;
    private static String requestId;
    private static String blobName;
    private static WriteChannel writer;

    public ServerStreamObserverUpload(StreamObserver<ImageResponse> replyStream, Storage storage, String projectId) {
        this.replyStream = replyStream;
        this.storage = storage;
        this.projectId = projectId;
    }

    @Override
    public void onNext(ImageUploadDownload imageUpload) {
        try {
            if(imageUpload.hasMetadata()) {
                imageMetadata = imageUpload.getMetadata();
                if (!isImageTypeSupported(imageMetadata.getType()) && !isImageSizeValid(imageMetadata.getSize())) return;
                requestId = UUID.randomUUID().toString();
                blobName = requestId + '/' + imageMetadata.getName();
                writer = createWriteChannel();
            } else {
                writeOnCloudStorage(imageUpload.getContent());
            }
        } catch (IOException e) {
            String message = "An internal error occurred, please try again later.";
            replyStream.onError(new StatusException(Status.INTERNAL.withDescription(message)));
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("* Error * " + throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        closeWriteChannel();
        ImageResponse response = ImageResponse.newBuilder()
                .setId(requestId)
                .setName(imageMetadata.getName())
                .build();
        System.out.println("\t- Image '" +  imageMetadata.getName() + "' stored with success.");
        replyStream.onNext(response);
        replyStream.onCompleted();
        PubSub.publishMessage(projectId, requestId, BUCKET_NAME, blobName, imageMetadata);
    }

    private boolean isImageSizeValid(Long imageSize) {
        if (imageSize > 0 && imageSize < MAX_FILE_SIZE) return true;
        String message = "The image size '" + imageSize + "' is not supported (0 > size < 5TB).";
        Throwable th = new StatusException(Status.INVALID_ARGUMENT.withDescription(message));
        replyStream.onError(th);
        System.out.println("* ERROR * " + message);
        return false;
    }

    private boolean isImageTypeSupported(String imageType) {
        if (possibleImageTypes.contains(imageType)) return true;
        String message = "The image type '." + imageType + "' is not supported.";
        Throwable th = new StatusException(Status.INVALID_ARGUMENT.withDescription(message));
        replyStream.onError(th);
        System.out.println("* ERROR * " + message);
        return false;
    }

    private WriteChannel createWriteChannel() {
        String contentType = "image/" + imageMetadata.getType();
        BlobId blobId = BlobId.of(BUCKET_NAME, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        return storage.writer(blobInfo);
    }

    private void writeOnCloudStorage(ByteString content) throws IOException {
        byte[] imageContent = content.toByteArray();
        int size = imageContent.length;
        writer.write(ByteBuffer.wrap(imageContent, 0, size));
    }

    private void closeWriteChannel() {
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("* Error * " + e.getMessage());
            e.printStackTrace();
        }
    }
}
