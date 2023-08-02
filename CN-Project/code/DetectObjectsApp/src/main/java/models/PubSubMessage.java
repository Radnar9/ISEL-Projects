package models;

public class PubSubMessage {
    private final String id;
    private final String imageName;
    private final String imageType;
    private final String bucket;
    private final String blob;

    public PubSubMessage(String id, String imageName, String imageType, String bucket, String blob) {
        this.id = id;
        this.imageName = imageName;
        this.imageType = imageType;
        this.bucket = bucket;
        this.blob = blob;
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

    public String getBlob() {
        return blob;
    }
}
