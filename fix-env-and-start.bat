@echo off
chcp 65001 >nul
title 自动配置环境并启动
echo ==========================================
echo   自动配置环境并启动项目
echo ==========================================
echo.

set "JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set "MAVEN_HOME=C:\tools\apache-maven-3.9.9"

echo [1/5] 配置 Java 环境变量...
setx JAVA_HOME "%JAVA_HOME%" /M >nul 2>&1
if %errorlevel% neq 0 (
    setx JAVA_HOME "%JAVA_HOME%" >nul 2>&1
)
echo   JAVA_HOME = %JAVA_HOME%

echo.
echo [2/5] 下载并安装 Maven...
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo   正在下载 Maven 3.9.9...
    if not exist "C:\tools" mkdir "C:\tools"
    powershell -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip' -OutFile 'C:\tools\maven.zip' -UseBasicParsing; Write-Host '下载完成'; Expand-Archive -Path 'C:\tools\maven.zip' -DestinationPath 'C:\tools' -Force; Write-Host '解压完成' } catch { Write-Host $_.Exception.Message }"
    if exist "C:\tools\maven.zip" del "C:\tools\maven.zip"
) else (
    echo   Maven 已存在
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo.
    echo   Maven 下载失败！请手动下载:
    echo   https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip
    echo   解压到 C:\tools\apache-maven-3.9.9
    pause
    exit /b 1
)

echo.
echo [3/5] 配置 Maven 环境变量...
setx MAVEN_HOME "%MAVEN_HOME%" /M >nul 2>&1
if %errorlevel% neq 0 (
    setx MAVEN_HOME "%MAVEN_HOME%" >nul 2>&1
)
echo   MAVEN_HOME = %MAVEN_HOME%

echo.
echo [4/5] 更新 PATH 环境变量...
for /f "usebackq tokens=2,*" %%A in (`reg query "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment" /v PATH 2^>nul`) do set "SYS_PATH=%%B"
echo %SYS_PATH% | findstr /i "%JAVA_HOME%\bin" >nul
if %errorlevel% neq 0 (
    setx PATH "%SYS_PATH%;%JAVA_HOME%\bin;%MAVEN_HOME%\bin" /M >nul 2>&1
    if %errorlevel% neq 0 (
        for /f "usebackq tokens=2,*" %%A in (`reg query "HKCU\Environment" /v PATH 2^>nul`) do set "USR_PATH=%%B"
        setx PATH "%USR_PATH%;%JAVA_HOME%\bin;%MAVEN_HOME%\bin" >nul 2>&1
    )
    echo   PATH 已更新
) else (
    echo   PATH 已包含所需路径
)

echo.
echo [5/5] 验证并启动...
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo   验证 Java:
"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /i "version"

echo   验证 Maven:
"%MAVEN_HOME%\bin\mvn" -v 2>&1 | findstr /i "Apache Maven"

echo.
echo   环境配置完成！正在启动服务...
echo.

echo   启动后端（端口 8080）...
start "PingPong-Backend" cmd /k "set \"PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%\" && cd /d C:\Users\20702\Desktop\cordarts-progect\backend && mvn spring-boot:run"

echo   等待后端启动（30秒）...
timeout /t 30 /nobreak >nul

echo   启动前端（端口 5173）...
start "PingPong-Frontend" cmd /k "cd /d C:\Users\20702\Desktop\cordarts-progect\frontend && npm install && npm run dev"

timeout /t 10 /nobreak >nul

echo.
echo ==========================================
echo   启动完成！
echo ==========================================
echo.
echo   球友前台:  http://localhost:5173
echo   管理后台:  http://localhost:5173/admin/login
echo   管理员账号: admin / admin123
echo.
pause