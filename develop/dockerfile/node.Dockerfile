FROM node:20.15.1-slim

LABEL author="moushengkoo"

ENV NODE_ENV=development \
    PATH=/app/node-service/node_modules/.bin:$PATH

# COPY ./server/node-service/ /app/node-service

# 设置工作目录
WORKDIR /app/node-service

RUN mkdir -p /app/node-service && yarn config set npmRegistryServer https://registry.npmmirror.com/ && yarn install



# 开始
CMD ["yarn", "start"]

EXPOSE 6060
