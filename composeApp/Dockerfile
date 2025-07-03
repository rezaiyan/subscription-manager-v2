FROM node:alpine

# Install serve globally
RUN npm install -g serve

# Copy the built website files from webpack output
COPY build/kotlin-webpack/wasmJs/productionExecutable/ /app/
COPY build/processedResources/wasmJs/main/index.html /app/

WORKDIR /app

EXPOSE 3002

# Start the static file server
CMD ["serve", "-l", "3002", "."] 