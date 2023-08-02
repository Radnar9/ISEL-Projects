#! /bin/bash
export GOOGLE_APPLICATION_CREDENTIALS=/var/server/detect-objects-app-service-acc.json
java -jar /var/server/DetectObjectsApp-1.0-jar-with-dependencies.jar > /tmp/log.txt