# WSL2 环境开发指南

本指南专门针对 Windows WSL2 用户，详细介绍如何在 WSL2 环境下使用 Barda 开发脚本进行项目开发，使用Wsl 2可以保持Windows环境的整洁，并且我已经将前后端做了容器化开发，你无需设置开发环境即可一键开始开发，前提就是你能下载Docker镜像。

## 系统要求

- **Windows 版本**：Windows 10 (版本 2004 或更高) 或 Windows 11
- **WSL 版本**：WSL 2
- **Linux 发行版**：Ubuntu 20.04+ (推荐) 或其他主流发行版
- **Docker Desktop**：4.0 或更高版本

## 环境准备（可选）

### 1. 启用 WSL 2

**Windows 10/11 启用 WSL 2**：
```powershell
# 以管理员身份运行 PowerShell
# 启用 WSL 功能
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart

# 启用虚拟机平台
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart

# 重启计算机
Restart-Computer
```

**安装 WSL 2**：
```powershell
# 设置 WSL 2 为默认版本
wsl --set-default-version 2

# 安装 Ubuntu (推荐)
wsl --install -d Ubuntu
```

### 2. 配置 Docker Desktop

1. 安装 [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
2. 启动 Docker Desktop
3. 启用 WSL 2 集成：
   - 打开 Docker Desktop 设置
   - 进入 "Resources" > "WSL Integration"
   - 启用 "Enable integration with my default WSL distro"
   - 选择您的 WSL 发行版
   - 点击 "Apply & Restart"

### 3. 在 WSL2 中安装必要工具

**更新系统包**：
```bash
sudo apt update && sudo apt upgrade -y
```

## 项目设置

### 1. 克隆项目

**重要提示**：将项目放在 Linux 文件系统中以获得最佳性能！

```bash
# 进入用户主目录（Linux 文件系统）
cd ~

# 克隆项目
git clone --depth=1 https://github.com/mousheng/barda.git
# 或使用国内镜像
git clone --depth=1 https://gitee.com/moushengkoo/Barda.git

cd barda
```

**避免的性能陷阱**：
```bash
# ❌ 错误：不要将项目放在 Windows 文件系统中
cd /mnt/c/Users/YourName/Projects/barda

# ✅ 正确：将项目放在 Linux 文件系统中
cd ~/barda
```

### 2. 设置脚本权限

```bash
cd develop
chmod +x dev.sh
```

## 使用开发脚本

### 快速开始

1. **进入开发目录**
   ```bash
   cd develop
   ```

2. **查看帮助信息**
   ```bash
   sudo ./dev.sh
   ```

3. **选择开发模式**

| 选项 | 功能 | 说明 |
|------|------|------|
| `-fr` | 调试远程前端 | 启动wsl2开发环境，连接远程后端 |
| `-f` | 调试本地前端 | 启动前端开发容器，连接本地后端 |
| `-rf` | 重置后调试前端 | 先删除容器，再启动前端调试 |
| `-dc` | 调试组件 | 启动组件开发环境 |
| `-cf` | 自定义调试前端 | 允许输入自定义启动命令 |
| `-b` | 调试前后端 | 启动完整的前后端开发环境 |
| `-rb` | 重置后调试前后端 | 先删除容器，再启动完整开发环境 |
| `-c` | 清理环境 | 删除容器、镜像和临时文件 |
| `-p` | 构建镜像 | 构建并发布 Docker 镜像 |
| `-o` | 设置文件所有者 | 设置项目文件的所有者为当前用户 |

### 开发模式详解

#### 远程调试前端 (`-fr`)

**适用场景**:
- 仅修改前端代码
- 无需使用Docker

**使用示例**：
```bash
./dev.sh -fr
```
**功能特点**：
- 自动打开浏览器（端口 30000, 8000）
- 连接远程后端

#### 本地调试前端 (`-f`)

**适用场景**：
- 仅修改前端代码
- 连接远程后端服务
- 快速原型开发

**使用示例**：
```bash
sudo ./dev.sh -f
```

**功能特点**：
- 自动打开浏览器（端口 30000, 8000）
- 启动前端开发容器

#### 重置后调试前端 (`-rf`)

**适用场景**：
- 需要清理之前的容器状态
- 解决容器相关问题

**使用示例**：
```bash
sudo ./dev.sh -rf
```

**执行流程**：
1. 删除现有容器
2. 重新创建容器
3. 启动前端开发环境

#### 调试组件 (`-dc`)

**适用场景**：
- 开发自定义组件
- 组件库开发

**使用示例**：
```bash
sudo ./dev.sh -dc
```

**功能特点**：
- 自动打开浏览器（端口 30000, 9000）
- 启动组件开发环境
- 支持组件热重载

#### 自定义调试前端 (`-cf`)

**适用场景**：
- 需要自定义启动命令
- 特殊开发需求

**使用示例**：
```bash
sudo ./dev.sh -cf
# 然后输入自定义命令，如：
# yarn build && yarn start
```

#### 调试前后端 (`-b`)

**适用场景**：
- 完整的前后端开发
- 需要修改后端代码

**使用示例**：
```bash
sudo ./dev.sh -b
```

**功能特点**：
- 自动配置 SSH 密钥
- 支持 IntelliJ IDEA 远程开发
- 启动完整开发环境
- 自动打开浏览器

#### 重置后调试前后端 (`-rb`)

**适用场景**：
- 需要清理环境后重新开始
- 解决环境冲突问题

**使用示例**：
```bash
sudo ./dev.sh -rb
```

#### 清理环境 (`-c`)

**使用示例**：
```bash
sudo ./dev.sh -c
```

**清理内容**：
- 删除所有开发容器
- 删除开发镜像
- 清理临时文件（可选）
- 清理依赖缓存（可选）
- 清理 Docker 构建缓存

#### 构建镜像 (`-p`)

**使用示例**：
```bash
sudo ./dev.sh -p
```

**功能**：
- 构建 Barda 多合一镜像
- 支持自定义镜像名称和标签
- 使用 Docker BuildKit 加速构建

#### 设置文件所有者 (`-o`)

**使用示例**：
```bash
sudo ./dev.sh -o
```

**功能**：
- 设置项目文件所有者为当前用户
- 解决权限问题
- 避免文件访问冲突

## 高级功能

### 1. SSH 远程开发支持

在前后端调试模式下，脚本会自动配置 SSH 密钥：

**自动配置流程**：
1. 检查 Windows 用户的 SSH 密钥
2. 如果不存在，自动生成密钥对
3. 将公钥复制到容器的 authorized_keys
4. 支持 IntelliJ IDEA 远程开发

**手动连接**：
```bash
# 使用 SSH 连接到开发容器
ssh -p 2222 root@127.0.0.1
```

### 2. 容器管理

**查看容器状态**：
```bash
docker ps -a
```

**查看容器日志**：
```bash
docker logs barda-client
```

**进入容器**：
```bash
docker exec -it barda-client sh
```

**停止容器**：
```bash
docker stop barda-client
```

### 3. 端口管理

**默认端口映射**：
- 前端开发服务器：8000
- 组件开发服务器：9000
- 后端 API 服务：30000
- SSH 服务：2222

**查看端口占用**：
```bash
sudo netstat -tlnp | grep :8000
```

**释放端口**：
```bash
# 查找占用端口的进程
sudo lsof -i :8000

# 终止进程
sudo kill -9 <进程ID>
```

### 4. 环境变量配置

脚本使用以下环境配置文件：

- `env/frontDev.env` - 前端开发环境
- `env/frontBackDev.env` - 前后端开发环境

**自定义环境变量**：
```bash
# 在脚本中设置
export BARDA_API_SERVICE_URL=http://your-backend-url:port
```

## 性能优化

### 1. 文件系统优化

**使用 Linux 文件系统**：
```bash
# ✅ 推荐：项目放在 Linux 文件系统
cd ~/barda

# ❌ 避免：项目放在 Windows 文件系统
cd /mnt/c/Users/YourName/barda
```

**性能对比**：
- Linux 文件系统：文件操作速度提升 10-20 倍
- 减少跨文件系统调用的开销
- 更好的 Docker 性能

### 2. Docker 优化

**增加 Docker 内存分配**：
1. 打开 Docker Desktop 设置
2. 进入 "Resources" > "Advanced"
3. 增加内存分配（推荐 8GB 或更多）

**使用 Docker BuildKit**：
```bash
# 脚本已自动启用
export DOCKER_BUILDKIT=1
```

### 3. WSL2 优化

**增加 WSL2 内存限制**：
创建或编辑 `%USERPROFILE%\.wslconfig`：
```ini
[wsl2]
memory=8GB
processors=4
swap=2GB
```

**启用 WSL2 的 systemd**（Ubuntu 22.04+）：
```bash
# 编辑 /etc/wsl.conf
sudo vim /etc/wsl.conf

# 添加以下内容
[boot]
systemd=true
```

## 故障排除

### 常见问题

#### 1. Docker 命令未找到

**错误信息**：
```
sudo: docker: command not found
```

**解决方案**：
1. 确保 Docker Desktop 已启动
2. 检查 WSL 2 集成是否启用
3. 重启 WSL2：
   ```bash
   # 在 Windows PowerShell 中执行
   wsl --shutdown
   wsl
   ```

#### 2. 权限问题

**错误信息**：
```
Permission denied
```

**解决方案**：
```bash
# 设置脚本执行权限
chmod +x dev.sh

# 使用 sudo 运行
sudo ./dev.sh -f

# 或设置文件所有者
sudo ./dev.sh -o
```

#### 3. 端口被占用

**错误信息**：
```
Port 8000 is already in use
```

**解决方案**：
```bash
# 查看端口占用
sudo netstat -tlnp | grep :8000

# 终止占用进程
sudo kill -9 <进程ID>

# 或使用脚本清理功能
sudo ./dev.sh -c
```

#### 4. 容器启动失败

**错误信息**：
```
Container failed to start
```

**解决方案**：
```bash
# 查看容器日志
docker logs barda-client

# 清理并重新创建
sudo ./dev.sh -c
sudo ./dev.sh -f
```

#### 5. 网络连接问题

**错误信息**：
```
Connection refused
```

**解决方案**：
```bash
# 检查 Docker 网络
docker network ls

# 重启 Docker 服务
sudo systemctl restart docker

# 或重启 Docker Desktop
```

#### 6. 文件同步问题

**问题**：修改文件后容器内未更新

**解决方案**：
```bash
# 检查挂载点
docker inspect barda-client | grep Mounts

# 确保项目在 Linux 文件系统中
pwd  # 应该显示类似 /home/username/barda
```

### 性能问题

#### 1. 文件操作缓慢

**原因**：项目在 Windows 文件系统中

**解决方案**：
```bash
# 将项目移动到 Linux 文件系统
mv /mnt/c/Users/YourName/barda ~/barda
cd ~/barda
```

#### 2. 内存不足

**解决方案**：
1. 增加 WSL2 内存限制（见性能优化部分）
2. 增加 Docker Desktop 内存分配
3. 关闭不必要的应用程序

## 相关资源

- [Windows 开发指南](windows-dev-guide.md)
- [调试后端代码](debugBackendCode.md)
- [WSL2 官方文档](https://docs.microsoft.com/en-us/windows/wsl/)
- [Docker Desktop 文档](https://docs.docker.com/desktop/windows/wsl/)
- [Ubuntu 官方文档](https://ubuntu.com/wsl)
