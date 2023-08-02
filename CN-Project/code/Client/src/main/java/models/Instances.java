package models;

import java.util.List;

public class Instances {
    public static class Instance {
        private final String name;
        private final String ip;

        public Instance(String name, String ip) {
            this.name = name;
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public String getIp() {
            return ip;
        }
    }

    private final List<Instance> instances;
    private final String error;

    public Instances(List<Instance> instances, String error) {
        this.instances = instances;
        this.error = error;
    }

    public List<Instance> getInstances() {
        return instances;
    }
    public String getError() {
        return error;
    }
}
