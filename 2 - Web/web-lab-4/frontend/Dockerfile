FROM nginx:alpine

RUN apk add --no-cache curl

WORKDIR /usr/share/nginx/html
RUN rm -rf ./*

COPY index.html ./
COPY src/ ./src/

COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN chmod -R 755 /usr/share/nginx/html

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
