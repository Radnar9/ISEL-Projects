package models;

import java.util.List;

public class DetectedObject {
    private final String name;
    private final double score;
    private final List<Vertex> objectVertices;

    public DetectedObject(String name, double score, List<Vertex> objectVertices) {
        this.name = name;
        this.score = score;
        this.objectVertices = objectVertices;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public List<Vertex> getObjectVertices() {
        return objectVertices;
    }
}
