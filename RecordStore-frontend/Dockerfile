FROM node:20-alpine

WORKDIR /app

# Install deps first (better caching)
COPY package*.json ./
RUN npm ci || npm install

# Copy source
COPY . .

EXPOSE 5173

# Vite must bind to 0.0.0.0 inside container
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
