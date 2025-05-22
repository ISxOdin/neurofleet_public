FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y curl \
    && curl -sL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && curl -L https://www.npmjs.com/install.sh | npm_install="10.2.3" | sh
WORKDIR /usr/src/app
COPY . .

# Create .env.production in frontend directory with proper escaping and validation
ARG VITE_GOOGLE_MAPS_API_KEY
RUN if [ -z "$VITE_GOOGLE_MAPS_API_KEY" ]; then \
    echo "Error: VITE_GOOGLE_MAPS_API_KEY is not set" && exit 1; \
    fi && \
    echo "VITE_GOOGLE_MAPS_API_KEY=$VITE_GOOGLE_MAPS_API_KEY" > frontend/.env.production && \
    chmod 600 frontend/.env.production

# Set up frontend build environment
RUN mkdir -p frontend/.svelte-kit && \
    echo '{}' > frontend/.svelte-kit/tsconfig.json && \
    cd frontend && \
    npm install && \
    # Verify environment variable is available
    if [ ! -f .env.production ] || [ ! -s .env.production ]; then \
    echo "Error: .env.production file is missing or empty" && exit 1; \
    fi && \
    # Run build with environment variable explicitly set
    VITE_GOOGLE_MAPS_API_KEY="$VITE_GOOGLE_MAPS_API_KEY" npm run build

# Clean up frontend and build backend
RUN rm -r frontend && \
    sed -i 's/\r$//' mvnw && \
    chmod +x mvnw && \
    ./mvnw package

EXPOSE 8080
CMD ["java", "-jar", "/usr/src/app/target/neurofleet-0.0.1-SNAPSHOT.jar"]