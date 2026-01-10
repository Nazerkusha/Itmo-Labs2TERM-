
FROM gradle:8-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

FROM quay.io/wildfly/wildfly:27.0.0.Final-jdk17
COPY --from=build /app/build/libs/geometry-service.war /opt/jboss/wildfly/standalone/deployments/
EXPOSE 8080
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
