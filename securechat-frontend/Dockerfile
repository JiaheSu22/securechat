# Build stage
FROM node:22-alpine as build-stage

# Set working directory
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine as production-stage

# Copy build artifacts to nginx directory
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Copy nginx configuration file
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
