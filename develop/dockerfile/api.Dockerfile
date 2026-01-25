#设置继承镜像
FROM maven:3.9.5-eclipse-temurin-17-focal

LABEL author="moushengkoo"

# COPY ./server/api-service/ /app/api-service
COPY ./run.sh /run.sh

RUN echo "\
    deb http://mirrors.163.com/ubuntu/ focal main restricted universe multiverse\n\
    deb http://mirrors.163.com/ubuntu/ focal-security main restricted universe multiverse\n\
    deb http://mirrors.163.com/ubuntu/ focal-updates main restricted universe multiverse\n\
    deb http://mirrors.163.com/ubuntu/ focal-proposed main restricted universe multiverse\n\
    deb http://mirrors.163.com/ubuntu/ focal-backports main restricted universe multiverse\n\
    deb-src http://mirrors.163.com/ubuntu/ focal main restricted universe multiverse\n\
    deb-src http://mirrors.163.com/ubuntu/ focal-security main restricted universe multiverse\n\
    deb-src http://mirrors.163.com/ubuntu/ focal-updates main restricted universe multiverse\n\
    deb-src http://mirrors.163.com/ubuntu/ focal-proposed main restricted universe multiverse\n\
    deb-src http://mirrors.163.com/ubuntu/ focal-backports main restricted universe multiverse\
    " >/etc/apt/sources.list && \
    apt-get update && \
    #安装ssh服务、jdk17、maven
    apt-get install -y openssh-server git libssl1.1 && \
    apt-get clean && \
    mkdir -p /var/run/sshd && \
    mkdir -p /root/.ssh && \
    touch /root/.ssh/authorized_keys && \
    #取消pam限制
    sed -ri 's/session    required     pam_loginuid.so/#session    required     pam_loginuid.so/g' /etc/pam.d/sshd && \
    sed -i 's/^#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/^#PermitUserEnvironment no/PermitUserEnvironment yes/' /etc/ssh/sshd_config && \
    #复制配置文件到相应位置，并赋予脚本可执行权限
    chmod 755 /run.sh && \
    # 设置root密码
    echo 'root:barda' | chpasswd

#开放端口
EXPOSE 22

#设置自启动命令
CMD ["/run.sh"]
