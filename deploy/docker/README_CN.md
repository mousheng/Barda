# 百搭Docker镜像

包含的Dockerfile可用于构建**all-in-one**镜像，该镜像包含并运行所有所需的服务在一个容器内，或者可以构建前端和后端服务的独立镜像。

有关运行一体化镜像或多镜像部署的示例，请参见 **deploy/docker/docker-compose.yaml** 和 **deploy/docker/docker-compose-multi.yaml**文档。


## 一、一体化镜像
此映像包含在一个容器中运行Barda平台所需的所有服务。


### 构建方法

先切换到项目的根目录，然后运行以下代码:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t moushengkoo/barda-ce .
```

### 配置

镜像可以通过设置环境变量来配置镜像.


| 环境变量                 | 描述                                             | 默认值                                             |
| ------------------------ | ------------------------------------------------ | -------------------------------------------------- |
| `REDIS_ENABLED`          | 如果为 **true**，则在容器中启动 Redis服务        | `true`                                             |
| `MONGODB_ENABLED`        | 如果为 **true**，则在容器中启动 Mongo数据库      | `true`                                             |
| `API_SERVICE_ENABLED`    | 如果为 **true**，则在容器中启动 BardaAPI服务     | `true`                                             |
| `NODE_SERVICE_ENABLED`   | 如果为 **true**，则在容器中启动 Barda节点服务    | `true`                                             |
| `FRONTEND_ENABLED`       | 如果为 **true**，则在容器中启动 Barda Web前端    | `true`                                             |
| `PUID`                   | 运行服务的用户ID。它将拥有所有创建的日志和数据。 | `9001`                                             |
| `PGID`                   | 运行服务的用户组ID。                             | `9001`                                             |
| `MONGODB_URI`            | Mongo数据库连接字符串                            | `mongodb://localhost:27017/barda?authSource=admin` |
| `REDIS_URL`              | Redis服务器 URL                                  | `redis://localhost:6379`                           |
| `JS_EXECUTOR_URI`        | 节点服务URL                                      | `http://localhost:6060`                            |
| `ENABLE_USER_SIGN_UP`    | 启用新用户注册                                   | `true`                                             |
| `ENCRYPTION_PASSWORD`    | 加密密码                                         | `barda.dev`                                        |
| `ENCRYPTION_SALT`        | 用于加密密码的盐                                 | `barda.dev`                                        |
| `CORS_ALLOWED_DOMAINS`   | 允许的 CORS 域                                   | `*`                                                |
| `BARDA_API_SERVICE_URL`  | Barda API 服务URL                                | `http://localhost:8080`                            |
| `BARDA_NODE_SERVICE_URL` | Barda 节点服务（JS 执行器）URL                   | `http://localhost:6060`                            |

## 二、分体镜像

### 1、构建api-service镜像

独立的百搭API服务镜像

#### 构建方法

先切换到项目的根目录，然后运行以下代码:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-api-service --target barda-ce-api-service .
```

#### 配置

镜像可以通过设置环境变量来配置镜像.

| 环境变量               | 描述                                              | 默认值                                             |
| ---------------------- | ------------------------------------------------- | -------------------------------------------------- |
| `PUID`                 | 运行服务的用户 ID。它将拥有所有创建的日志和数据。 | `9001`                                             |
| `PGID`                 | 运行服务的用户组 ID。                             | `9001`                                             |
| `MONGODB_URI`          | Mongo 数据库连接字符串                            | `mongodb://localhost:27017/barda?authSource=admin` |
| `REDIS_URL`            | Redis 服务器 URL                                  | `redis://localhost:6379`                           |
| `JS_EXECUTOR_URI`      | 节点服务 URL                                      | `http://localhost:6060`                            |
| `ENABLE_USER_SIGN_UP`  | 启用新用户注册                                    | `true`                                             |
| `ENCRYPTION_PASSWORD`  | 加密密码                                          | `barda.dev`                                        |
| `ENCRYPTION_SALT`      | 用于加密密码的盐                                  | `barda.dev`                                        |
| `CORS_ALLOWED_DOMAINS` | 允许的 CORS 域                                    | `*`                                                |



### 2、构建node-service镜像

node-service是Barda的JS执行器.

#### 构建方法

先切换到项目的根目录，然后运行以下代码:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-node-service --target barda-ce-node-service .
```

#### 配置

镜像可以通过设置环境变量来配置镜像.

| 环境变量                | 描述                                             | 默认值                  |
| ----------------------- | ------------------------------------------------ | ----------------------- |
| `PUID`                  | 运行服务的用户ID。它将拥有所有创建的日志和数据。 | `9001`                  |
| `PGID`                  | 运行服务的用户组ID。                             | `9001`                  |
| `BARDA_API_SERVICE_URL` | Barda API 服务URL                                | `http://localhost:8080` |


### 3、构建前端镜像

独立Barda前端镜像。

#### 构建方法

先切换到项目的根目录，然后运行以下代码:

```
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t bardadev/barda-ce-frontend --target barda-ce-frontend .
```

#### 配置

镜像可以通过设置环境变量来配置镜像.

| 环境变量                 | 描述                                              | 默认值                  |
| ------------------------ | ------------------------------------------------- | ----------------------- |
| `PUID`                   | 运行服务的用户 ID。它将拥有所有创建的日志和数据。 | `9001`                  |
| `PGID`                   | 运行服务的用户组 ID。                             | `9001`                  |
| `BARDA_API_SERVICE_URL`  | Barda API 服务 URL                                | `http://localhost:8080` |
| `BARDA_NODE_SERVICE_URL` | Barda 节点服务（JS 执行器）URL                    | `http://localhost:6060` |


