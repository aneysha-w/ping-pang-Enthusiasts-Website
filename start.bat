@echo off
chcp 65001 >nul
echo ==========================================
echo   乒乓球交流平台 - 启动脚本
echo ==========================================
echo.

echo [1/3] 检查环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Java，请安装 JDK 17+
    pause
    exit /b 1
)
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未找到 Node.js，请安装 Node.js 18+
    pause
    exit /b 1
)
echo Java 和 Node.js 已就绪
echo.

echo [2/3] 启动后端服务 (端口 8080)...
start "PingPong Backend" cmd /c "cd /d %~dp0backend && mvn spring-boot:run 2>&1"

echo 等待后端启动...
timeout /t 15 /nobreak >nul

echo [3/3] 启动前端服务 (端口 5173)...
start "PingPong Frontend" cmd /c "cd /d %~dp0frontend && npm install && npm run dev 2>&1"

timeout /t 5 /nobreak >nul
echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo  球友前台:  http://localhost:5173
echo  管理后台:  http://localhost:5173/admin/login
echo  H2控制台:  http://localhost:8080/api/v1/h2-console
echo.
echo  管理员账号: admin / admin123
echo.
echo  按任意键关闭此窗口（服务将继续运行）
pause >nul