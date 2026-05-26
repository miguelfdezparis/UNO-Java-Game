@echo off
chcp 65001 >nul

echo ====================================
echo   UNO - Iniciando bases de datos
echo ====================================
echo.

:: ── Buscar psql.exe ───────────────────────────────────────────────────────────
set "PSQL="
for /f "tokens=*" %%i in ('where psql 2^>nul') do set "PSQL=%%i"
if "%PSQL%"=="" (
    for /r "C:\Program Files\PostgreSQL" %%i in (psql.exe) do set "PSQL=%%i"
)
if "%PSQL%"=="" (
    echo ERROR: No se encontro PostgreSQL instalado.
    pause & exit /b 1
)

:: ── Pedir contrasena de postgres ──────────────────────────────────────────────
echo.
set /p "PGPASS=Contrasena de postgres (la que usas en pgAdmin): "
set "PGPASSWORD=%PGPASS%"

:: ── Crear BD y schema ─────────────────────────────────────────────────────────
echo.
echo Configurando base de datos...
"%PSQL%" -U postgres -c "CREATE DATABASE unojavagame;" 2>nul
"%PSQL%" -U postgres -d unojavagame -f "%~dp0sql\schema.sql"
if errorlevel 1 (
    echo ERROR: fallo al crear la tabla. Comprueba la contrasena.
    pause & exit /b 1
)
echo PostgreSQL listo.

:: ── Arrancar MongoDB ──────────────────────────────────────────────────────────
echo.
echo Arrancando MongoDB...
net start MongoDB >nul 2>&1
net start mongod >nul 2>&1
net start "MongoDB Server" >nul 2>&1
:: Verificar si mongod.exe esta corriendo
tasklist /fi "imagename eq mongod.exe" 2>nul | find "mongod.exe" >nul
if errorlevel 1 (
    echo AVISO: MongoDB no esta corriendo.
    echo Abre MongoDB Compass y asegurate de que el servidor este activo.
    echo Si no tienes MongoDB instalado, descargalo de https://www.mongodb.com/try/download/community
    pause
) else (
    echo MongoDB corriendo.
)

:: ── Escribir .env ─────────────────────────────────────────────────────────────
(
    echo POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
    echo POSTGRES_USER=postgres
    echo POSTGRES_PASS=%PGPASS%
    echo MONGO_URI=mongodb://localhost:27017/unojavagame
    echo MONGO_DB=unojavagame
) > "%~dp0.env"
echo .env configurado.

echo.
echo ====================================
echo  Todo listo! Ya puedes jugar.
echo ====================================
echo.
pause
