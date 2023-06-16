FROM eclipse-temurin:17

WORKDIR /app

COPY target/bookmarks-0.0.1-SNAPSHOT.jar /app/bookmarks.jar
COPY application.properties /app/application.properties

ENTRYPOINT ["java", "-jar", "bookmarks.jar"]