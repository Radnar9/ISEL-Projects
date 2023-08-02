#! /bin/bash
export GOOGLE_APPLICATION_CREDENTIALS=/var/server/grpc-server-service-acc.json
java -jar /var/server/GRPCServer-1.0-jar-with-dependencies.jar 8000 > /tmp/log.txt