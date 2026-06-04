@echo off
chcp 65001 > nul
echo ========================================
echo   API Platform 一键打包脚本
echo ========================================
echo.

REM 检查是否安装了 Maven
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [错误] 未找到 Maven，请先安装 Maven
    echo 下载地址: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

echo [1/3] 正在清理之前的构建...
call mvn clean -q
echo.

echo [2/3] 正在构建项目（包含前端）...
call mvn package -DskipTests
echo.

if %ERRORLEVEL% neq 0 (
    echo [错误] 构建失败，请检查错误信息
    pause
    exit /b 1
)

echo [3/3] 构建成功！
echo.

REM 查找生成的 JAR 文件
for /f "delims=" %%i in ('dir /b /o-n "%SCRIPT_DIR%backend\target\*.jar" 2^>nul ^| findstr /v "original"') do (
    set JAR_FILE=%%i
    goto :found
)

:found
if defined JAR_FILE (
    echo 生成的文件: backend\target\%JAR_FILE%
    echo 文件大小: 
    powershell -command "(Get-Item '%SCRIPT_DIR%backend\target\%JAR_FILE%').Length / 1MB | ForEach-Object { '{0:N2} MB' -f $_ }"
) else (
    echo [警告] 未找到生成的 JAR 文件
)

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo 启动方式:
echo   java -jar backend\target\api-platform-backend-1.0.0.jar
echo.
echo 注意事项:
echo   1. 确保 MySQL 和 Redis 已启动
echo   2. 首次运行会自动初始化数据库
echo   3. 访问地址: http://localhost:8080/api
echo.
pause
