##
## Create custom JRE for running Barda server application
##
FROM eclipse-temurin:17-jdk-jammy AS jre-build
RUN jlink --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.se,java.security.jgss,java.security.sasl,java.smartcardio,java.sql,java.sql.rowset,java.transaction.xa,java.xml,java.xml.crypto,jdk.accessibility,jdk.charsets,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.dynalink,jdk.httpserver,jdk.incubator.foreign,jdk.incubator.vector,jdk.internal.vm.ci,jdk.jdwp.agent,jdk.jfr,jdk.jsobject,jdk.localedata,jdk.management,jdk.management.agent,jdk.management.jfr,jdk.naming.dns,jdk.naming.rmi,jdk.net,jdk.nio.mapmode,jdk.sctp,jdk.security.auth,jdk.security.jgss,jdk.unsupported,jdk.xml.dom,jdk.zipfs,jdk.attach \
  --output /build/jre \
  --no-man-pages \
  --no-header-files \
  --compress=2

##
## Build Barda api-service application
##
FROM maven:3.9.5-eclipse-temurin-17-focal AS build-api-service
COPY ./server/api-service /barda-server
WORKDIR /barda-server
RUN --mount=type=cache,target=/root/.m2 mvn install:install-file \
  -Dfile=./lib/rjson-1.3.1-20210724.182155-1.jar \
  -DgroupId=tv.twelvetone.rjson \
  -DartifactId=rjson \
  -Dpackaging=jar \
  -Dversion=1.3.1-SNAPSHOT && \
  mvn -f pom.xml clean package -DskipTests -T 4

# Create required folder structure
RUN mkdir -p /barda/api-service/plugins /barda/api-service/config /barda/api-service/logs \
    && chmod 755 /barda/api-service/config

