
当你对Barda做出了修改，你可以使用Docker打包成镜像，首先切换到**项目根目录**，然后输入以下命令
## 打包镜像
```bash
DOCKER_BUILDKIT=1 docker build -f deploy/docker/Dockerfile -t moushengkoo/barda:v1 .
```

!> 最后的`.`是必要的，不能省略

?> `moushengkoo/barda:v1` 可以设置成你自己便于记忆的tag

!> 打包镜像根据你的网速和电脑性能可能需要较长时间，**有些地址可能会访问失败**。
