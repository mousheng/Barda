FROM node:20.15.1-slim

LABEL author="moushengkoo"

ENV NODE_ENV=development \
    PATH=/app/client/node_modules/.bin:$PATH \
    BARDA_API_SERVICE_URL="http://localhost:8080" \
    NODE_OPTIONS="--max-old-space-size=6144"


# 设置工作目录
WORKDIR /app/client

RUN mkdir -p /app/client && \
    yarn config set npmRegistryServer https://registry.npmmirror.com/ && \
    yarn cache clean


# 开始
CMD ["yarn", "start"]

EXPOSE 8000
