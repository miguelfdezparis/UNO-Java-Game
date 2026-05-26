@echo off
chcp 65001 >nul

echo ====================================
echo   UNO - Iniciando bases de datos
echo ====================================
echo.

:: ── Buscar psql.exe ──────────────────────────────────────────────────────────
set "PSQL="
for /f "tokens=*" %%i in ('where psql 2^>nul') do set "PSQL=%%i"
if "%PSQL%"=="" (
    for /r "C:\Program Files\PostgreSQL" %%i in (psql.exe) do set "PSQL=%%i"
)
if "%PSQL%"=="" (
    echo ERROR: No se encontro PostgreSQL instalado.
    echo Instala PostgreSQL o abre pgAdmin para verificar que esta instalado.
    pause & exit /b 1
)
echo PostgreSQL encontrado: %PSQL%

:: ── Pedir contrasena de postgres ─────────────────────────────────────────────
echo.
set /p "PGPASS=Introduce la contrasena de postgres (la que usas en pgAdmin): "
set "PGPASSWORD=%PGPASS%"

:: ── Crear base de datos ───────────────────────────────────────────────────────
echo.
echo Configurando base de datos...
"%PSQL%" -U postgres -c "CREATE DATABASE unojavagame;" 2>nul
echo PostgreSQL listo.

:: ── Arrancar MongoDB ─────────────────────────────────────────────────────────
echo.
echo Arrancando MongoDB...
net start MongoDB >nul 2>&1
if errorlevel 1 (
    sc query MongoDB >nul 2>&1
    if errorlevel 1 (
        echo AVISO: MongoDB no esta instalado como servicio.
        echo Arrancalo manualmente desde MongoDB Compass o como servicio.
    ) else (
        echo MongoDB ya estaba corriendo.
    )
) else (
    echo MongoDB arrancado.
)

:: ── Escribir .env ─────────────────────────────────────────────────────────────
set "ROOT=%~dp0"
(
    echo POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
    echo POSTGRES_USER=postgres
    echo POSTGRES_PASS=%PGPASS%
    echo MONGO_URI=mongodb://localhost:27017/unojavagame
    echo MONGO_DB=unojavagame
) > "%ROOT%.env"
echo .env configurado.

echo.
echo ====================================
echo  Todo listo! Ya puedes jugar.
echo ====================================
echo.
pause
