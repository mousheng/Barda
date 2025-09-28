FROM node:20.15.1-slim
LABEL description="Barda Docs images."
WORKDIR /docs
RUN npm install -g docsify-cli@latest --registry https://registry.npmmirror.com
EXPOSE 3000/tcp
ENTRYPOINT docsify serve .