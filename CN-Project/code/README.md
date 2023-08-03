# Project Configurations

##### In order to test the developed project, it is recommended to respect the order of components initialization, following the sequence described below.
##### If you are using the Google Cloud Platform, you should enable the functionalities related to Cloud Function and Vision API. Additionally, you must install the [gRPC contract](GRPCContract/) through Maven's `install` functionality to ensure that the gRPC client and server work correctly and can communicate with each other.
##### Finally, to execute the commands to create the cloud functions, you need to install the [`Google Cloud CLI`](https://cloud.google.com/sdk/docs/install).

## Lookup Function

To deploy the cloud function on GCP, simply fill in the command below with the service account email that has the necessary permissions:

```bash
    gcloud functions deploy lookup-function --allow-unauthenticated --entry-point=Entrypoint --runtime=java11 --trigger-http --region=europe-west1 --source=target/deployment --service-account=<todo-service-account-email>
```

#### Service Account - Permissions (roles):
- `Compute Admin`: To access the Compute Engine service

#### Configurations:
- Put the corresponding **project ID** in the `PROJECT` constant of the `Entrypoint class`;

#### Usage:
- In the client application, the `instance group's name and its zone` must be provided so that the Lookup Function can retrieve the corresponding VMs.

## Monitor Function

Similar to the previous cloud function, just complete the command with the service account email containing the necessary permissions and execute it to create the cloud function:

```bash
    gcloud functions deploy monitor-function --entry-point=Entrypoint --runtime=java11 --trigger-topic detectionworkers --region=europe-west1 --source=target/deployment --service-account=<todo-service-account-email>
```

#### Service Account - Permissions (roles):
- `Compute Admin`: To access the Compute Engine service
- `Cloud Datastore Owner`: To access the Firestore service

#### Configurations:
- Put the **project ID** in the `PROJECT` constant of the `Entrypoint class`;
- Create an **instance group** with the name `'detect-objects-app-instance-group'` in the zone `'europe-west1-b'`, or change the values of the constants `INSTANCE_GROUP_NAME` and `INSTANCE_GROUP_ZONE` in the `Entrypoint class` with desired values.

#### Usage:
- If the `Monitor` collection or its `properties` document is deleted from Firestore or does not exist, the function will **create the collection and the document when they do not exist**;
- Once the function is deployed and the configurations are correct, no further operation is required; the function should be working correctly.

## GRPC Server

#### Service Account - Permissions (roles):
- `Cloud Datastore Owner`: To access the Firestore service
- `Storage Admin`: To access the Cloud Storage service
- `Pub/Sub Admin`: To access the Cloud Pub/Sub service

#### Configurations:
- Set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` with the path to the **.json file** corresponding to the **service account with the mentioned roles**.

#### Usage:

- When starting the server, the `port` to which it should be associated must be provided as an argument;
- Although not mandatory, you can pass the `bucket name` as the second argument, **which should be pre-initialized**;
- It is not necessary to create the subscription used by the application beforehand; if it does not exist during the application's initialization, specifically during the creation of the subscriber, it will be created.

## Detect Objects App

#### Service Account - Permissions (roles):
- `Cloud Datastore Owner`: To access the Firestore service
- `Storage Admin`: To access the Cloud Storage service
- `Pub/Sub Admin`: To access the Cloud Pub/Sub service

#### Configurations:
- Set the environment variable `GOOGLE_APPLICATION_CREDENTIALS` with the path to the **.json file** corresponding to the **service account with the mentioned roles**.

#### Usage:
- It is not necessary to create the subscription used by the application beforehand; if it does not exist during the application's initialization, specifically during the creation of the subscriber, it will be created.

## Client
* For the `Client` application, no service account or additional configuration is required. When the application is started, you will be asked to enter the `name and zone of the instance group` where the gRPC server instances are running, unless you choose to use localhost.

---
If you intend to place the `gRPC server` and the `Detect Objects App` in VMs running on the cloud, you can use the scripts provided within this code directory to start the applications and set the environment variable with the corresponding service account during **VM startup**. The scripts to be used are: `grpc-server-startup.sh` and `detect-objects-app-startup.sh`, respectively. 

When using the scripts, please note that the servers and service accounts must be in the `/var/server` directory, created using the command `'sudo mkdir /var/server'` executed from the `home` directory.

###### June 9, 2022