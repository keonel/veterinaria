# Inicia el backend completo SIN Docker (base de datos H2 en archivos).
# Uso, desde la carpeta raíz del proyecto:   .\scripts\iniciar-local.ps1
# Para detener:                               .\scripts\detener-local.ps1
#
# Cada servicio corre en su propia carpeta .run\<servicio>, donde guarda su base H2,
# sus claves y su archivo log.txt.

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$servicios = @('usuarios', 'catalogo', 'pedidos', 'citas', 'gateway')

# Java 17 o superior: se usa JAVA_HOME si existe; si no, el java del PATH
$java = 'java'
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $java = Join-Path $env:JAVA_HOME 'bin\java.exe'
}

# Carga las variables de .env (si existe) para este proceso
$archivoEnv = Join-Path $raiz '.env'
if (Test-Path $archivoEnv) {
    foreach ($linea in Get-Content $archivoEnv) {
        if ($linea -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)$') {
            [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim(), 'Process')
        }
    }
}
# Con H2 local no se usan las variables de MySQL
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue

# Compila si falta algún JAR
$faltan = $servicios | Where-Object { -not (Test-Path (Join-Path $raiz "backend\$_\target\$_-1.0.0.jar")) }
if ($faltan) {
    Write-Host 'Compilando el backend (la primera vez descarga dependencias)...'
    Push-Location (Join-Path $raiz 'backend')
    & .\mvnw.cmd -B -q -DskipTests package
    $codigo = $LASTEXITCODE
    Pop-Location
    if ($codigo -ne 0) { throw 'La compilación falló. Revisa que JAVA_HOME apunte a un JDK 17 o superior.' }
}

$pids = @()
foreach ($s in $servicios) {
    $carpeta = Join-Path $raiz ".run\$s"
    New-Item -ItemType Directory -Force $carpeta | Out-Null
    $jar = Join-Path $raiz "backend\$s\target\$s-1.0.0.jar"
    $proceso = Start-Process -FilePath $java -ArgumentList '-jar', "`"$jar`"" `
        -WorkingDirectory $carpeta -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $carpeta 'log.txt') `
        -RedirectStandardError (Join-Path $carpeta 'error.txt')
    $pids += $proceso.Id
    Write-Host "Iniciando $s (PID $($proceso.Id))"
}
$pids | Set-Content (Join-Path $raiz '.run\pids.txt')

Write-Host ''
Write-Host 'Espera unos 30 segundos y abre:'
Write-Host '  Swagger (todas las APIs): http://localhost:8080/swagger-ui.html'
Write-Host '  Frontend: en otra terminal ->  cd frontend ; npm install ; npm run dev'
Write-Host 'Logs: .run\<servicio>\log.txt'