# Define barda main jar and plugin jars
ARG JAR_FILE=/barda-server/barda-server/target/barda-server-*-SNAPSHOT.jar
ARG PLUGIN_JARS=/barda-server/barda-plugins/*/target/*.jar

# Copy Java runtime for running server
COPY --from=jre-build /build/jre /barda/api-service/jre

# Copy barda server application and plugins
RUN cp ${JAR_FILE} /barda/api-service/server.jar \
  && cp ${PLUGIN_JARS} /barda/api-service/plugins/ \
  && rm /barda/api-service/plugins/original-*.jar \
  && rm /barda/api-service/plugins/sqlBasedPlugin-*-SNAPSHOT.jar

# Copy barda server configuration
COPY server/api-service/barda-server/src/main/resources/selfhost/ce/application.yml /barda/api-service/config/
COPY server/api-service/barda-server/src/main/resources/selfhost/ce/application-selfhost.yml /barda/api-service/config/

# Add bootstrapfile
COPY deploy/docker/api-service/entrypoint.sh /barda/api-service/entrypoint.sh
COPY deploy/docker/api-service/init.sh /barda/api-service/init.sh
RUN chmod +x /barda/api-service/*.sh 

##
## Intermediary Barda api-service image
##
## To create a separate image out of it, build it with:
##   DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-api-service --target barda-ce-api-service .
##
FROM ubuntu:jammy AS barda-ce-api-service
LABEL maintainer="barda"

# 使用国内镜像源加速apt-get
RUN sed -i 's@//.*archive.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && sed -i 's@//.*security.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && apt-get update && apt-get install -y --no-install-recommends gosu \
  && rm -rf /var/cache/apt/lists \
  && addgroup --system --gid 9001 barda \
  && adduser --system --disabled-password --no-create-home --uid 9001 --gid 9001 barda

# Copy barda server configuration
COPY --chown=barda:barda --from=build-api-service /barda/api-service /barda/api-service

EXPOSE 8080
CMD [ "sh" , "/barda/api-service/entrypoint.sh" ]

#############################################################################

##
## Build barda node service
##
FROM ubuntu:jammy AS build-node-service

# 使用国内镜像源加速apt-get
RUN sed -i 's@//.*archive.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && sed -i 's@//.*security.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && apt update && DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y curl ca-certificates

# Download nodejs and install yarn
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
  && apt-get install --no-install-recommends -y nodejs \
  && npm install -g yarn

# Copy and build the node-service app
COPY server/node-service/ /barda/node-service/app/
WORKDIR /barda/node-service/app/
RUN yarn --immutable
RUN yarn build

# Copy startup script
COPY deploy/docker/node-service/entrypoint.sh /barda/node-service/entrypoint.sh
COPY deploy/docker/node-service/init.sh /barda/node-service/init.sh
RUN chmod +x /barda/node-service/*.sh

##
## Intermediary Barda node service image
##
## To create a separate image out of it, build it with:
##   DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-node-service --target barda-ce-node-service .
##
FROM ubuntu:jammy AS barda-ce-node-service
LABEL maintainer="barda"

# 使用国内镜像源加速apt-get
RUN sed -i 's@//.*archive.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && sed -i 's@//.*security.ubuntu.com@//mirrors.aliyun.com@g' /etc/apt/sources.list \
  && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y curl ca-certificates \
  && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
  && apt-get install --no-install-recommends -y nodejs gosu \
  && npm install -g yarn \
  && rm -rf /var/cache/apt/lists \
  && addgroup --system --gid 9001 barda \
  && adduser --system --disabled-password --no-create-home --uid 9001 --gid 9001 barda

COPY --from=build-node-service /barda/node-service /barda/node-service

EXPOSE 6060
CMD [ "/bin/sh", "/barda/node-service/entrypoint.sh" ]

#############################################################################

##
## Build barda client application
##
FROM node:20.15.1-slim AS build-client
COPY ./client /barda-client
WORKDIR /barda-client
RUN yarn --immutable

# curl is required for yarn build to succeed, because it calls it while building client
# 使用国内镜像源加速apt-get
RUN sed -i 's|http://deb.debian.org/debian|http://mirrors.aliyun.com/debian|g; s|http://security.debian.org/debian-security|http://mirrors.aliyun.com/debian-security|g' /etc/apt/sources.list.d/debian.sources \
  && apt-get update && apt-get install -y --no-install-recommends curl ca-certificates

ARG REACT_APP_COMMIT_ID=beta
ARG REACT_APP_ENV=production
ARG REACT_APP_EDITION=community
ARG REACT_APP_DISABLE_JS_SANDBOX=true
RUN yarn build


##
## Intermediary Barda client image
##
## To create a separate image out of it, build it with:
##   DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-frontend --target barda-ce-frontend .
##
FROM nginx:1.23.3 AS barda-ce-frontend
LABEL maintainer="barda"

# Change default nginx user into barda user
RUN usermod --login barda --uid 9001 nginx \
  && groupmod --new-name barda --gid 9001 nginx

# Copy barda client data
COPY --chown=barda:barda --from=build-client /barda-client/packages/barda/build/ /barda/client

# Copy additional nginx init scripts
COPY deploy/docker/frontend/00-change-nginx-user.sh /docker-entrypoint.d/00-change-nginx-user.sh
COPY deploy/docker/frontend/01-update-nginx-conf.sh /docker-entrypoint.d/01-update-nginx-conf.sh

RUN chmod +x /docker-entrypoint.d/00-change-nginx-user.sh && \
  chmod +x /docker-entrypoint.d/01-update-nginx-conf.sh

COPY deploy/docker/frontend/nginx.conf /etc/nginx/nginx.conf
EXPOSE 3000

#############################################################################

##
## Build Barda all-in-one image
##
FROM barda-ce-frontend
LABEL maintainer="barda"

# 创建预装库目录
RUN mkdir -p /app/preload-libraries

# 使用国内镜像源加速apt-get
RUN sed -i 's|deb.debian.org|mirrors.aliyun.com|g; s|deb.debian.org|mirrors.aliyun.com|g' /etc/apt/sources.list \
  && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y bash gnupg curl lsb-release \
  && curl -fsSL https://packages.redis.io/gpg | gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg \
  && echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb bullseye main" | tee /etc/apt/sources.list.d/redis.list \
  && curl -fsSL https://www.mongodb.org/static/pgp/server-4.4.asc | gpg --dearmor -o /usr/share/keyrings/mongodb-archive-keyring.gpg \
  && echo "deb [signed-by=/usr/share/keyrings/mongodb-archive-keyring.gpg arch=amd64,arm64] http://repo.mongodb.org/apt/ubuntu focal/mongodb-org/4.4 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.4.list \
  && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
  && apt-get update && DEBIAN_FRONTEND=noninteractive apt-get -y install --no-install-recommends -y \
  mongodb-org \
  redis \
  supervisor \
  gosu \
  nodejs \
  wget \
  && npm install -g yarn \
  && rm -rf /var/cache/apt/lists  \
  && cd /app/preload-libraries && \
  # JMESPath 0.16.0
  wget -O jmespath.min.js https://cdn.jsdelivr.net/npm/jmespath@0.16.0/jmespath.min.js && \
  # jsPDF 4.2.1
  wget -O jspdf.min.js https://cdn.jsdelivr.net/npm/jspdf@4.2.1/dist/jspdf.umd.min.js  && \
  # i18next 26.0.4
  wget -O i18next.min.js https://cdn.jsdelivr.net/npm/i18next@26.0.4/i18next.min.js && \
  # JSONPath 1.3.0
  wget -O jsonpath.min.js https://cdn.jsdelivr.net/npm/jsonpath@1.3.0/jsonpath.min.js

# Add barda api-service
COPY --chown=barda:barda --from=barda-ce-api-service /barda/api-service /barda/api-service

# Add barda node-service
COPY --chown=barda:barda --from=barda-ce-node-service /barda/node-service /barda/node-service

# Add services configuration
COPY --chown=barda:barda deploy/docker/all-in-one/etc /barda/etc

# Add config initialization script
COPY --chown=barda:barda deploy/docker/init-config-dir.sh /barda/init-config-dir.sh

# Add startup script
COPY --chown=barda:barda deploy/docker/all-in-one/entrypoint.sh /barda/entrypoint.sh

EXPOSE 3000

ENTRYPOINT [ "/bin/sh" , "/barda/entrypoint.sh" ]
CMD ["/usr/bin/supervisord", "-n" , "-c" , "/barda/etc/supervisord.conf"]

