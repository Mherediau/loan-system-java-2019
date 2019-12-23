FROM openjdk:11-jre-slim

LABEL maintainer="Credit Union DevOps <devops@creditunion.com>"
LABEL version="1.0.0"
LABEL description="Loan Management Microservice"

# Create app directory
WORKDIR /app

# Copy JAR file
COPY target/loan-service.jar app.jar

# Create non-root user
RUN groupadd -r loanapp && useradd -r -g loanapp loanapp
RUN chown -R loanapp:loanapp /app

# Switch to non-root user
USER loanapp

# Expose service port
EXPOSE 8081

# JVM options
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
