## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:jdk-21 AS build
COPY --chown=quarkus:quarkus --chmod=0755 mvnw /code/mvnw
COPY --chown=quarkus:quarkus .mvn /code/.mvn
COPY --chown=quarkus:quarkus pom.xml /code/
USER quarkus
WORKDIR /code
RUN ./mvnw -B org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline
COPY src /code/src
RUN ./mvnw package -Dnative -Dmaven.test.skip

## Stage 2 : create the docker final image
# FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.5
WORKDIR /work/
COPY --from=build /code/target/*-runner /work/application

# Check if curl is already available, install if not
RUN if ! command -v curl &> /dev/null; then \
      microdnf install -y curl-minimal || microdnf install -y curl; \
    fi && \
    microdnf clean all && \
    rm -rf /var/cache/yum

# set up permissions for user `1001`
RUN chmod 775 /work /work/application \
  && chown -R 1001 /work \
  && chmod -R "g+rwX" /work \
  && chown -R 1001:root /work

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]