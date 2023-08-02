import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.*;

import java.io.IOException;

public class PubSub {
    private static final String TOPIC = "detectionworkers";
    private static final String WORKERS_SUBSCRIPTION = "workers";

    /**
     *  Verifies if the subscripion already exists in the topic, if it doesn't, the subscription is created.
     *  The correspondent topic must exist already.
     */
    private static void initSubscription(String projectId) throws IOException {
        try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {

            // Verify if the subscription already exists
            SubscriptionAdminClient.ListSubscriptionsPagedResponse res =
                    subscriptionAdminClient.listSubscriptions(ProjectName.of(projectId));
            for (Subscription sub : res.iterateAll()) {
                String[] subPath = sub.getName().split("/"); // projects/cn2122-t2-g09/subscriptions/workers
                if (subPath[subPath.length - 1].compareTo(WORKERS_SUBSCRIPTION) == 0) return;
            }

            // If it doesn't exist, creates the subscription
            System.out.println("\t- Creating subscription '" + WORKERS_SUBSCRIPTION + "'...");
            PushConfig pconfig = PushConfig.getDefaultInstance();
            TopicName tName = TopicName.ofProjectTopicName(projectId, TOPIC);
            SubscriptionName subscriptionName = SubscriptionName.of(projectId, WORKERS_SUBSCRIPTION);
            subscriptionAdminClient.createSubscription(subscriptionName, tName, pconfig, 30);

            System.out.println("\t- Subscription '" + WORKERS_SUBSCRIPTION + "' successfully created in topic '" +
                    TOPIC + "' of the project '" + projectId + "'.");
        }
    }

    public static Subscriber subscribeMessages(String projectId, Storage storage) throws IOException {
        initSubscription(projectId);
        ProjectSubscriptionName projSubscriptionName = ProjectSubscriptionName.of(projectId, WORKERS_SUBSCRIPTION);

        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1)
                .build();

        Subscriber subscriber = Subscriber
                .newBuilder(projSubscriptionName, new MessageReceiveHandler(storage))
                .setExecutorProvider(executorProvider)
                .build();

        subscriber.startAsync().awaitRunning();
        return subscriber;
    }
}
