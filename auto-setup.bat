@echo off
chcp 65001 >nul
title 乒乓球平台 - 自动安装并启动
echo ==========================================
echo   乒乓球交流平台 - 自动安装并启动
echo ==========================================
echo.

REM 刷新环境变量
call :RefreshEnvVars

echo [步骤1] 检查 Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo   未检测到 Node.js，正在通过 winget 安装...
    winget install OpenJS.NodeJS.LTS --accept-source-agreements --accept-package-agreements -h
    call :RefreshEnvVars
)
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo   winget 安装可能需要重启刷新环境变量
    echo   请尝试: 关闭此窗口，重新双击运行本脚本
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('node -v') do echo   Node.js: %%i

echo.
echo [步骤2] 检查 Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo   未检测到 Java，正在通过 winget 安装 JDK 17...
    winget install Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements -h
    call :RefreshEnvVars
)
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo   Java 未就绪，请重启电脑后重新运行本脚本
    pause
    exit /b 1
)
echo   Java 已就绪

echo.
echo [步骤3] 检查 Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo   未检测到 Maven，正在通过 winget 安装...
    winget install Apache.Maven --accept-source-agreements --accept-package-agreements -h
    call :RefreshEnvVars
)
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo   Maven 自动安装失败！
    echo   请手动下载: https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
    echo   解压后将 bin 目录添加到系统 PATH 环境变量
    echo   然后重新运行本脚本
    pause
    exit /b 1
)
echo   Maven 已就绪

echo.
echo [步骤4] 安装前端依赖...
cd /d "%~dp0frontend"
if not exist "node_modules" (
    echo   正在安装前端依赖（可能需要几分钟）...
    call npm install
    if %errorlevel% neq 0 (
        echo   前端依赖安装失败！
        echo   请手动运行: cd frontend ^&^& npm install
        pause
        exit /b 1
    )
)
echo   前端依赖已就绪

echo.
echo [步骤5] 启动后端服务...
echo   后端启动中（端口 8080），请等待...
start "PingPong-Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"

echo   等待后端就绪（25秒）...
timeout /t 25 /nobreak >nul

echo.
echo [步骤6] 启动前端服务...
start "PingPong-Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo   等待前端就绪（8秒）...
timeout /t 8 /nobreak >nul

echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo   球友前台:  http://localhost:5173
echo   管理后台:  http://localhost:5173/admin/login
echo.
echo   管理员账号: admin / admin123
echo.
echo   首次安装后如无法访问，请重启电脑再运行本脚本
echo.
pause
exit /b 0

:RefreshEnvVars
REM 刷新环境变量
for /f "usebackq tokens=2,*" %%A in (`reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v PATH 2^>nul`) do set "SYS_PATH=%%B"
for /f "usebackq tokens=2,*" %%A in (`reg query "HKCU\Environment" /v PATH 2^>nul`) do set "USR_PATH=%%B"
set "PATH=%SYS_PATH%;%USR_PATH%"
goto :eof