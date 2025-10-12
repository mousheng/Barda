$ErrorActionPreference = "Stop"

# region 彩色输出与通用工具 -------------------------------------------------
function Write-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Yellow
}

function Write-ErrorLine {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}

function Pause-IfNeeded {
    param([string]$Message = "按任意键继续...")
    Write-Host $Message -ForegroundColor DarkGray
    $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
}

function Test-Port {
    param(
        [Parameter(Mandatory=$true)][string]$HostName,
        [Parameter(Mandatory=$true)][int]$Port,
        [int]$TimeoutMs = 1000
    )
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $iar.AsyncWaitHandle.WaitOne($TimeoutMs, $false)) {
            $client.Close()
            return $false
        }
        $client.EndConnect($iar)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}
# endregion -------------------------------------------------------------------

# region Java / Maven 检测辅助 ------------------------------------------------
function Test-JavaAvailable {
    try {
        if ($env:JAVA_HOME) {
            $javaFromHome = Join-Path $env:JAVA_HOME "bin\java.exe"
            if (Test-Path $javaFromHome) { return $true }
        }
        $cmd = Get-Command java -ErrorAction SilentlyContinue
        if ($cmd) { return $true }
        & where.exe java *> $null
        if ($LASTEXITCODE -eq 0) { return $true }
    } catch {}
    return $false
}

function Test-MavenAvailable {
    try {
        $cmd = Get-Command mvn -ErrorAction SilentlyContinue
        if ($cmd) { return $true }
        & where.exe mvn *> $null
        if ($LASTEXITCODE -eq 0) { return $true }
    } catch {}
    return $false
}

function Add-CommonJavaMavenPath {
    # 尝试注入常见安装路径到当前会话 PATH（不改系统环境变量）
    $pathsToTry = @()
    
    # Java 路径
    $pathsToTry += (Get-ChildItem -Path "C:\Program Files\Microsoft" -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*jdk*' } | ForEach-Object { Join-Path $_.FullName "bin" })
    
    # 用户目录下的 Maven 路径
    $userPrograms = Join-Path $env:USERPROFILE "Programs"
    if (Test-Path $userPrograms) {
        $pathsToTry += (Get-ChildItem -Path $userPrograms -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like 'apache-maven*' } | ForEach-Object { Join-Path $_.FullName "bin" })
    }

    foreach ($p in $pathsToTry) {
        if ($p -and (Test-Path $p) -and (-not ($env:Path -split ';' | Where-Object { $_ -eq $p }))) {
            $env:Path = "$env:Path;$p"
        }
    }
}
# endregion -------------------------------------------------------------------

# region Maven 本地仓库解析 ---------------------------------------------------
function Get-MavenLocalRepoPath {
    try {
        $output = mvn -N help:evaluate -Dexpression=settings.localRepository -q -DforceStdout 2>$null
        if ($LASTEXITCODE -eq 0 -and $output -and ($output -notmatch 'null')) {
            return $output.Trim()
        }
    } catch {}
    # 回退到默认
    return (Join-Path $env:USERPROFILE ".m2\repository")
}
# endregion -------------------------------------------------------------------

