@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ====================================
echo   UNO - Iniciando bases de datos
echo ====================================
echo.

:: ── Buscar psql.exe ───────────────────────────────────────────────────────────
set "PSQL="
for /f "tokens=*" %%i in ('where psql 2^>nul') do set "PSQL=%%i"
if "%PSQL%"=="" (
    for /d %%v in ("C:\Program Files\PostgreSQL\*") do (
        if exist "%%v\bin\psql.exe" set "PSQL=%%v\bin\psql.exe"
    )
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
echo Configurando PostgreSQL...
"%PSQL%" -U postgres -c "CREATE DATABASE unojavagame;" 2>nul
"%PSQL%" -U postgres -d unojavagame -f "%~dp0sql\schema.sql"
if errorlevel 1 (
    echo ERROR: fallo al crear la tabla. Comprueba la contrasena.
    pause & exit /b 1
)
echo PostgreSQL listo.

:: ── Arrancar MongoDB ──────────────────────────────────────────────────────────
echo.
echo Comprobando MongoDB...
set "MONGO_OK=0"
net start MongoDB >nul 2>&1
net start mongod >nul 2>&1
net start "MongoDB Server" >nul 2>&1
tasklist /fi "imagename eq mongod.exe" 2>nul | find "mongod.exe" >nul
if not errorlevel 1 set "MONGO_OK=1"

if "%MONGO_OK%"=="1" (
    echo MongoDB corriendo.
) else (
    echo AVISO: MongoDB no esta corriendo. Solo funcionara PostgreSQL.
    echo Para usar MongoDB instala el servicio desde mongodb.com/try/download/community
)

:: ── Escribir .env en esta misma carpeta ───────────────────────────────────────
echo.
(
    echo POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
    echo POSTGRES_USER=postgres
    echo POSTGRES_PASS=%PGPASS%
    echo MONGO_URI=mongodb://localhost:27017/unojavagame
    echo MONGO_DB=unojavagame
) > ".env"

echo .env creado en: %~dp0.env

echo.
echo ====================================
echo  Todo listo. Ya puedes abrir IntelliJ.
echo ====================================
echo.
pause
