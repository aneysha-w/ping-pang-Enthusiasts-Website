@echo off
chcp 65001 >nul
echo ==========================================
echo   乒乓球交流平台 - 一键安装并启动
echo ==========================================
echo.

echo [1/5] 检查并安装 Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo 未检测到 Node.js，正在安装...
    winget install OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements
    echo Node.js 安装完成，请关闭此窗口重新运行本脚本
    pause
    exit /b 0
) else (
    for /f "tokens=*" %%i in ('node -v') do echo Node.js 版本: %%i
)

echo.
echo [2/5] 检查并安装 JDK 17...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo 未检测到 Java，正在安装 JDK 17...
    winget install Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements
    echo JDK 安装完成，请关闭此窗口重新运行本脚本
    pause
    exit /b 0
) else (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo Java: %%i
)

echo.
echo [3/5] 检查并安装 Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo 未检测到 Maven，正在安装...
    winget install Apache.Maven --accept-source-agreements --accept-package-agreements
    if %errorlevel% neq 0 (
        echo.
        echo winget 安装 Maven 失败，尝试手动下载...
        echo 请手动安装 Maven: https://maven.apache.org/download.cgi
        echo 或使用 IDE（IntelliJ IDEA）打开 backend 目录直接运行
        pause
        exit /b 1
    )
    echo Maven 安装完成，请关闭此窗口重新运行本脚本
    pause
    exit /b 0
) else (
    for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do echo %%i
)

echo.
echo [4/5] 安装前端依赖...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    echo 正在执行 npm install...
    call npm install
    if %errorlevel% neq 0 (
        echo 前端依赖安装失败！
        pause
        exit /b 1
    )
)
echo 前端依赖已就绪

echo.
echo [5/5] 启动服务...
echo.
echo 启动后端服务（端口 8080）...
start "PingPong Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo 等待后端启动（20秒）...
timeout /t 20 /nobreak >nul

echo 启动前端服务（端口 5173）...
start "PingPong Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

timeout /t 8 /nobreak >nul

echo.
echo ==========================================
echo   安装并启动完成！
echo ==========================================
echo.
echo  球友前台:  http://localhost:5173
echo  管理后台:  http://localhost:5173/admin/login
echo.
echo  管理员账号: admin / admin123
echo.
echo  如需停止服务，运行 stop.bat
echo.
echo  按任意键关闭此窗口（服务将继续运行）
pause >nul