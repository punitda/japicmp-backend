# Download images
FROM openjdk:11
FROM gradle:8.4

# Copy project files
COPY --chown=gradle:gradle . /home/gradle/src

# Copy Env config
COPY env.json /home/gradle/src/src/main/resources

# Set working directory
WORKDIR /home/gradle/src

# Build Fat Jar
RUN gradle buildFatJar --no-daemon

# Expose port
EXPOSE 8080:8080

# Run the app
ENTRYPOINT ["java","-jar","build/libs/fat.jar"]