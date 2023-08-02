import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.util.LinkedList;
import java.util.List;

public class Entrypoint implements HttpFunction {
    private static final String PROJECT = "cn2122-t2-g09";
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        BufferedWriter writer = response.getWriter();
        String zone = request.getFirstQueryParameter("zone").orElse(null);
        String instanceGroup = request.getFirstQueryParameter("instance-group").orElse(null);
        if (zone == null || instanceGroup == null) {
            String message = "Query parameter " + (zone == null ? "'zone'" : "'instance-group'") + " is missing!";
            writer.write(new Gson().toJson(new RunningInstances(null, message)));
            return;
        }

        try (InstancesClient client = InstancesClient.create()) {
            List<RunningInstances.Instance> instances = new LinkedList<>();
            for (Instance instance : client.list(PROJECT, zone).iterateAll()) {
                if (instance.getName().contains(instanceGroup) && instance.getStatus().compareTo("RUNNING") == 0) {
                    String ip = instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    instances.add(new RunningInstances.Instance(instance.getName(), ip));
                }
            }
            writer.write(new Gson().toJson(new RunningInstances(instances, null)));
        }
    }
}
