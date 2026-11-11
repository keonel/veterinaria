# Deja el proyecto operativo con un solo doble clic (lo llama INICIAR.bat).
# 1) Verifica Java 17+ y Node 20.19+ (los instala con winget si faltan)
# 2) Compila el backend e instala el frontend (solo la primera vez)
# 3) Inicia los 5 servicios + el frontend y abre el navegador

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$run = Join-Path $raiz '.run'
Set-Location $raiz

function Paso($t) { Write-Host ''; Write-Host "==> $t" -ForegroundColor Magenta }
function Falla($t) { Write-Host ''; Write-Host "ERROR: $t" -ForegroundColor Red; exit 1 }

# ---------- Java ----------
function Version-Java($exe) {
    try {
        $salida = & cmd /c "`"$exe`" -version 2>&1" | Select-Object -First 1
        if ($salida -match '"(\d+)(\.(\d+))?') {
            $mayor = [int]$Matches[1]
            if ($mayor -eq 1) { $mayor = [int]$Matches[3] }   # 1.8 -> 8
            return $mayor
        }
    } catch {}
    return 0
}
function Buscar-Java {
    $candidatos = @()
    if ($env:JAVA_HOME) { $candidatos += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
    $candidatos += (Get-Command java -All -ErrorAction SilentlyContinue | ForEach-Object { $_.Source })
    foreach ($base in 'C:\Program Files\BellSoft', 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Java', 'C:\Program Files\Microsoft', 'C:\Program Files\Amazon Corretto', 'C:\Program Files\Zulu') {
        if (Test-Path $base) { $candidatos += (Get-ChildItem $base -Directory -ErrorAction SilentlyContinue | ForEach-Object { Join-Path $_.FullName 'bin\java.exe' }) }
    }
    foreach ($c in $candidatos) {
        if ($c -and (Test-Path $c) -and ((Version-Java $c) -ge 17)) { return $c }
    }
    return $null
}

Paso 'Verificando Java 17 o superior'
$java = Buscar-Java
if (-not $java) {
    Write-Host 'No se encontró Java 17+. Instalando Temurin JDK 21 con winget...'
    if (-not (Get-Command winget -ErrorAction SilentlyContinue)) { Falla 'Instala Java 21 manualmente desde https://adoptium.net y vuelve a ejecutar INICIAR.bat' }
    winget install --id EclipseAdoptium.Temurin.21.JDK --exact --silent --accept-package-agreements --accept-source-agreements | Out-Null
    $java = Buscar-Java
    if (-not $java) { Falla 'No se pudo instalar Java. Instálalo desde https://adoptium.net y vuelve a ejecutar INICIAR.bat' }
}
$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $java)
Write-Host "Java OK: $java"

# ---------- Node ----------
function Node-Valido {
    $n = Get-Command node -ErrorAction SilentlyContinue
    if (-not $n) { return $false }
    $v = [version]((& node -v).TrimStart('v'))
    return ($v.Major -ge 23) -or ($v.Major -eq 22 -and $v.Minor -ge 12) -or ($v.Major -eq 20 -and $v.Minor -ge 19)
}
Paso 'Verificando Node.js 20.19 o superior'
if (-not (Node-Valido)) {
    $env:Path = 'C:\Program Files\nodejs;' + $env:Path
}
if (-not (Node-Valido)) {
    Write-Host 'No se encontró un Node.js compatible. Instalando Node 24 con winget...'
    if (-not (Get-Command winget -ErrorAction SilentlyContinue)) { Falla 'Instala Node 22 o 24 desde https://nodejs.org y vuelve a ejecutar INICIAR.bat' }
    winget install --id OpenJS.NodeJS.LTS --exact --version 24.22.1 --silent --accept-package-agreements --accept-source-agreements | Out-Null
    $env:Path = 'C:\Program Files\nodejs;' + $env:Path
    if (-not (Node-Valido)) { Falla 'No se pudo instalar Node. Instálalo desde https://nodejs.org y vuelve a ejecutar INICIAR.bat' }
}
Write-Host "Node OK: $(& node -v)"

# ---------- Detener lo que haya quedado de una ejecución anterior ----------
& (Join-Path $PSScriptRoot 'detener-todo.ps1') -Silencioso

# ---------- Backend ----------
$servicios = @('usuarios', 'catalogo', 'pedidos', 'citas', 'gateway')
$faltan = $servicios | Where-Object { -not (Test-Path (Join-Path $raiz "backend\$_\target\$_-1.0.0.jar")) }
if ($faltan) {
    Paso 'Compilando el backend (la primera vez demora unos minutos: descarga dependencias)'
    Push-Location (Join-Path $raiz 'backend')
    & .\mvnw.cmd -B -q -DskipTests package
    $codigo = $LASTEXITCODE
    Pop-Location
    if ($codigo -ne 0) { Falla 'La compilación del backend falló. Revisa tu conexión a internet y vuelve a intentar.' }
}

# ---------- Frontend ----------
$front = Join-Path $raiz 'frontend'
if (-not (Test-Path (Join-Path $front 'node_modules'))) {
    Paso 'Instalando dependencias del frontend (solo la primera vez)'
    Push-Location $front
    & npm ci --no-audit --no-fund
    $codigo = $LASTEXITCODE
    Pop-Location
    if ($codigo -ne 0) { Falla 'npm no pudo instalar las dependencias del frontend.' }
}
if (-not (Test-Path (Join-Path $front '.env')) -and (Test-Path (Join-Path $front '.env.example'))) {
    Copy-Item (Join-Path $front '.env.example') (Join-Path $front '.env')
}

# ---------- Iniciar ----------
Paso 'Iniciando servicios'
Remove-Item Env:SPRING_PROFILES_ACTIVE -ErrorAction SilentlyContinue   # perfil h2: no requiere base de datos instalada
$pids = @()
foreach ($s in $servicios) {
    $carpeta = Join-Path $run $s
    New-Item -ItemType Directory -Force $carpeta | Out-Null
    $jar = Join-Path $raiz "backend\$s\target\$s-1.0.0.jar"
    $p = Start-Process -FilePath $java -ArgumentList '-jar', "`"$jar`"" -WorkingDirectory $carpeta -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $carpeta 'log.txt') -RedirectStandardError (Join-Path $carpeta 'error.txt')
    $pids += $p.Id
    Write-Host "  $s iniciado"
}
$npm = (Get-Command npm.cmd -ErrorAction SilentlyContinue).Source
$p = Start-Process -FilePath $npm -ArgumentList 'run', 'dev', '--', '--port', '5173', '--strictPort' -WorkingDirectory $front -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput (Join-Path $run 'frontend.log') -RedirectStandardError (Join-Path $run 'frontend-error.log')
$pids += $p.Id
Write-Host '  frontend iniciado'
$pids | Set-Content (Join-Path $run 'pids.txt')

# ---------- Esperar a que estén listos ----------
Paso 'Esperando a que todo esté listo (hasta 2 minutos)'
$puertos = 8081, 8082, 8083, 8084, 8080
$limite = (Get-Date).AddMinutes(2)
$listo = $false
while ((Get-Date) -lt $limite) {
    $ok = $true
    foreach ($pt in $puertos) {
        try { $r = Invoke-WebRequest "http://localhost:$pt/actuator/health" -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -ne 200) { $ok = $false } } catch { $ok = $false }
    }
    try { Invoke-WebRequest 'http://localhost:5173' -UseBasicParsing -TimeoutSec 2 | Out-Null } catch { $ok = $false }
    if ($ok) { $listo = $true; break }
    Write-Host '.' -NoNewline
    Start-Sleep -Seconds 3
}
Write-Host ''
if (-not $listo) { Falla "Los servicios no respondieron a tiempo. Revisa los registros en la carpeta .run (por ejemplo .run\usuarios\log.txt)." }

Start-Process 'http://localhost:5173'
Write-Host ''
Write-Host '=============================================' -ForegroundColor Green
Write-Host ' Veterinaria San Marcos está funcionando' -ForegroundColor Green
Write-Host '=============================================' -ForegroundColor Green
Write-Host ' Tienda:   http://localhost:5173'
Write-Host ' Swagger:  http://localhost:8080/swagger-ui.html'
Write-Host ''
Write-Host ' Cuentas de prueba:'
Write-Host '   admin@sanmarcos.cl     Admin1234     (Administrador)'
Write-Host '   vendedor@sanmarcos.cl  Vende1234     (Vendedor)'
Write-Host '   cliente@gmail.com      Cliente123    (Cliente)'
Write-Host ''
Write-Host ' Para apagar todo, ejecuta DETENER.bat'
