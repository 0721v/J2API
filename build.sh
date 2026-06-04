#!/bin/bash
set -e

echo "========================================"
echo "  API Platform 一键打包脚本"
echo "========================================"
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 检查是否安装了 Maven
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未找到 Maven，请先安装 Maven"
    echo "下载地址: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "[1/3] 正在清理之前的构建..."
mvn clean -q
echo ""

echo "[2/3] 正在构建项目（包含前端）..."
mvn package -DskipTests
echo ""

if [ $? -ne 0 ]; then
    echo "[错误] 构建失败，请检查错误信息"
    exit 1
fi

echo "[3/3] 构建成功！"
echo ""

# 查找生成的 JAR 文件
JAR_FILE=$(ls -t backend/target/*.jar 2>/dev/null | grep -v "original" | head -1)

if [ -n "$JAR_FILE" ]; then
    echo "生成的文件: $JAR_FILE"
    echo "文件大小:"
    ls -lh "$JAR_FILE" | awk '{print $5}'
else
    echo "[警告] 未找到生成的 JAR 文件"
fi

echo ""
echo "========================================"
echo "  打包完成！"
echo "========================================"
echo ""
echo "启动方式:"
echo "  java -jar backend/target/api-platform-backend-1.0.0.jar"
echo ""
echo "注意事项:"
echo "  1. 确保 MySQL 和 Redis 已启动"
echo "  2. 首次运行会自动初始化数据库"
echo "  3. 访问地址: http://localhost:8080/api"
echo ""
