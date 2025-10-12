# Windows 环境开发指南

本指南专门针对 Windows 用户，详细介绍如何在纯 Windows 环境下使用 Barda 开发脚本进行项目开发，可以不使用Docker就快速进行前端开发，可以大量节省内存开销，适用于内存小于32G的开发者和无法下载Docker镜像的开发者。

## 系统要求

- **操作系统**：Windows 10 (版本 1903 或更高) 或 Windows 11
- **Node.js**：20.x 或更高版本
- **Docker Desktop**：4.0 或更高版本（可选,用于容器化开发）

## 环境准备

### 1. 安装 Node.js

**方法一：官网下载**
1. 访问 [Node.js 官网](https://nodejs.org/)
2. 下载 LTS 版本（推荐）
3. 运行安装程序，按默认设置安装

**方法二：使用包管理器**
```powershell
# 使用 Chocolatey
choco install nodejs

# 使用 winget
winget install OpenJS.NodeJS
```

**验证安装**
```powershell
node --version
npm --version
```

### 2. 安装 Yarn

```powershell
npm install -g yarn
```

### 3. 安装 Docker Desktop(可选)

1. 下载 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. 运行安装程序
3. 启动 Docker Desktop
4. 确保 WSL 2 后端已启用（设置 > General > Use the WSL 2 based engine）

**验证安装**
```powershell
docker --version
docker-compose --version
```

### 4. 安装 Git

```powershell
# 使用 winget
winget install Git.Git

# 或下载安装程序
# https://git-scm.com/download/win
```

## 项目设置

### 1. 克隆项目

```powershell
# 在 PowerShell 中执行
git clone --depth 1 https://github.com/mousheng/barda.git
# 或使用国内镜像
git clone --depth 1 https://gitee.com/moushengkoo/Barda.git

cd barda
```

### 2. 设置 PowerShell 执行策略

```powershell
# 以管理员身份运行 PowerShell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 使用开发脚本

### 快速开始

1. **进入开发目录**
   ```powershell
   cd develop
   ```

2. **运行开发脚本**
   ```powershell
   .\dev.ps1
   ```

3. **选择开发模式**
   脚本会显示交互式菜单，根据您的需求选择：

   ```
   请选择开发模式:
     1) 前端远程调试
     2) 前端本地调试
     3) 前后端本地调试
     q) 退出
   ```

### 开发模式详解

#### 模式 1：前端远程调试

**适用场景**：
- 仅修改前端代码
- 快速跑通项目

**特点**：
- 最简单，无需本地后端环境
- 连接远程后端服务
- 启动速度快

**使用步骤**：
1. 选择模式 1
2. 脚本自动安装前端依赖
3. 启动前端开发服务器
4. 浏览器自动打开 `http://localhost:8000`

#### 模式 2：前端本地调试

**适用场景**：
- 前端开发为主
- 需要本地后端服务
- 避免复杂的本地环境配置

**特点**：
- 前端本地运行
- 后端使用 Docker 容器

**使用步骤**：
1. 选择模式 2
2. 脚本自动启动后端容器
3. 安装前端依赖
4. 启动前端开发服务器

**后端容器管理**：
```powershell
# 查看容器状态
docker ps

# 查看容器日志
docker logs barda-client

# 停止容器
docker stop barda-client
```

#### 模式 3：前后端本地调试

**适用场景**：
- 需要修改后端代码
- 完整的全栈开发
- 需要调试后端逻辑

**特点**：
- 最完整的开发环境
- 支持前后端联调
- 自动环境检测和配置

**自动安装功能**：
如果检测到缺少 JDK 或 Maven，脚本会询问是否自动安装：

```powershell
未检测到 JDK 或 Maven。JDK17 与 Maven 为本地编译后端所必需。
是否自动安装? (y=安装 / n=取消 / s=切换模式2)
```

选择 `y` 后，脚本会：
1. 使用 winget 安装 OpenJDK 17
2. 自动下载并安装Maven
3. 自动配置环境变量

**数据库自动配置**：
脚本会自动检测并启动必要的数据库服务：

- **MongoDB**：自动启动 `mongo:latest` 容器
- **Redis**：自动启动 `redis:latest` 容器

## 高级功能

### 1. IntelliJ IDEA 集成

脚本会自动查找并启动 IntelliJ IDEA：

**支持的安装位置**：
- `C:\Program Files\JetBrains\`
- `%LOCALAPPDATA%\JetBrains\Toolbox\apps\IDEA-U\`
- `%LOCALAPPDATA%\JetBrains\Toolbox\apps\IDEA-C\`

### 2. 端口管理

**默认端口**：
- 前端开发服务器：8000
- 后端 API 服务：3000 (模式2) 或 8080 (模式3)
- MongoDB：27017
- Redis：6379

**端口冲突处理**：
```powershell
# 查看端口占用
netstat -ano | findstr :8000

# 终止占用端口的进程
taskkill /PID <进程ID> /F
```

### 3. 环境变量配置

脚本会自动设置必要的环境变量：

```powershell
# 模式 1
$env:BARDA_API_SERVICE_URL = "http://43.133.22.234:30000"

# 模式 2
$env:BARDA_API_SERVICE_URL = "http://127.0.0.1:3000"

# 模式 3
$env:BARDA_API_SERVICE_URL = "http://127.0.0.1:8080"
```

## 故障排除

### 常见问题

#### 1. PowerShell 执行策略错误

**错误信息**：
```
无法加载文件 dev.ps1，因为在此系统上禁止运行脚本
```

**解决方案**：
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### 2. Docker 未启动

**错误信息**：
```
Docker 不可用，无法执行模式2/3
```

**解决方案**：
1. 启动 Docker Desktop
2. 等待 Docker 完全启动
3. 重新运行脚本

#### 3. 端口被占用

**错误信息**：
```
端口 8000 已被占用
```

**解决方案**：
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8000

# 终止进程
taskkill /PID <进程ID> /F
```

#### 4. Node.js 版本不兼容

**错误信息**：
```
Node.js 版本过低
```

**解决方案**：
1. 升级到 Node.js 16.x 或更高版本
2. 或使用 nvm-windows 管理多个 Node.js 版本

#### 5. 依赖安装失败

**错误信息**：
```
yarn install 失败
```

**解决方案**：
```powershell
# 清理缓存
yarn cache clean

# 删除 node_modules
Remove-Item -Recurse -Force node_modules

# 重新安装
yarn install
```

### 性能优化

#### 1. 使用 SSD 硬盘
将项目放在 SSD 硬盘上可以显著提升开发体验。

#### 2. 增加 Docker 内存分配
在 Docker Desktop 设置中增加内存分配（推荐 8GB 或更多）。

#### 3. 使用 WSL 2 后端
确保 Docker Desktop 使用 WSL 2 后端以获得更好的性能。

## 相关资源

- [调试后端代码](debugBackendCode.md)
- [Docker 部署指南](../docker.md)
- [Node.js 官方文档](https://nodejs.org/docs/)
- [Docker Desktop 文档](https://docs.docker.com/desktop/windows/)
