# Imagen base con JDK y Maven
FROM eclipse-temurin:17-jdk

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY . .

# Instalar dependencias y ejecutar la aplicación
RUN ./mvnw clean install -DskipTests  # Usa Maven Wrapper para evitar instalación manual

# Exponer el puerto del backend
EXPOSE 8080

# Comando para ejecutar la aplicación directamente
CMD ["./mvnw", "spring-boot:run"]
