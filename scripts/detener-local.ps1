# Detiene los servicios iniciados con iniciar-local.ps1
$raiz = Split-Path -Parent $PSScriptRoot
$archivo = Join-Path $raiz '.run\pids.txt'

if (-not (Test-Path $archivo)) {
    Write-Host 'No hay servicios registrados en .run\pids.txt'
    exit 0
}

foreach ($id in Get-Content $archivo) {
    $proceso = Get-Process -Id $id -ErrorAction SilentlyContinue
    # Solo se detienen procesos java: evita cerrar otro programa que haya reutilizado el PID
    if ($proceso -and $proceso.ProcessName -like 'java*') {
        Stop-Process -Id $id -Force
        Write-Host "Detenido PID $id"
    }
}
Remove-Item $archivo
