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

# 初始化并配置MongoDB认证
CONFIG_DIR="/barda-stacks/config"
MONGO_DATA_DIR="/barda-stacks/data/mongodb"
MONGO_INIT_USERNAME_DEFAULT="barda"
MONGO_CRED_FILE="${CONFIG_DIR}/mongodb-credentials.env"

# 确保config目录存在（init-config-dir.sh已处理，这里兜底一次）
mkdir -p "${CONFIG_DIR}"

# 读取/生成root用户名与密码
if [ -z "${MONGO_INITDB_ROOT_USERNAME}" ]; then
	if [ -f "${MONGO_CRED_FILE}" ]; then
		. "${MONGO_CRED_FILE}"
	fi
fi

if [ -z "${MONGO_INITDB_ROOT_USERNAME}" ]; then
	export MONGO_INITDB_ROOT_USERNAME="${MONGO_INIT_USERNAME_DEFAULT}"
fi

if [ -z "${MONGO_INITDB_ROOT_PASSWORD}" ]; then
	# 若未指定密码，尝试从已保存的文件读取
	if [ -f "${MONGO_CRED_FILE}" ]; then
		. "${MONGO_CRED_FILE}"
	fi
fi

if [ -z "${MONGO_INITDB_ROOT_PASSWORD}" ]; then
	# 生成随机密码（长度32）
	export MONGO_INITDB_ROOT_PASSWORD="$(tr -dc 'A-Za-z0-9~!#_+-' < /dev/urandom | head -c 32)"
fi

# 将凭证保存到config目录
echo "MONGO_INITDB_ROOT_USERNAME=${MONGO_INITDB_ROOT_USERNAME}" > "${MONGO_CRED_FILE}"
echo "MONGO_INITDB_ROOT_PASSWORD=${MONGO_INITDB_ROOT_PASSWORD}" >> "${MONGO_CRED_FILE}"
chmod 600 "${MONGO_CRED_FILE}"

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

# 如未初始化，则先无认证启动MongoDB并创建管理员用户，然后关闭
MONGO_INIT_FLAG="${MONGO_DATA_DIR}/.mongodb_auth_initialized"
if [ ! -f "${MONGO_INIT_FLAG}" ]; then
	# 以本地回环接口无认证临时启动，便于初始化用户
	mongod --port 27017 --dbpath "${MONGO_DATA_DIR}" --logpath "/barda-stacks/logs/mongodb/init.log" --bind_ip 127.0.0.1 --fork

	# 等待端口就绪
	for i in 1 2 3 4 5 6 7 8 9 10; do
		if mongo --host 127.0.0.1 --port 27017 --eval "db.adminCommand('ping')" >/dev/null 2>&1; then
			break
		fi
		sleep 1
	done

	# 创建admin用户
	mongo --host 127.0.0.1 --port 27017 <<EOF
use admin
db.createUser({
  user: "${MONGO_INITDB_ROOT_USERNAME}",
  pwd: "${MONGO_INITDB_ROOT_PASSWORD}",
  roles: [ { role: "root", db: "admin" } ]
})
EOF
	# 打印凭证到Docker日志
	echo "[barda] MongoDB 超级管理员账户: ${MONGO_INITDB_ROOT_USERNAME}"
	echo "[barda] MongoDB 超级管理员密码: ${MONGO_INITDB_ROOT_PASSWORD}"
	# 关闭临时实例
	mongo --host 127.0.0.1 --port 27017 admin --eval 'db.shutdownServer()' || true

	# 标记初始化完成
	touch "${MONGO_INIT_FLAG}"
fi

# 若未设置MONGODB_URI或未包含凭证，则根据凭证拼装并导出
if [ -z "${MONGODB_URI}" ] || ! echo "${MONGODB_URI}" | grep -q "@"; then
	export MONGODB_URI="mongodb://${MONGO_INITDB_ROOT_USERNAME}:${MONGO_INITDB_ROOT_PASSWORD}@localhost:27017/barda?authSource=admin"
fi

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
