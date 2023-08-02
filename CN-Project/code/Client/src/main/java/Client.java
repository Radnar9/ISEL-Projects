import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import grpcserver.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import models.Instances;
import streamobservers.StreamObserverDownload;
import streamobservers.StreamObserverUpload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Client {
    private static final int _32K = 1024 * 32;
    private static final int LIMIT = 10;
    private static final String LOOKUP_IPS_URL = "https://europe-west1-cn2122-t2-g09.cloudfunctions.net/lookup-function?";
    private static final String DEFAULT_SERVER_PORT = "8000";
    private static final String DEFAULT_SERVER_INSTANCE_GROUP_NAME = "grpc-server-instance-group";
    private static final String DEFAULT_SERVER_INSTANCE_GROUP_ZONE = "europe-west1-b";
    private static ServerGrpc.ServerBlockingStub blockingStub;
    private static ServerGrpc.ServerStub noBlockStub;

    static int menu() {
        Scanner scanner = new Scanner(System.in);
        int option;
        do {
            System.out.println();
            System.out.println("########## MENU ##########");
            System.out.println("Available operations:");
            System.out.println(" 0: Upload an image to detect objects");
            System.out.println(" 1: Get the list of detected objects names of an image");
            System.out.println(" 2: Download an annotated image");
            System.out.println(" 3: Get files names between two dates, with a specific object and score");
            System.out.println(" 4: Get all the files");
            System.out.println(" 5: Delete a file");
            System.out.println("99: Exit");
            System.out.print("Enter an option: \n");
            option = scanner.nextInt();
            System.out.println();
        } while (!((option >= 0 && option <= 5) || option == 99));
        return option;
    }

    private static String processInput(String input, String defaultValue) {
        if (input.compareTo("") == 0) return defaultValue;
        return input;
    }

    private static String readInput(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }

    private static void uploadImage(String imagePath) {
        Path path = Paths.get(imagePath);
        String fileNameWithType = path.getFileName().toString();
        int extensionIdx = fileNameWithType.lastIndexOf('.');
        String fileName = fileNameWithType.substring(0, extensionIdx);
        String fileType = fileNameWithType.substring((extensionIdx) + 1);

        StreamObserverUpload replyStream = new StreamObserverUpload();
        StreamObserver<ImageUploadDownload> reqStream = noBlockStub.uploadImage(replyStream);

        // Build and upload image metadata
        ImageMetadata metadata;
        try {
            metadata = ImageMetadata.newBuilder()
                    .setName(fileName)
                    .setType(fileType)
                    .setSize(Files.size(path))
                    .build();
        } catch (IOException e) {
            System.out.println("* ERROR * " + e.getMessage());
            throw new RuntimeException(e);
        }

        ImageUploadDownload uploadMetaData = ImageUploadDownload.newBuilder().setMetadata(metadata).build();
        reqStream.onNext(uploadMetaData);

        // Upload image content in blocks of 32KB
        System.out.println("-> Uploading image...");
        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] bytes = new byte[_32K];
            int size;
            int cnt = 0;
            while ((size = inputStream.read(bytes)) > 0){
                ImageUploadDownload uploadImage = ImageUploadDownload.newBuilder()
                        .setContent(ByteString.copyFrom(bytes, 0 , size))
                        .build();
                reqStream.onNext(uploadImage);
            }
            reqStream.onCompleted();
        } catch (IOException e) {
            System.out.println("* ERROR * " + e.getMessage());
            Throwable th = new StatusException(Status.INTERNAL.withCause(e));
            reqStream.onError(th);
        }
    }

    private static void getImageDetectedObjects(String imageId) {
        try {
            ImageIdentifier imageIdentifier = ImageIdentifier.newBuilder().setId(imageId).build();
            ImageObjects reply = blockingStub.getImageDetectedObjects(imageIdentifier);

            System.out.println("Objects found in the image '" + reply.getImageName() + "' with id '" + reply.getId() + "':");
            Map<String, Integer> objectsNamesMap = reply.getObjectsNamesMap();
            objectsNamesMap.forEach((key, value) -> System.out.println("\t- x" + value + " " + key));
        } catch (StatusRuntimeException e) {
            System.out.println("* ERROR * " + e.getMessage());
        }
    }

    private static void downloadAnnotatedImage(String imageId, String absPath) {
        try {
            ImageIdentifier imageIdentifier = ImageIdentifier.newBuilder().setId(imageId).build();
            System.out.println("Downloading image...");
            noBlockStub.downloadAnnotatedImage(imageIdentifier, new StreamObserverDownload(absPath));
        } catch (StatusRuntimeException e) {
            System.out.println("* ERROR * " + e.getMessage());
        }
    }

    private static Timestamp localDateTimeToProtoTimestamp(long seconds, int nanos) {
        return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
    }

    private static void searchForFiles(String initialDateStr, String lastDateStr, String objectName, String strScore) {
        try {
            if (objectName.length() == 0) {
                System.out.println("\n* ERROR * Please insert a valid object name.");
                return;
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDateTime initialTime = LocalDate.parse(initialDateStr, dateFormatter).atTime(0, 0, 0, 0);
            LocalDateTime lastTime = LocalDate.parse(lastDateStr, dateFormatter).atTime(23, 59, 59, 999);

            java.sql.Timestamp tm = java.sql.Timestamp.valueOf(initialTime);
            java.sql.Timestamp tm2 = java.sql.Timestamp.valueOf(lastTime);

            Timestamp initialTimestamp = localDateTimeToProtoTimestamp(tm.getTime() / 1000, tm.getNanos());
            Timestamp lastTimestamp = localDateTimeToProtoTimestamp(tm2.getTime() / 1000, tm.getNanos());
            double score = Double.parseDouble(strScore);

            SearchProperties props = SearchProperties.newBuilder()
                    .setInitialTimestamp(initialTimestamp)
                    .setLastTimestamp(lastTimestamp)
                    .setObjectName(objectName)
                    .setScore(score)
                    .build();
            FilesResponse response = blockingStub.searchForFiles(props);

            List<ImageResponse> filesList = response.getResponsesList();
            if (filesList.isEmpty()) {
                System.out.println("- No file was found with the inserted characteristics.");
                return;
            }

            System.out.format("<****| Files found between %s and %s with the object '%s' with a score of at least %s |****>\n",
                    initialDateStr, lastDateStr, objectName, strScore);
            filesList.forEach(image -> System.out.format("\t- Id: %s, Image name: %s, Objects found: %o\n",
                    image.getId(), image.getName(), image.getObjectsFound()));
        } catch (DateTimeParseException | NullPointerException | NumberFormatException e) {
            String inputError;
            if (e instanceof DateTimeParseException) {
                inputError = "dates";
            } else {
                inputError = "score";
            }
            System.out.format("Error parsing %s, please verify if you're inserting the %s in the right format.",
                    inputError, inputError);
        }
    }

    private static void getAllFiles(Scanner scanner) {
        int offset = 0;

        while(true) {
            Pagination pagination = Pagination.newBuilder().setLimit(LIMIT + 1).setOffset(offset).build();
            FilesResponse response = blockingStub.getAllFiles(pagination);

            if (response.getResponsesList().isEmpty()) {
                System.out.println("\n* There are no files stored *");
                return;
            }

            int imageCnt = 0;
            System.out.println("<****| Page " + (offset / LIMIT + 1) + " files |****>");
            for (ImageResponse image: response.getResponsesList()) {
                if (++imageCnt > LIMIT) continue;
                System.out.format(
                        "\t- Id: %s | Image name: %s | Objects found: %o\n",
                        image.getId(), image.getName(), image.getObjectsFound()
                );
            }

            if (response.getResponsesList().size() <= LIMIT) {
                System.out.println("\n* There are no more pages of files to fetch *");
                return;
            }

            String input = processInput(
                    readInput("\nFetch next page? (Press ENTER to fetch or press n to exit)", scanner),
                    "");
            if (input.compareTo("n") == 0) {
                return;
            }
            offset += LIMIT;
        }
    }

    private static void deleteFile(String imageId) {
        try {
            ImageIdentifier imageIdentifier = ImageIdentifier.newBuilder().setId(imageId).build();
            ImageResponse resp = blockingStub.deleteFile(imageIdentifier);
            System.out.println("\t- File with id '" + resp.getId() + "' and image name '" + resp.getName() + "' successfully deleted.");
        } catch (StatusRuntimeException e) {
            System.out.println("* ERROR * " + e.getMessage());
        }
    }

    private static Optional<String> listInstanceGroupVMsIp(String instanceGroup, String zone, Scanner scanner) {
        HttpClient client = HttpClient.newBuilder().build();
        URI uri = URI.create(LOOKUP_IPS_URL + "zone=" + zone + '&' + "instance-group=" + instanceGroup);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                Instances instances = new Gson().fromJson(response.body(), Instances.class);
                if (instances.getError() != null) {
                    System.out.println("\t* ERROR * " + instances.getError());
                    return Optional.of("Error");
                }

                if (instances.getInstances().isEmpty()) {
                    System.out.println("\t- No available VMs were found in the instance group '" + instanceGroup + "'.");
                    return Optional.empty();
                }

                int idx = 0;
                System.out.println("Available VMs in the instance group '" + instanceGroup + "':");
                for (Instances.Instance instance: instances.getInstances()) {
                    System.out.format("\t%o - Name: %s | IP: %s\n", idx++, instance.getName(), instance.getIp());
                }
                System.out.println();
                int option;
                do {
                    System.out.print("Number of the desired VM? ");
                    option = scanner.nextInt();
                } while (option < 0 || option > instances.getInstances().size());
                System.out.println();
                return Optional.of(instances.getInstances().get(option).getIp());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("* ERROR * " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InputMismatchException e) {
            System.out.println("* ERROR * Please insert a number associated to a VM.");
            return Optional.of("InputMismatch");
        }
        System.out.format("\t* ERROR * Instance group '%s' in zone '%s' not found.\n", instanceGroup, zone);
        return Optional.of("Error");
    }

    private static Optional<String> getServerIp() {
        Scanner scanner = new Scanner(System.in);
        Optional<String> result = Optional.of(
                processInput(readInput("Connect to localhost? (Press ENTER to confirm otherwise press n)", scanner),
                "localhost")
        );
        if (result.get().compareTo("n") == 0) {
            String instanceGroup = processInput(
                    readInput("Instance group name? (Press ENTER to 'grpc-server-instance-group')", scanner),
                    DEFAULT_SERVER_INSTANCE_GROUP_NAME);
            String zone = processInput(
                    readInput("Instance group zone? (Press ENTER to 'europe-west1-b')", scanner),
                    DEFAULT_SERVER_INSTANCE_GROUP_ZONE);
            result = listInstanceGroupVMsIp(instanceGroup, zone, scanner);
        }
        return result;
    }

    public static void main(String[] args) {
        Optional<String> svcIP = getServerIp();
        if (svcIP.isEmpty()) {
            System.out.println("\t- Please try again later or use localhost instead.");
            return;
        } else if (svcIP.get().compareTo("InputMismatch") == 0 || svcIP.get().compareTo("Error") == 0) return;

        Scanner scanner = new Scanner(System.in);
        int svcPort = Integer.parseInt(processInput(
                readInput("Server port? (Press ENTER to 8000)", scanner),
                DEFAULT_SERVER_PORT)
        );

        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP.get(), svcPort)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        blockingStub = ServerGrpc.newBlockingStub(channel);
        noBlockStub = ServerGrpc.newStub(channel);

        while (true) {
            try {
                int option = menu();
                switch (option) {
                    case 0:
                        uploadImage(readInput("Absolute path of the image to upload?", scanner));
                        break;
                    case 1:
                        getImageDetectedObjects(readInput("Request id?", scanner));
                        break;
                    case 2:
                        String imageId = readInput("Request id?", scanner);
                        String absPath = readInput("Absolute path to download the annotated image?", scanner);
                        downloadAnnotatedImage(imageId, absPath);
                        break;
                    case 3:
                        String initialDate = readInput("Initial date? [dd/mm/yyyy]", scanner);
                        String lastDate = readInput("Last date? [dd/mm/yyyy]", scanner);
                        String objectName = readInput("Object name?", scanner);
                        String score = readInput("Score above of? (1 <= t >= 0, e.g. 0.69)", scanner);
                        searchForFiles(initialDate, lastDate, objectName, score);
                        break;
                    case 4:
                        getAllFiles(scanner);
                        break;
                    case 5:
                        deleteFile(readInput("Request id?", scanner));
                        break;
                    case 99:
                        System.exit(0);
                }
            } catch (Exception e) {
                System.out.println("Error executing operations! " + e);
            }
        }
    }
}
