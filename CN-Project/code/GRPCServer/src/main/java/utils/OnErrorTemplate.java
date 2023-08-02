package utils;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

public class OnErrorTemplate {
    public static <T> void sendNotFoundError(StreamObserver<T> streamObserver, String message) {
        Throwable th = new StatusException(Status.NOT_FOUND.withDescription(message));
        streamObserver.onError(th);
    }

    public static <T> void sendInternalError(StreamObserver<T> streamObserver, String message) {
        Throwable th = new StatusException(Status.INTERNAL.withDescription(message));
        streamObserver.onError(th);
    }
}
