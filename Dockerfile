FROM gradle:jdk17-alpine as builder
WORKDIR /build
COPY src ./src
COPY build.gradle ./
RUN gradle shadowJar

FROM eclipse-temurin:17-jre-focal
WORKDIR /server
COPY --from=builder /build/build/libs/build-1.0-SNAPSHOT-all.jar server.jar

EXPOSE 25565

CMD ["/opt/java/openjdk/bin/java", "-jar", "server.jar"]
