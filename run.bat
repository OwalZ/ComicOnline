@echo off
REM Ejecuta el proyecto con limpieza previa.
if not exist pom.xml (
  echo [ERROR] Ejecuta este script en la carpeta donde esta pom.xml
  pause
  exit /b 1
)
echo [INFO] Limpiando y arrancando Spring Boot...
call mvn -DskipTests clean spring-boot:run
echo [INFO] Proceso terminado (codigo %ERRORLEVEL%)
pause