# region IDEA 查找与启动 -------------------------------------------------------
function Get-IntelliJIdeaPath {
    try {
        $searchBases = @()
        $searchBases += "C:\\Program Files\\JetBrains"
        if ($env:LOCALAPPDATA) {
            $searchBases += (Join-Path $env:LOCALAPPDATA "JetBrains\\Toolbox\\apps\\IDEA-U")
            $searchBases += (Join-Path $env:LOCALAPPDATA "JetBrains\\Toolbox\\apps\\IDEA-C")
        }

        foreach ($base in $searchBases) {
            if (-not (Test-Path $base)) { continue }
            $exe = Get-ChildItem -Path $base -Recurse -Filter "idea64.exe" -ErrorAction SilentlyContinue |
                Sort-Object LastWriteTime -Descending |
                Select-Object -First 1
            if ($exe) { return $exe.FullName }
        }

        # 在其他盘符中查找常见安装位置与 Toolbox 目录
        $fsDrives = (Get-PSDrive -PSProvider FileSystem | Select-Object -ExpandProperty Root)
        foreach ($root in $fsDrives) {
            try {
                $candidates = @()
                $candidates += (Join-Path $root "Program Files\\JetBrains")
                $candidates += (Join-Path $root "Program Files (x86)\\JetBrains")
                $candidates += (Join-Path $root "JetBrains")

                foreach ($cand in $candidates) {
                    if (-not (Test-Path $cand)) { continue }
                    $exe = Get-ChildItem -Path $cand -Recurse -Filter "idea64.exe" -ErrorAction SilentlyContinue |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1
                    if ($exe) { return $exe.FullName }
                }

                # 尝试用户目录下的 Toolbox 路径
                $usersDir = Join-Path $root "Users"
                if (Test-Path $usersDir) {
                    $userHomes = Get-ChildItem -Path $usersDir -Directory -ErrorAction SilentlyContinue
                    foreach ($uh in $userHomes) {
                        $tbU = Join-Path $uh.FullName "AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-U"
                        $tbC = Join-Path $uh.FullName "AppData\\Local\\JetBrains\\Toolbox\\apps\\IDEA-C"
                        foreach ($tb in @($tbU, $tbC)) {
                            if (-not (Test-Path $tb)) { continue }
                            $exe = Get-ChildItem -Path $tb -Recurse -Filter "idea64.exe" -ErrorAction SilentlyContinue |
                                Sort-Object LastWriteTime -Descending |
                                Select-Object -First 1
                            if ($exe) { return $exe.FullName }
                        }
                    }
                }
            } catch {}
        }

        # 兜底：PATH 中可直接调用 idea64.exe
        try {
            & where.exe idea64.exe *> $null
            if ($LASTEXITCODE -eq 0) { return "idea64.exe" }
        } catch {}
    } catch {}
    return $null
}

