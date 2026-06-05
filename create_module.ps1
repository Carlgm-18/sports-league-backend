param (
    [Parameter(Mandatory=$true)]
    [string]$ModuleName
)

# Configura aquí la ruta base de tus paquetes de Kotlin
# Cambia 'com/tuproyecto' por la estructura real de tu grupo de Spring Boot
$BasePath = "src/main/kotlin/es/uib/tfg/sports_league_backend/$ModuleName"

Write-Host "Creando estructura DDD para el módulo: $ModuleName..." -ForegroundColor Cyan

# Lista de carpetas a crear respetando la arquitectura limpia
$Folders = @(
    # 1. Capa de Dominio (Domain)
    "$BasePath/domain/errors",

    # 2. Capa de Aplicación (Application)
    "$BasePath/application/",

    # 3. Capa de Infraestructura (Infrastructure)
    "$BasePath/infrastructure/controller",
    "$BasePath/infrastructure/mapper",
    "$BasePath/infrastructure/repository"
)

# Crear cada carpeta de forma recursiva si no existe
foreach ($Folder in $Folders) {
    if (-not (Test-Path $Folder)) {
        New-Item -ItemType Directory -Path $Folder -Force | Out-Null
        # Esto crea un archivo vacío dentro de la carpeta
        New-Item -ItemType File -Path "$Folder/.gitkeep" -Force | Out-Null
    }
}

Write-Host "¡Estructura creada con éxito en $BasePath!" -ForegroundColor Green