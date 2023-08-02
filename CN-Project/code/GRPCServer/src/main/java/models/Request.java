package models;

import com.google.cloud.Timestamp;

import java.util.List;

public class Request {

    private String id;
    private String imageName;
    private String imageType;
    private String bucket;
    private String originalBlob;
    private String annotatedBlob;
    private Timestamp annotationTimestamp;
    private List<DetectedObject> detectedObjects;

    public Request() {}

    public Request(
            String id,
            String imageName,
            String imageType,
            String bucket,
            String originalBlob,
            String annotatedBlob,
            Timestamp annotationTimestamp,
            List<DetectedObject> detectedObjects
    ) {
        this.id = id;
        this.imageName = imageName;
        this.imageType = imageType;
        this.bucket = bucket;
        this.originalBlob = originalBlob;
        this.annotatedBlob = annotatedBlob;
        this.annotationTimestamp = annotationTimestamp;
        this.detectedObjects = detectedObjects;
    }

    public String getId() {
        return id;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public String getBucket() {
        return bucket;
    }

    public String getOriginalBlob() {
        return originalBlob;
    }

    public String getAnnotatedBlob() {
        return annotatedBlob;
    }

    public Timestamp getAnnotationTimestamp() {
        return annotationTimestamp;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }
}
