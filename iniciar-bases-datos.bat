@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
set "DBDIR=%ROOT%databases"
set "PGDIR=%DBDIR%\pgsql"
set "PGDATA=%DBDIR%\pgdata"
set "MGDIR=%DBDIR%\mongodb"
set "MGDATA=%DBDIR%\mongodata"

echo ====================================
echo   UNO - Iniciando bases de datos
echo ====================================
echo.

if not exist "%DBDIR%"   mkdir "%DBDIR%"
if not exist "%PGDATA%"  mkdir "%PGDATA%"
if not exist "%MGDATA%"  mkdir "%MGDATA%"

:: ── PostgreSQL portable ──────────────────────────────────────────────────────
if not exist "%PGDIR%\bin\pg_ctl.exe" (
    echo [1/2] Descargando PostgreSQL... primera vez, ~60MB, espera un momento
    powershell -Command "Invoke-WebRequest -Uri 'https://get.enterprisedb.com/postgresql/postgresql-16.4-1-windows-x64-binaries.zip' -OutFile '%DBDIR%\pg.zip' -UseBasicParsing"
    powershell -Command "Expand-Archive -Path '%DBDIR%\pg.zip' -DestinationPath '%DBDIR%' -Force"
    del "%DBDIR%\pg.zip"
    echo PostgreSQL descargado.
    echo.
)

if not exist "%PGDATA%\postgresql.conf" (
    echo Configurando PostgreSQL por primera vez...
    "%PGDIR%\bin\initdb.exe" -D "%PGDATA%" -U postgres -E UTF8 -A trust >nul 2>&1
    "%PGDIR%\bin\pg_ctl.exe" -D "%PGDATA%" -l "%DBDIR%\pg.log" start -w >nul 2>&1
    timeout /t 2 /nobreak >nul
    "%PGDIR%\bin\psql.exe" -U postgres -c "CREATE USER uno_pg WITH PASSWORD 'unoLocal2026';" >nul 2>&1
    "%PGDIR%\bin\psql.exe" -U postgres -c "CREATE DATABASE unojavagame OWNER uno_pg;" >nul 2>&1
    echo PostgreSQL listo.
) else (
    "%PGDIR%\bin\pg_ctl.exe" -D "%PGDATA%" status >nul 2>&1
    if errorlevel 1 (
        "%PGDIR%\bin\pg_ctl.exe" -D "%PGDATA%" -l "%DBDIR%\pg.log" start -w >nul 2>&1
    )
    echo PostgreSQL corriendo.
)

:: ── MongoDB portable ─────────────────────────────────────────────────────────
if not exist "%MGDIR%\bin\mongod.exe" (
    echo [2/2] Descargando MongoDB... primera vez, ~150MB, espera un momento
    powershell -Command "Invoke-WebRequest -Uri 'https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-7.0.12.zip' -OutFile '%DBDIR%\mg.zip' -UseBasicParsing"
    powershell -Command "Expand-Archive -Path '%DBDIR%\mg.zip' -DestinationPath '%DBDIR%\mgtmp' -Force"
    for /d %%i in ("%DBDIR%\mgtmp\*") do xcopy "%%i" "%MGDIR%\" /e /i /q >nul
    rmdir /s /q "%DBDIR%\mgtmp"
    del "%DBDIR%\mg.zip"
    echo MongoDB descargado.
    echo.
)

tasklist /fi "imagename eq mongod.exe" 2>nul | find "mongod.exe" >nul
if errorlevel 1 (
    start /b "" "%MGDIR%\bin\mongod.exe" --dbpath "%MGDATA%" --port 27017 --logpath "%DBDIR%\mongo.log" --logappend
    timeout /t 2 /nobreak >nul
)
echo MongoDB corriendo.

:: ── .env ─────────────────────────────────────────────────────────────────────
(
    echo POSTGRES_URL=jdbc:postgresql://localhost:5432/unojavagame
    echo POSTGRES_USER=uno_pg
    echo POSTGRES_PASS=unoLocal2026
    echo MONGO_URI=mongodb://localhost:27017/unojavagame
    echo MONGO_DB=unojavagame
) > "%ROOT%.env"

echo.
echo ====================================
echo  Todo listo! Ejecuta el juego.
echo ====================================
echo.
pause
