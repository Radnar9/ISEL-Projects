package streamobservers;

import com.google.protobuf.ByteString;
import grpcserver.ImageMetadata;
import grpcserver.ImageUploadDownload;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StreamObserverDownload implements StreamObserver<ImageUploadDownload> {
    private Path path;
    // Image name with type/extension
    private String imageFullName;
    private OutputStream writer;

    public StreamObserverDownload(String absPath) {
        path = Paths.get(absPath);
    }

    @Override
    public void onNext(ImageUploadDownload imageDownload) {
        try {
            if (imageDownload.hasMetadata()) {
                ImageMetadata imageMetaData = imageDownload.getMetadata();
                imageFullName = imageMetaData.getName() + '.' + imageMetaData.getType();
                path = path.resolve(imageFullName);
                writer = Files.newOutputStream(
                        path,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } else {
                writeImage(imageDownload.getContent());
            }
        } catch (IOException e) {
            System.out.println("* ERROR * " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("* Error * " + throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        closeFile();
        System.out.println("\t- Image '" + imageFullName + "' downloaded with success to path '" + path + "'.");
    }

    private void writeImage(ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    private void closeFile(){
        try {
            writer.close();
        } catch (Exception e) {
            System.out.println("* ERROR * " + e.getMessage());
            e.printStackTrace();
        }
    }
}
