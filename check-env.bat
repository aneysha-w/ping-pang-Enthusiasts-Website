@echo off
chcp 65001 >nul
echo ==========================================
echo   环境诊断检查
echo ==========================================
echo.

echo [1] 检查 Node.js...
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo   ✗ 未安装 Node.js
    set "MISSING_NODE=1"
) else (
    for /f "tokens=*" %%i in ('node -v') do echo   ✓ Node.js: %%i
)

echo.
echo [2] 检查 npm...
where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo   ✗ 未安装 npm
) else (
    for /f "tokens=*" %%i in ('npm -v') do echo   ✓ npm: %%i
)

echo.
echo [3] 检查 Java...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo   ✗ 未安装 Java
    set "MISSING_JAVA=1"
) else (
    for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do echo   ✓ Java: %%i
)

echo.
echo [4] 检查 Maven...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo   ✗ 未安装 Maven
    set "MISSING_MVN=1"
) else (
    for /f "tokens=*" %%i in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do echo   ✓ %%i
)

echo.
echo [5] 检查 winget...
where winget >nul 2>&1
if %errorlevel% neq 0 (
    echo   ✗ 未安装 winget（无法自动安装）
) else (
    echo   ✓ winget 可用
)

echo.
echo [6] 检查前端依赖...
if exist "%~dp0frontend\node_modules" (
    echo   ✓ 前端依赖已安装
) else (
    echo   ✗ 前端依赖未安装（需要运行 npm install）
)

echo.
echo [7] 检查端口占用...
netstat -ano | findstr ":8080 " >nul 2>&1
if %errorlevel% equ 0 (
    echo   ! 端口 8080 已被占用
) else (
    echo   ✓ 端口 8080 空闲
)
netstat -ano | findstr ":5173 " >nul 2>&1
if %errorlevel% equ 0 (
    echo   ! 端口 5173 已被占用
) else (
    echo   ✓ 端口 5173 空闲
)

echo.
echo ==========================================
echo   诊断完成，请将以上内容截图发给我
echo ==========================================
echo.
pause