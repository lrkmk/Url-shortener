  # Stage 1: build
  FROM maven:3.9-eclipse-temurin-21 AS build
  WORKDIR /app
  COPY pom.xml .
  RUN mvn dependency:go-offline -q
  COPY src ./src
  RUN mvn package -DskipTests -q

  # Stage 2: runtime
  FROM eclipse-temurin:21-jre-jammy
  WORKDIR /app
  COPY --from=build /app/target/shorturl-0.0.1-SNAPSHOT.jar app.jar

  ENV JAVA_OPTS="-Xmx512m -Xms256m"
  EXPOSE 8080

  ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
