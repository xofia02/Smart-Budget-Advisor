# Etapa 1: compila o projeto com Maven (não precisa ter Java nem Maven na máquina)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: imagem leve só com o Java para rodar o .jar
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
