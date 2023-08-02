import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import models.DetectedObject;
import models.PubSubMessage;
import models.Vertex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Vision {

    public static void detectLocalizedObjectsGcs(
        Storage storage,
        PubSubMessage message
    ) throws IOException, ExecutionException, InterruptedException {
        String bucket = message.getBucket();
        String blob = message.getBlob();

        String gcsPath = "gs://" + bucket + "/" + blob;
        ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(Feature.newBuilder().setType(Feature.Type.OBJECT_LOCALIZATION))
                        .setImage(img)
                        .build();

        BatchAnnotateImagesRequest singleBatchRequest = BatchAnnotateImagesRequest.newBuilder()
                .addRequests(request)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            // Perform the request
            BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(singleBatchRequest);
            List<AnnotateImageResponse> listResponses = batchResponse.getResponsesList();

            if (listResponses.isEmpty()) {
                System.out.println("Empty response, no objects detected.");
                return;
            }
            // Get the only response
            AnnotateImageResponse response = listResponses.get(0);

            // Get the information detected in the image
            LinkedList<DetectedObject> detectedObjects = new LinkedList<>();
            for (LocalizedObjectAnnotation annotation : response.getLocalizedObjectAnnotationsList()) {
                LinkedList<models.Vertex> objectVertices = new LinkedList<>();
                annotation.getBoundingPoly()
                        .getNormalizedVerticesList()
                        .forEach(vertex -> objectVertices.add(new Vertex(vertex.getX(), vertex.getY())));
                detectedObjects.add(new DetectedObject(annotation.getName(), annotation.getScore(), objectVertices));
            }

            // Annotate in memory Blob image
            BlobId blobId = BlobId.of(bucket, blob);
            BufferedImage bufferImg = getBlobBufferedImage(storage, blobId);
            annotateWithObjects(bufferImg, response.getLocalizedObjectAnnotationsList());

            // Save the image to a new blob in the same bucket. The name of new blob has the annotated prefix
            String destinationBlobName = blob + "-annotated";
            writeAnnotatedImage(storage, bufferImg, blobId, bucket, destinationBlobName);
            System.out.println("\t- Annotated image with id '" + message.getId() + "' successfully stored.");

            // Save objects found in Firestore
            FirestoreOperations.setDetectedObjectsInfo(message, destinationBlobName, detectedObjects);
            System.out.println("\t- Request and detected objects information successfully saved.");
        }
    }

    private static void writeAnnotatedImage(
            Storage storage,
            BufferedImage bufferImg,
            BlobId blobId,
            String bucketName,
            String destinationBlobName
    ) throws IOException {
        String imageContentType = storage.get(blobId).getContentType(); // image/jgp
        String imageType = imageContentType.substring(imageContentType.indexOf('/') + 1); // jpg

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, destinationBlobName))
                .setContentType(imageContentType)
                .build();
        Blob destBlob = storage.create(blobInfo);
        WriteChannel writeChannel = storage.writer(destBlob);
        OutputStream out = Channels.newOutputStream(writeChannel);
        ImageIO.write(bufferImg, imageType, out);
        out.close();
    }

    private static BufferedImage getBlobBufferedImage(Storage storage, BlobId blobId) throws IOException {
        Blob blob = storage.get(blobId);
        if (blob == null) {
            System.out.println("No such Blob exists!");
            throw new IOException("Blob '" + blobId.getName() + "' not found in bucket '" + blobId.getBucket() + "'.");
        }
        ReadChannel reader = blob.reader();
        InputStream in = Channels.newInputStream(reader);
        return ImageIO.read(in);
    }

    private static void annotateWithObjects(BufferedImage img, List<LocalizedObjectAnnotation> objects) {
        for (LocalizedObjectAnnotation obj : objects) {
            annotateWithObject(img, obj);
        }
    }

    private static void annotateWithObject(BufferedImage img, LocalizedObjectAnnotation obj) {
        Graphics2D gfx = img.createGraphics();
        gfx.setFont(new Font("Arial", Font.PLAIN, 18));
        gfx.setStroke(new BasicStroke(3));
        gfx.setColor(new Color(0x00ff00));
        Polygon poly = new Polygon();
        BoundingPoly imgPoly = obj.getBoundingPoly();

        // Draw object name
        gfx.drawString(obj.getName(),
                imgPoly.getNormalizedVertices(0).getX() * img.getWidth(),
                imgPoly.getNormalizedVertices(0).getY() * img.getHeight() - 3);

        // Draw bounding box of object
        for (NormalizedVertex vertex : obj.getBoundingPoly().getNormalizedVerticesList()) {
            poly.addPoint((int) (img.getWidth() * vertex.getX()), (int) (img.getHeight() * vertex.getY()));
        }
        gfx.draw(poly);
    }
}