function Open-In-IntelliJ {
    param([Parameter(Mandatory=$true)][string]$PathToOpen)
    $ideaExe = Get-IntelliJIdeaPath
    if ($ideaExe) {
        Write-Info "启动 IntelliJ IDEA 打开: $PathToOpen"
        Start-Process -FilePath $ideaExe -ArgumentList "`"$PathToOpen`"" | Out-Null
        return $true
    } else {
        Write-Warn "未找到 IntelliJ IDEA，可手动打开 IDE 并加载该目录。"
        return $false
    }
}
# endregion -------------------------------------------------------------------

# region Maven 手动安装函数 ---------------------------------------------------
function Install-MavenManually {
    try {
        Write-Info "开始手动安装 Maven 3.9.10..."
        
        # 检查管理员权限
        $isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")
        if (-not $isAdmin) {
            Write-Warn "需要管理员权限来安装到 C:\Program Files，尝试使用用户目录..."
            $installDir = Join-Path $env:USERPROFILE "Programs"
            $mavenTargetDir = Join-Path $installDir "apache-maven-3.9.10"
        } else {
            $installDir = "C:\Program Files"
            $mavenTargetDir = Join-Path $installDir "apache-maven-3.9.10"
        }
        
        # 创建临时目录
        $tempDir = [System.IO.Path]::GetTempPath()
        $mavenZip = Join-Path $tempDir "apache-maven-3.9.10-bin.zip"
        $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.zip"
        
        # 下载 Maven
        Write-Info "下载 Maven 3.9.10..."
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
        
        # 确保安装目录存在
        if (-not (Test-Path $installDir)) {
            New-Item -ItemType Directory -Path $installDir -Force | Out-Null
        }
        
        Write-Info "解压 Maven 到: $mavenTargetDir"
        Expand-Archive -Path $mavenZip -DestinationPath $installDir -Force
        
        # 设置环境变量
        Write-Info "配置 Maven 环境变量..."
        
        # 设置 MAVEN_HOME
        if ($isAdmin) {
            [Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenTargetDir, "Machine")
        } else {
            [Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenTargetDir, "User")
        }
        $env:MAVEN_HOME = $mavenTargetDir
        
        # 更新 PATH
        $mavenBinPath = Join-Path $mavenTargetDir "bin"
        if ($isAdmin) {
            $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
            if ($currentPath -notlike "*$mavenBinPath*") {
                $newPath = "$currentPath;$mavenBinPath"
                [Environment]::SetEnvironmentVariable("PATH", $newPath, "Machine")
            }
        } else {
            $currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
            if ($currentPath -notlike "*$mavenBinPath*") {
                $newPath = "$currentPath;$mavenBinPath"
                [Environment]::SetEnvironmentVariable("PATH", $newPath, "User")
            }
        }
        $env:Path = "$env:Path;$mavenBinPath"
        
        # 清理临时文件
        Remove-Item $mavenZip -Force -ErrorAction SilentlyContinue
        
        Write-Success "Maven 3.9.10 安装完成！"
        Write-Info "MAVEN_HOME: $mavenTargetDir"
        Write-Info "Maven bin 已添加到 PATH"
        
    } catch {
        Write-ErrorLine "手动安装 Maven 失败: $($_.Exception.Message)"
        throw
    }
}
# endregion -------------------------------------------------------------------

# region 项目路径解析 ---------------------------------------------------------
Write-Host ""; Write-Host "=== Barda Windows 开发调试脚本 (dev.ps1) ===" -ForegroundColor Magenta

$PROJECT_ROOT = (Resolve-Path "$PSScriptRoot/..\").Path
$CLIENT_DIR = Join-Path $PROJECT_ROOT "client"
$API_SERVICE_DIR = Join-Path $PROJECT_ROOT "server\api-service"
$BARDA_CLIENT_DIR = Join-Path $CLIENT_DIR "packages\barda"

# endregion -------------------------------------------------------------------

# region 环境检查 -------------------------------------------------------------
$hasNode = $false
try { $ver = node -v 2>$null; if ($LASTEXITCODE -eq 0 -and $ver) { $hasNode = $true } } catch {}

if (-not (Test-Path $BARDA_CLIENT_DIR)) {
    Write-ErrorLine "未找到 barda-client 目录。期望路径: $BARDA_CLIENT_DIR"
    Pause-IfNeeded "请检查目录结构后按任意键退出..."
    exit 1
}

$hasDocker = $false
try { docker --version *> $null; if ($LASTEXITCODE -eq 0) { $hasDocker = $true } } catch {}

if (-not $hasNode) {
    Write-Warn "未检测到 Node.js，选项1以外的依赖将不可用。"
}

if (-not $hasDocker) {
    Write-Warn "未检测到 Docker，选项2和3将不可用。"
}
# endregion -------------------------------------------------------------------

# region 菜单 -----------------------------------------------------------------
function Show-Menu {
    param([bool]$Enable2, [bool]$Enable3)
    Write-Host ""; Write-Host "请选择开发模式:" -ForegroundColor White
    Write-Host "  1) 前端远程调试" -ForegroundColor White
    if ($Enable2) { Write-Host "  2) 前端本地调试" -ForegroundColor White } else { Write-Host "  2) 前端本地调试 (不可用)" -ForegroundColor DarkGray }
    if ($Enable3) { Write-Host "  3) 前后端本地调试" -ForegroundColor White } else { Write-Host "  3) 前后端本地调试 (不可用)" -ForegroundColor DarkGray }
    Write-Host "  q) 退出" -ForegroundColor White
    $choice = Read-Host "输入选项编号"
    return $choice
}

$enable2 = $hasDocker
$enable3 = $hasDocker
$choice = Show-Menu -Enable2:$enable2 -Enable3:$enable3
if ($choice -eq 'q') { exit 0 }
# endregion -------------------------------------------------------------------

# region 模式实现 -------------------------------------------------------------
$originalLocation = Get-Location

function Ensure-YarnInClient {
    param([string]$ClientDir)
    if (-not (Test-Path $ClientDir)) { throw "client 目录不存在: $ClientDir" }
    Set-Location $ClientDir
    Write-Info "安装前端依赖: yarn install"
    yarn install
}

function Start-Frontend {
    Write-Info "启动前端: yarn start"
    yarn start
}

switch ($choice) {
    '1' {
        try {
            Write-Host ""; Write-Success "进入模式1：前端远程调试"
            $env:BARDA_API_SERVICE_URL = "http://43.133.22.234:30000"
            Ensure-YarnInClient -ClientDir $CLIENT_DIR
            Start-Frontend
        } catch {
            Write-ErrorLine "模式1执行失败: $($_.Exception.Message)"
            throw
        } finally {
            Set-Location $originalLocation
        }
    }
    '2' {
        if (-not $enable2) { Write-ErrorLine "Docker 不可用，无法执行模式2。"; exit 1 }
        try {
            Write-Host ""; Write-Success "进入模式2：前端本地调试"
            $containerName = "barda-client"
            $mountHost = Join-Path $PROJECT_ROOT "develop\.temp\barda-stacks"
            if (-not (Test-Path $mountHost)) { New-Item -ItemType Directory -Force -Path $mountHost | Out-Null }

            $existing = (docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $containerName })
            if (-not $existing) {
                Write-Info "创建后端容器: $containerName"
                docker run -d --name $containerName -p 3000:3000 --mount type=bind,source="${mountHost}",target=/barda-stacks --restart unless-stopped moushengkoo/barda:latest *> $null
            } else {
                $running = (docker ps --format '{{.Names}}' | Where-Object { $_ -eq $containerName })
                if (-not $running) {
                    Write-Info "启动已存在但未运行的容器: $containerName"
                    docker start $containerName *> $null
                } else {
                    Write-Info "容器已在运行: $containerName"
                }
            }

            $env:BARDA_API_SERVICE_URL = "http://127.0.0.1:3000"
            Ensure-YarnInClient -ClientDir $CLIENT_DIR
            Start-Frontend
        } catch {
            Write-ErrorLine "模式2执行失败: $($_.Exception.Message)"
            throw
        } finally {
            Set-Location $originalLocation
        }
    }
    '3' {
        if (-not $enable3) { Write-ErrorLine "Docker 不可用，无法执行模式3。"; exit 1 }
        try {
            Write-Host ""; Write-Success "进入模式3：前后端本地调试"

            # 检查 Java / Maven（若缺失，提供自动安装或建议切换模式2）
            $hasJava = Test-JavaAvailable
            $hasMaven = Test-MavenAvailable
            if (-not $hasJava -or -not $hasMaven) {
                Write-Warn "未检测到 JDK 或 Maven。JDK17 与 Maven 为本地编译后端所必需。"
                $opt = Read-Host "是否自动安装? (y=安装 / n=取消 / s=切换模式2)"
                if ([string]::IsNullOrWhiteSpace($opt)) { $opt = 'y' }
                if ($opt -match '^(s|S)$') {
                    Write-Warn "建议使用模式2：前端本地 + 后端Docker。"
                    throw "已取消模式3，改用其他模式。"
                } elseif ($opt -match '^(y|Y)$') {
                    try {
                        Write-Info "安装 JDK 17（Eclipse Temurin）..."
                        winget install --id EclipseAdoptium.Temurin.17.JDK -e --accept-source-agreements --accept-package-agreements
                    } catch {}
                    # 安装 Maven
                    try {
                        Write-Info "安装 Maven 3.9.10..."
                        Install-MavenManually
                    } catch {
                        Write-ErrorLine "安装 Maven 失败: $($_.Exception.Message)"
                    }

                    # 注入常见安装路径并复检
                    Add-CommonJavaMavenPath
                    $hasJava = Test-JavaAvailable
                    $hasMaven = Test-MavenAvailable
                    if (-not $hasJava -or -not $hasMaven) {
                        Write-ErrorLine "自动安装未成功，请手动安装 JDK17 与 Maven 后再重试，或改用模式2。"
                        throw "缺少 JDK/Maven"
                    }
                } else {
                    throw "缺少 JDK/Maven"
                }
            }

            # 检查 MongoDB
            $mongoUp = (Get-Process -Name mongod -ErrorAction SilentlyContinue) -or (Test-Port -HostName '127.0.0.1' -Port 27017)
            if (-not $mongoUp) {
                $startMongo = Read-Host "未检测到 MongoDB，是否用 Docker 启动 mongo:latest? (y/n)"
                if ([string]::IsNullOrWhiteSpace($startMongo)) { $startMongo = 'y' }
                if ($startMongo -match '^(y|Y)$') {
                    $mongoContainerName = "barda-mongodb"
                    $mongoExisting = (docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $mongoContainerName })
                    if (-not $mongoExisting) {
                        Write-Info "创建 MongoDB 容器: $mongoContainerName"
                        docker run -d --name $mongoContainerName -p 27017:27017 mongo:latest *> $null
                    } else {
                        $mongoRunning = (docker ps --format '{{.Names}}' | Where-Object { $_ -eq $mongoContainerName })
                        if (-not $mongoRunning) {
                            Write-Info "启动已存在但未运行的 MongoDB 容器: $mongoContainerName"
                            docker start $mongoContainerName *> $null
                        } else {
                            Write-Info "MongoDB 容器已在运行: $mongoContainerName"
                        }
                    }
                }
            }

            # 检查 Redis
            $redisUp = (Get-Process -Name redis-server -ErrorAction SilentlyContinue) -or (Test-Port -HostName '127.0.0.1' -Port 6379)
            if (-not $redisUp) {
                $startRedis = Read-Host "未检测到 Redis，是否用 Docker 启动 redis:latest? (y/n)"
                if ([string]::IsNullOrWhiteSpace($startRedis)) { $startRedis = 'y' }
                if ($startRedis -match '^(y|Y)$') {
                    $redisContainerName = "barda-redis"
                    $redisExisting = (docker ps -a --format '{{.Names}}' | Where-Object { $_ -eq $redisContainerName })
                    if (-not $redisExisting) {
                        Write-Info "创建 Redis 容器: $redisContainerName"
                        docker run -d --name $redisContainerName -p 6379:6379 redis:latest *> $null
                    } else {
                        $redisRunning = (docker ps --format '{{.Names}}' | Where-Object { $_ -eq $redisContainerName })
                        if (-not $redisRunning) {
                            Write-Info "启动已存在但未运行的 Redis 容器: $redisContainerName"
                            docker start $redisContainerName *> $null
                        } else {
                            Write-Info "Redis 容器已在运行: $redisContainerName"
                        }
                    }
                }
            }

            # rjson 安装
            $rjsonVersion = "1.3.1-SNAPSHOT"
            $localRepo = Get-MavenLocalRepoPath
            $rjsonLocalPath = Join-Path $localRepo ("tv\twelvetone\rjson\rjson\$rjsonVersion\rjson-$rjsonVersion.jar")

            # 优先候选路径
            $candidates = @()
            $candidates += (Join-Path $API_SERVICE_DIR "lib\rjson-1.3.1-20210724.182155-1.jar")
            $candidates += (Join-Path $API_SERVICE_DIR "barda-server\lib\rjson-1.3.1-20210724.182155-1.jar")
            # 兜底：在 api-service 下递归查找 rjson-1.3.1-*.jar
            if (-not ($candidates | Where-Object { Test-Path $_ })) {
                $found = Get-ChildItem -Path $API_SERVICE_DIR -Recurse -Filter "rjson-1.3.1-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
                if ($found) { $candidates += $found.FullName }
            }

            if (-not (Test-Path $rjsonLocalPath)) {
                $rjsonJarPath = ($candidates | Where-Object { Test-Path $_ } | Select-Object -First 1)
                echo $rjsonJarPath
                if (-not $rjsonJarPath) { throw "未找到 rjson 源 jar，请确认位于 server\\api-service\\lib\\rjson-1.3.1-20210724.182155-1.jar" }
                Write-Info "安装 rjson 到本地 Maven 仓库: $rjsonJarPath -> $localRepo"
                mvn install:install-file -Dfile="${rjsonJarPath}" -DgroupId="tv.twelvetone.rjson" -DartifactId="rjson" -Dversion="$rjsonVersion" -Dpackaging=jar
            } else {
                Write-Info "已检测到本地 rjson: $rjsonLocalPath"
            }

            # 构建后端
            $serverDir = Join-Path $API_SERVICE_DIR "barda-server"
            $targetDir = Join-Path $serverDir "target"
            $needBuild = $true
            if (Test-Path $targetDir) {
                $rebuild = Read-Host "检测到已有构建产物，是否重新构建? (y/n)"
                if ([string]::IsNullOrWhiteSpace($rebuild)) { $rebuild = 'y' }
                if ($rebuild -match '^(n|N)$') { $needBuild = $false }
            }
            if ($needBuild) {
                # 在 api-service 根目录编译并安装相关模块，解决子模块 SNAPSHOT 缺失
                $apiServiceRoot = $API_SERVICE_DIR
                Set-Location $apiServiceRoot
                mvn -pl barda-server -am clean install -DskipTests
            }

            # 构建完成后，尝试自动用 IntelliJ 打开后端工程目录
            $openedIdea = Open-In-IntelliJ -PathToOpen $API_SERVICE_DIR

            if ($openedIdea) {
                Pause-IfNeeded "已尝试打开 IntelliJ IDEA（server/api-service）。按任意键继续启动前端..."
            } else {
                Pause-IfNeeded "未检测到 IntelliJ IDEA，请手动在 IDE 中打开目录：$API_SERVICE_DIR。按任意键继续启动前端..."
            }

            $env:BARDA_API_SERVICE_URL = "http://127.0.0.1:8080"
            Ensure-YarnInClient -ClientDir $CLIENT_DIR
            Start-Frontend
        } catch {
            Write-ErrorLine "模式3执行失败: $($_.Exception.Message)"
            throw
        } finally {
            Set-Location $originalLocation
        }
    }
    Default {
        Write-Warn "无效选项：$choice"
    }
}
# endregion -------------------------------------------------------------------


