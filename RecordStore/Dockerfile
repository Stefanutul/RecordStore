FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
