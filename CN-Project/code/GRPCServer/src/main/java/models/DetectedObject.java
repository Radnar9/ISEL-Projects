package models;

import com.google.cloud.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectedObject {
    private String objectName;
    private double score;
    private List<Vertex> vertices;
    private String requestId;
    private Timestamp creationTimestamp;

    public DetectedObject() {}

    public DetectedObject(double score, List<Vertex> vertices, String objectName, String requestId, Timestamp creationTimestamp) {
        this.score = score;
        this.vertices = vertices;
        this.objectName = objectName;
        this.requestId = requestId;
        this.creationTimestamp = creationTimestamp;
    }

    public double getScore() {
        return score;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getRequestId() {
        return requestId;
    }

    public Timestamp getCreationTimestamp() {
        return creationTimestamp;
    }

    public static Map<String, Integer> getDetectedObjectsAppearances(List<DetectedObject> detectedObjects) {
        HashMap<String, Integer> objectsNamesMap = new HashMap<>();
        detectedObjects.forEach(obj -> {
            if (objectsNamesMap.containsKey(obj.getObjectName())) {
                int appearances = objectsNamesMap.get(obj.getObjectName());
                objectsNamesMap.put(obj.getObjectName(), appearances + 1);
                return;
            }
            objectsNamesMap.put(obj.getObjectName(), 1);
        });
        return objectsNamesMap;
    }
}
