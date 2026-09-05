@echo off
chcp 65001 >nul
title 乒乓球平台启动器

echo 正在启动后端...
start "后端" cmd /k "cd /d C:\Users\20702\Desktop\cordarts-progect\backend-node && npm install && npm run dev"

echo 等待后端启动...
timeout /t 8 /nobreak >nul

echo 正在启动前端...
start "前端" cmd /k "cd /d C:\Users\20702\Desktop\cordarts-progect\frontend && npm run dev"

echo 等待前端启动...
timeout /t 8 /nobreak >nul

echo 正在打开浏览器...
start http://localhost:5173

echo.
echo 启动完成！浏览器已打开。
echo 如果页面空白，请等几秒后刷新。
echo.
echo 管理后台: http://localhost:5173/admin/login
echo 管理员账号: admin / admin123
echo.
pause