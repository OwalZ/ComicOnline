@echo off
REM Ejecuta el proyecto con limpieza previa y seleccion de puerto dinamica.
REM Uso: run.bat [puertoBase]

if not exist pom.xml (
  echo [ERROR] Ejecuta este script en la carpeta donde esta pom.xml
  pause
  exit /b 1
)

set BASE_PORT=8080
if not "%1"=="" set BASE_PORT=%1
set PORT=%BASE_PORT%
set MAX_PORT=8100

echo [INFO] Buscando puerto disponible a partir de %PORT% ...

:CHECK_PORT
REM Busca si el puerto está en uso (LISTENING) usando netstat
netstat -ano | findstr ":%PORT% " | findstr /i "LISTENING" >nul 2>&1
if %errorlevel%==0 (
  echo [WARN] Puerto %PORT% en uso. Probando siguiente...
  set /a PORT+=1
  if %PORT% GTR %MAX_PORT% (
    echo [ERROR] No se encontro puerto libre en el rango %BASE_PORT%-%MAX_PORT%.
    echo [TIP] Cierra el proceso que usa el puerto original con:
    echo       netstat -ano ^| findstr :8080
    echo       taskkill /PID <PID> /F
    pause
    exit /b 1
  )
  goto CHECK_PORT
)

echo [INFO] Usando puerto %PORT%
echo [INFO] Limpiando y arrancando Spring Boot...
call mvn -DskipTests -Dspring-boot.run.arguments=--server.port=%PORT% clean spring-boot:run
echo [INFO] Proceso terminado (codigo %ERRORLEVEL%)
pause
