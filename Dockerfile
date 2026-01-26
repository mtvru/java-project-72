FROM gradle:9.2-jdk21

WORKDIR /app

COPY ./app/config /app/config
COPY ./app/gradle /app/gradle
COPY ./app/src /app/src
COPY ./app/build.gradle.kts /app/build.gradle.kts
COPY ./app/gradlew /app/gradlew
COPY ./app/settings.gradle.kts /app/settings.gradle.kts

RUN ["./gradlew", "clean", "shadowJar"]
ENV JAVA_OPTS="-Xmx512M -Xms512M"
EXPOSE 7070
CMD ["java", "-jar", "build/libs/app-1.0-SNAPSHOT-all.jar"]
