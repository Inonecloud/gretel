FROM adoptopenjdk/openjdk17:alpine AS build
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew build --no-daemon

RUN ./gradlew build

FROM adoptopenjdk/openjdk17:alpine-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

ENV MONGO_HOST=mongodb \
    MONGO_PORT=27017 \
    MONGO_DB=mydatabase

CMD ["java", "-jar", "app.jar"]