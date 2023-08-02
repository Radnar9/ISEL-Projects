import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.PubsubMessage;
import models.PubSubMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessageReceiveHandler implements MessageReceiver {

    private final Storage storage;

    public MessageReceiveHandler(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
        String id = pubsubMessage.getData().toStringUtf8();
        System.out.println("Request received with id: " + id);

        Map<String, String> attributesMap = pubsubMessage.getAttributesMap();
        String imageName = attributesMap.get("imageName");
        String imageType = attributesMap.get("imageType");
        String bucket = attributesMap.get("bucket");
        String blob = attributesMap.get("blob");

        PubSubMessage message = new PubSubMessage(id, imageName, imageType, bucket, blob);

        try {
            Vision.detectLocalizedObjectsGcs(storage, message);
            ackReplyConsumer.ack();
        } catch (ExecutionException | InterruptedException | IOException e) {
            ackReplyConsumer.nack();
            System.out.println("Error in detectLocalizedObjectsGcs of Vision class: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
