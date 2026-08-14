FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN set -eu; \
    npm run build -- --configuration production; \
    mkdir -p /output; \
    index_file="$(find dist -name index.html | head -n 1)"; \
    if [ -z "$index_file" ]; then \
      echo "Build concluido sem index.html em dist/." >&2; \
      exit 1; \
    fi; \
    out_dir="$(dirname "$index_file")"; \
    cp -R "$out_dir"/. /output/

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /output /usr/share/nginx/html

EXPOSE 80
