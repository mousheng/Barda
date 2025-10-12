# 快速开始开发

本文档提供一些常见开发场景的快速开始示例，帮助您快速上手 Barda 开发脚本。

## 开发场景

**第一步：进入开发目录**
```bash
cd develop
```

### 场景一：无需安装Docker，最快速度跑通项目

<!-- tabs:start -->
<!-- tab:Windows 用户 -->
```powershell
# 1. 运行脚本并选择模式 1
.\dev.ps1
# 选择: 1) 前端远程调试

# 2. 等待自动安装依赖和启动
# 3. 浏览器会自动打开 http://localhost:8000
```

<!-- tab:WSL2 用户 -->
```bash
# 1. 启动前端调试
./dev.sh -fr

# 2. 等待自动安装依赖和启动
# 3. 浏览器会自动打开相关页面
```
<!-- tabs:end -->
### 场景二：远程调试速度太慢，我想本地调试前端

<!-- tabs:start -->
<!-- tab:Windows 用户 -->
```powershell
# 1. 运行脚本并选择模式 2
.\dev.ps1
# 选择: 2) 前端本地调试

# 2. 脚本会自动启动后端容器
# 3. 前端连接本地后端服务
```

<!-- tab:WSL2 用户 -->
```bash
# 1. 启动前端本地调试
sudo ./dev.sh -f

# 2. 脚本会自动处理容器管理
```
<!-- tabs:end -->

### 场景三：我是全栈开发者，需要修改后端代码

<!-- tabs:start -->
<!-- tab:Windows 用户 -->
```powershell
# 1. 运行脚本并选择模式 3
.\dev.ps1
# 选择: 3) 前后端本地调试

# 2. 如果缺少 JDK/Maven，选择自动安装
# 3. 脚本会自动配置数据库和开发环境
# 4. IntelliJ IDEA 会自动打开后端项目
```

<!-- tab:WSL2 用户 -->
```bash
# 1. 启动完整开发环境
sudo ./dev.sh -b

# 2. 脚本会自动配置 SSH 密钥
# 3. 可以使用 IntelliJ IDEA 远程开发
```
<!-- tabs:end -->

### 常见问题快速解决

#### 问题：端口被占用
```bash
# 查找占用端口的进程
sudo netstat -tlnp | grep :8000

# 终止进程
sudo kill -9 <进程ID>

# 或使用脚本清理
sudo ./dev.sh -c
```

#### 问题：权限不足
```bash
# 设置脚本权限
chmod +x dev.sh

# 使用 sudo 运行
sudo ./dev.sh -f

# 或设置文件所有者
sudo ./dev.sh -o
```

#### 问题：Docker 未启动
```bash
# 启动 Docker Desktop
# 或重启 WSL2
wsl --shutdown
wsl
```

#### 问题：依赖安装失败
```bash
# 清理缓存
yarn cache clean

# 删除 node_modules
rm -rf node_modules

# 重新安装
yarn install
```

### 开发建议

#### 新手推荐流程
1. **首次使用**：选择最简单的模式（前端远程调试）
2. **熟悉项目**：使用前端本地调试模式
3. **全栈开发**：使用前后端本地调试模式

#### 性能优化建议
- **WSL2 用户**：将项目放在 Linux 文件系统中
- **Windows 用户**：使用 SSD 硬盘，增加 Docker 内存分配
- **所有用户**：定期清理开发环境

### 相关文档

- [Windows 环境开发指南](windows-dev-guide.md)
- [WSL2 环境开发指南](wsl2-dev-guide.md)
- [调试后端代码](debugBackendCode.md)
