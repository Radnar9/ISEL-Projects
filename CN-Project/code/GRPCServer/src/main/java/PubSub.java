import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import grpcserver.ImageMetadata;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PubSub {
    private static final String TOPIC = "detectionworkers";

    /**
     *  Verifies if the topic already exists, if it doesn't, the topic is created.
     */
    public static void initTopic(String projectId) throws IOException {
        try (TopicAdminClient topicAdmin = TopicAdminClient.create()) {

            // Verify if the topic already exists
            TopicAdminClient.ListTopicsPagedResponse res = topicAdmin.listTopics(ProjectName.of(projectId));
            for (Topic topic : res.iterateAll()) {
                String[] topicPath = topic.getName().split("/"); // projects/cn2122-t2-g09/topics/detectionworkers
                if (topicPath[topicPath.length - 1].compareTo(TOPIC) == 0) return;
            }

            // If it doesn't exist, creates the topic
            System.out.println("\t- Creating topic '" + TOPIC + "'...");
            TopicName tName = TopicName.ofProjectTopicName("CN2122-T2-G09", TOPIC);
            topicAdmin.createTopic(tName);
            System.out.println("\t- Topic '" + TOPIC + "' successfully created in project '" + projectId + "'.");
        }
    }

    public static void publishMessage(String projectId, String id, String bucket, String blob, ImageMetadata metaData) {
        try {
            TopicName topic = TopicName.ofProjectTopicName(projectId, TOPIC);
            Publisher publisher = Publisher.newBuilder(topic).build();

            ByteString msgData = ByteString.copyFromUtf8(id);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(msgData)
                    .putAttributes("bucket", bucket)
                    .putAttributes("blob", blob)
                    .putAttributes("imageName", metaData.getName())
                    .putAttributes("imageType", metaData.getType())
                    .build();
            ApiFuture<String> future = publisher.publish(pubsubMessage);
            String msgID = future.get();
            publisher.shutdown();
            System.out.println("\t- Message published in topic '" + TOPIC + "' with id = " + msgID + '.');
        } catch (IOException | ExecutionException | InterruptedException e) {
            System.out.println("* ERROR * " + e);
            e.printStackTrace();
        }
    }
}
