import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.protobuf.ByteString;
import grpcserver.ImageMetadata;
import grpcserver.ImageUploadDownload;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DownloadAnnotatedImage {

    private static final int _32K = 32 * 1024;

    public static void sendImageMetaData(
            String name,
            String type,
            long size,
            StreamObserver<ImageUploadDownload> responseObserver
    ) {
        ImageMetadata imageMetaData = ImageMetadata.newBuilder()
                .setName(name)
                .setType(type)
                .setSize(size)
                .build();
        ImageUploadDownload imageDownload = ImageUploadDownload.newBuilder().setMetadata(imageMetaData).build();
        responseObserver.onNext(imageDownload);
    }

    public static void sendImageContent(Blob blob, StreamObserver<ImageUploadDownload> responseObserver) throws IOException {
        try (ReadChannel reader = blob.reader()) {
            ByteBuffer bytes = ByteBuffer.allocate(_32K);
            int size;
            while ((size = reader.read(bytes)) > 0) {
                bytes.flip();
                ImageUploadDownload downloadImage = ImageUploadDownload.newBuilder()
                        .setContent(ByteString.copyFrom(bytes.array(), 0 , size))
                        .build();
                responseObserver.onNext(downloadImage);
                bytes.clear();
            }
        }
    }
}
