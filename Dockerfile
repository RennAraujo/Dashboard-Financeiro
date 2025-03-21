FROM maven:3.8.5-openjdk-17-slim AS build

WORKDIR /app

# Copiar arquivos de configuração do Maven primeiro para aproveitar o cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código-fonte e compilar
COPY src ./src
RUN mvn package -DskipTests -Dmaven.test.skip=true

# Imagem final
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar o JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente para configuração da aplicação
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/dashboard_financeiro
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV SERVER_PORT=8080

# Porta exposta
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
