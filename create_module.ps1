param (
    [Parameter(Mandatory=$true)]
    [string]$ModuleName
)

$BasePath = "src/main/kotlin/es/uib/tfg/sports_league_backend/$ModuleName"

$Folders = @(
    # (Domain)
    "$BasePath/domain/errors",

    # (Application)
    "$BasePath/application/",

    # (Infrastructure)
    "$BasePath/infrastructure/controller",
    "$BasePath/infrastructure/mapper",
    "$BasePath/infrastructure/repository"
)

foreach ($Folder in $Folders) {
    if (-not (Test-Path $Folder)) {
        New-Item -ItemType Directory -Path $Folder -Force | Out-Null
        New-Item -ItemType File -Path "$Folder/.gitkeep" -Force | Out-Null
    }
}

Write-Host "¡Module creted at $BasePath!" -ForegroundColor Green