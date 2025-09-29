#!/bin/bash

set -e

export USER_ID=${PUID:=9001}
export GROUP_ID=${PGID:=9001}

# 更新barda用户的ID
if [ ! $(id --user barda) -eq ${USER_ID} ]; then
    usermod --uid ${USER_ID} barda
    echo "更新Barda ID为: ${USER_ID}"
fi

# 更新barda组的ID
if [ ! $(id --group barda) -eq ${GROUP_ID} ]; then
    groupmod --gid ${GROUP_ID} barda
    echo "更新Barda组为: ${GROUP_ID}"
fi

# 更新mongo应该监听的主机
# 如果 MONGODB_EXPOSED 为true，则设置为监听所有接口
if [ "${MONGODB_EXPOSED}" = "true" ]; then
    export MONGO_LISTEN_HOST="0.0.0.0"
else
    export MONGO_LISTEN_HOST="127.0.0.1"
fi

LOGS="/barda-stacks/logs"
DATA="/barda-stacks/data"
# 创建用于保存应用程序日志和数据的文件夹
mkdir -p ${LOGS}/redis \
    ${LOGS}/mongodb \
    ${LOGS}/api-service \
    ${LOGS}/node-service \
    ${LOGS}/frontend \
    ${DATA}/redis \
    ${DATA}/mongodb

# 初始化config目录
if [ -f "/barda/init-config-dir.sh" ]; then
    chmod +x /barda/init-config-dir.sh
    /barda/init-config-dir.sh
fi

# 更新日志和数据的所有者
chown -R ${USER_ID}:${GROUP_ID} /barda-stacks/ /barda/etc

# 启用服务
SUPERVISOR_AVAILABLE="/barda/etc/supervisord/conf-available"
SUPERVISOR_ENABLED="/barda/etc/supervisord/conf-enabled"

# 创建文件夹
mkdir -p ${SUPERVISOR_ENABLED}

# 重新创建已启用服务的链接
rm -f ${SUPERVISOR_ENABLED}/*.conf

# 是否启用Redis
if [ "${REDIS_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/01-redis.conf ${SUPERVISOR_ENABLED}/01-redis.conf
fi

# 是否启用Mongdb
if [ "${MONGODB_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/02-mongodb.conf ${SUPERVISOR_ENABLED}/02-mongodb.conf
fi

# 是否启用 api-service
if [ "${API_SERVICE_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/10-api-service.conf ${SUPERVISOR_ENABLED}/10-api-service.conf
fi

# 是否启用 node-service
if [ "${NODE_SERVICE_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/11-node-service.conf ${SUPERVISOR_ENABLED}/11-node-service.conf
fi

# 是否启用 forntend
if [ "${FRONTEND_ENABLED:=true}" = "true" ]; then
    ln ${SUPERVISOR_AVAILABLE}/20-frontend.conf ${SUPERVISOR_ENABLED}/20-frontend.conf
fi

# 处理CMD命令
"$@"
