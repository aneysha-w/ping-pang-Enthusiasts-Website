@echo off
chcp 65001 >nul
title 启动乒乓球平台
echo ==========================================
echo   乒乓球交流平台 - 启动
echo ==========================================
echo.

echo [1/2] 启动后端服务（端口 8080）...
start "PingPong-Backend" cmd /k "cd /d %~dp0backend-node && npm install && npm run dev"

echo 等待后端启动...
timeout /t 5 /nobreak >nul

echo [2/2] 启动前端服务（端口 5173）...
start "PingPong-Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

timeout /t 5 /nobreak >nul
echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo  球友前台:  http://localhost:5173
echo  管理后台:  http://localhost:5173/admin/login
echo  管理员账号: admin / admin123
echo.
pause