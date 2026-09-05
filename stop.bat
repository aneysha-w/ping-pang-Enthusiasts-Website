@echo off
chcp 65001 >nul
echo 正在停止所有服务...
taskkill /fi "WINDOWTITLE eq PingPong Backend*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq PingPong Frontend*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq java*" /f >nul 2>&1
taskkill /fi "WINDOWTITLE eq node*" /f >nul 2>&1
echo 服务已停止
pause