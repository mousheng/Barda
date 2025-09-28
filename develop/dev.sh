#!/bin/bash

# 获取原始用户
original_user=$(echo $SUDO_USER)

# 获取原始用户的组名
original_group=$(id -gn $original_user)

# 设置镜像内部和主机相同的路径
export BARDA_CLIENT_WORKDIR=$(realpath ..)

# 设置当前用户和用户组
# export IDGID="${original_user}:${original_group}"

# 获取原始用户的主目录
user_home=$(getent passwd $original_user | cut -d: -f6)

GREEN='\033[0;32m'
RED='\e[31m'
NC='\033[0m' # No Color

# 显示帮助信息
show_help() {
    echo
    echo -e "${GREEN}描述:${NC}"
    echo -e "      本脚本用于快速调试Barda前后端"
    echo
    echo -e "${GREEN}语法:${NC} sudo ./dev.sh [选项]"
    echo -e "\n${GREEN}选项:${NC}"
    echo -e "${GREEN}  -f${NC}    仅调试前端"
    echo -e "${GREEN}  -rf${NC}   先删除容器，再调试前端"
    echo -e "${GREEN}  -dc${NC}   调试组件"
    echo -e "${GREEN}  -cf${NC}   自定义调试前端"
    echo -e "${GREEN}  -b${NC}    调试前后端"
    echo -e "${GREEN}  -rb${NC}   先删除容器，再调试前后端"
    echo -e "${GREEN}  -c${NC}    清理：删除容器，${RED}并清空temp文件夹下所有内容！"
    echo -e "${GREEN}  -p${NC}    构建镜像"
    echo -e "${GREEN}  -o${NC}    设置当前文件夹所有者为当前用户"
    echo
    exit 1
}

# 进入容器内部
enter_client() {
    docker-compose exec barda-client sh -c "$1"
    echo 您可使用以下命令重新开始调试：docker-compose exec barda-client sh -c \"$1\"
}

# 调试前端
dev_front() {
    echo "正在调试前端"
    docker-compose --profile onlyFrontDevelop --env-file ./env/frontDev.env up -d
    enter_client "yarn && yarn start"
}

# 调试组件
dev_component() {
    echo "正在调试组件"
    docker-compose --profile onlyFrontDevelop --env-file ./env/frontDev.env up -d
    enter_client "yarn && yarn build:sdk && yarn start:comps"
}

# 自定义调试前端
custom_front() {
    read -p "请输入自定义启动命令 (默认: yarn start): " command
    command=${command:-yarn install && yarn start}
    docker-compose --profile onlyFrontDevelop --env-file ./env/frontDev.env up -d
    enter_client "yarn install && $command"
}

# 调试前后端
dev_frontAndBack() {
    echo "正在调试前后端"
    docker-compose --profile DevelopFrontAndBack --env-file ./env/frontBackDev.env up -d
    docker-compose exec barda-client sh -c "cd packages/barda && yarn && yarn start"
    enter_client "yarn install && yarn start"
}

# 删除容器
del_container() {
    echo "正在删除容器"
    docker-compose --profile onlyFrontDevelop --env-file ./env/frontDev.env down
    docker-compose --profile DevelopFrontAndBack --env-file ./env/frontBackDev.env down
}

# 删除前端镜像
del_frontImage() {
    echo "正在删除前端镜像"
    docker rmi -f barda-client:development
}

# 打开网页
open_browser() {
    if [ -e /mnt/c/Windows/explorer.exe ]; then
        echo "正在打开网页"
        /mnt/c/Windows/explorer.exe http://localhost:$1
    else
        echo "请手动打开浏览器并访问 http://localhost:$1"
    fi
}

# 检查秘钥对是否存在，如果不存在则创建，如果存在，则复制写入到authorized_keys
check_ssh_authorized_keys() {
    windows_username=$(/mnt/c/Windows/System32/cmd.exe /c 'echo %USERNAME%' | tr -d '\r\n')
    windows_ssh_path="/mnt/c/Users/$windows_username/.ssh"
    if [ -e "$windows_ssh_path/id_rsa.pub" ]; then
        echo "Windows的SSH公钥已存在，正在复制到authorized_keys"
        mkdir -p ./.temp
        cat "$windows_ssh_path/id_rsa.pub" >./.temp/authorized_keys
    else
        echo "正在在Windows目录中生成SSH密钥对"
        mkdir -p "$windows_ssh_path"
        ssh-keygen -t rsa -f "$windows_ssh_path/id_rsa" -N ""
        chmod 700 "$windows_ssh_path"
        chmod 600 "$windows_ssh_path/id_rsa"
        chmod 644 "$windows_ssh_path/id_rsa.pub"
        
        echo "正在复制到容器authorized_keys"
        mkdir -p ./.temp
        cat "$windows_ssh_path/id_rsa.pub" >./.temp/authorized_keys
    fi
    echo -e "\n${GREEN}密钥配置完成，您可以使用IDEA的SSH远程开发功能连接127.0.0.1:2222，按任意键继续运行前端开发容器...${NC}"
    read -n 1 -s
}

# 清除缓存
clear() {
    echo -e "\n${RED}temp文件夹包含您测试时生成的App等数据，你确定要删除temp文件夹吗？(yes/no) [no]: ${NC}\c"
    read choice

    # 检查用户输入
    if [[ "$choice" == "yes" || "$choice" == "y" ]]; then
        echo "正在删除文件..."
        rm -rf ./.temp
    else
        echo "操作已取消。"
    fi

    echo -e "\n${RED}你确定要删除依赖包缓存吗？(yes/no) [no]: ${NC}\c"
    read choice2
    # 检查用户输入
    if [[ "$choice2" == "yes" || "$choice2" == "y" ]]; then
        echo "正在删除node_modules文件夹..."
        find ../client/ -type d -name "node_modules" -exec rm -rf {} +
        echo "正在删除yarn缓存"
        rm -rf ../client/.yarn/cache
        
    else
        echo "操作已取消。"
    fi

    echo -e "是否清理docker缓存?"
    docker builder prune
}
# 生成barda多合一镜像
publish_image() {
    read -p "请输入镜像的名称 (默认: moushengkoo/barda): " name
    name=${name:-moushengkoo/barda}
    # 输入镜像标签，默认值：latest
    read -p "请输入镜像的标签 (默认: latest): " tag
    tag=${tag:-latest}
    DOCKER_BUILDKIT=1 docker build -f ../deploy/docker/Dockerfile -t "${name}:$tag" ..

}

# 设置当前文件夹的所有者为当前用户
set_owner() {
    echo "正在设置当前文件夹的所有者为 $original_user"
    find ../client/ -type d -name "node_modules" -exec chown -R $original_user:$original_user {} +
}

# 检查是否有输入参数
if [ $# -eq 0 ]; then
    show_help
fi

# 根据参数执行相应命令
case "$1" in
-f)
    open_browser 30000
    open_browser 8000
    dev_front
    ;;
-cf)
    custom_front
    ;;
-dc)
    open_browser 30000
    open_browser 9000
    dev_component
    ;;
-rf)
    del_container
    open_browser 30000
    open_browser 8000
    dev_front
    ;;
-b)
    open_browser 30000
    open_browser 8000
    check_ssh_authorized_keys
    dev_frontAndBack
    ;;
-rb)
    del_container
    open_browser 30000
    open_browser 8000
    dev_frontAndBack
    ;;
-c)
    del_container
    del_frontImage
    clear
    ;;
-p)
    publish_image
    ;;
-o)
    set_owner
    ;;
*)
    echo "无效的参数: $1"
    show_help
    ;;
esac
