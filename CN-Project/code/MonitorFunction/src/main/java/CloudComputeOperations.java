import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class CloudComputeOperations {
    private static final Logger logger = Logger.getLogger(CloudComputeOperations.class.getName());

    public static void resizeInstaceGroup(String project, String zone, String instanceGroup, int instances) {
        try {
            InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
            OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                    project,
                    zone,
                    instanceGroup,
                    instances
            );
            Operation oper = result.get();
            logger.info("\t- Instance group resized with status " + oper.getStatus());
        } catch (InterruptedException | ExecutionException | IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
