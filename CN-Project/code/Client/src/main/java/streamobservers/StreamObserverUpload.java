package streamobservers;

import grpcserver.ImageResponse;
import io.grpc.stub.StreamObserver;

public class StreamObserverUpload implements StreamObserver<ImageResponse> {
    private ImageResponse imageResponse;
    @Override
    public void onNext(ImageResponse imageResponse) {
        this.imageResponse = imageResponse;
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("* Error * " + throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("\t- Image '" + imageResponse.getName() +
                "' was uploaded with the id '" + imageResponse.getId() + "'.");
    }
}
