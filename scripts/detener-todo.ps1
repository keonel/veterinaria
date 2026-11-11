# Detiene el backend y el frontend iniciados por INICIAR.bat
param([switch]$Silencioso)

$raiz = Split-Path -Parent $PSScriptRoot
$archivo = Join-Path $raiz '.run\pids.txt'

function Matar-Arbol($id) {
    # taskkill /T cierra también los procesos hijos (npm -> node)
    & taskkill /PID $id /T /F 2>&1 | Out-Null
}

# 1) Procesos registrados
if (Test-Path $archivo) {
    foreach ($id in Get-Content $archivo) { Matar-Arbol $id }
    Remove-Item $archivo -ErrorAction SilentlyContinue
}

# 2) Por si quedó algo escuchando en nuestros puertos (solo java y node)
foreach ($puerto in 8080, 8081, 8082, 8083, 8084, 5173) {
    Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue | ForEach-Object {
        $p = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue
        if ($p -and $p.ProcessName -match '^(java|node)$') { Matar-Arbol $p.Id }
    }
}

Start-Sleep -Seconds 2
if (-not $Silencioso) { Write-Host 'Servicios detenidos.' }
