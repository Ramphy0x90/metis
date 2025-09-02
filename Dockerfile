# Multi-stage build for Spring Boot application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install necessary packages
RUN apk add --no-cache tzdata

# Set timezone
ENV TZ=UTC

# Create app user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to app user
RUN chown -R appuser:appgroup /app

# Switch to app user
USER appuser

# Run the startup script
ENTRYPOINT ["/app/start.sh"]

# Expose port
EXPOSE 8080

# Expose debug port
EXPOSE 5005

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Create startup script
RUN echo '#!/bin/sh' > /app/start.sh && \
    echo 'if [ "$DEBUG_ENABLED" = "true" ]; then' >> /app/start.sh && \
    echo '  echo "Starting application in debug mode..."' >> /app/start.sh && \
    echo '  java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar' >> /app/start.sh && \
    echo 'else' >> /app/start.sh && \
    echo '  echo "Starting application in normal mode..."' >> /app/start.sh && \
    echo '  java -jar app.jar' >> /app/start.sh && \
    echo 'fi' >> /app/start.sh && \
    chmod +x /app/start.sh

# Change ownership to app user
RUN chown -R appuser:appgroup /app
