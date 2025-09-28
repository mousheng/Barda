#! /bin/bash

# 检查rjson库是否发布到本地仓库
if [ ! -e /root/.m2/repository/tv/twelvetone/rjson/rjson/maven-metadata-local.xml ]; then
    echo "maven本地仓库未安装rjson，正在准备安装"
    if [ ! -e /barda/server/api-service/lib/rjson*.jar ]; then
        echo "本地未找到rjson离线包，请确定是否正确挂在api-server目录"
    else
        mvn install:install-file \
            -Dfile=/barda/server/api-service/lib/rjson-1.3.1-20210724.182155-1.jar \
            -DgroupId=tv.twelvetone.rjson \
            -DartifactId=rjson \
            -Dpackaging=jar \
            -Dversion=1.3.1-SNAPSHOT
    fi
fi

# 保存docker中的环境变量，确保在idea中能正确读取
env > /root/.ssh/environment

# 设置barda目录为信任目录
git config --global --add safe.directory /barda

/usr/sbin/sshd -D
