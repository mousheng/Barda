#!/bin/bash

# 初始化config目录脚本
# 确保barda-stacks/config目录存在

set -e

CONFIG_DIR="/barda-stacks/config"

echo "初始化config目录..."

# 创建config目录（如果不存在）
if [ ! -d "$CONFIG_DIR" ]; then
    mkdir -p "$CONFIG_DIR"
    echo "创建config目录: $CONFIG_DIR"
fi

# 设置基本权限
chmod 755 "$CONFIG_DIR"

echo "config目录初始化完成: $CONFIG_DIR"
