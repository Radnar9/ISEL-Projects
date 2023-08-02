import java.util.List;

public class RunningInstances {
    public static class Instance {
        private final String name;
        private final String ip;

        public Instance(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
    }

    private final List<Instance> instances;
    private final String error;

    public RunningInstances(List<Instance> instances, String error) {
        this.instances = instances;
        this.error = error;
    }
}
