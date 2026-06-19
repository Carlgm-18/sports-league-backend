param (
    [Parameter(Mandatory=$true)]
    [string]$ModuleName
)

$BasePath = "src/main/kotlin/es/uib/tfg/sports_league_backend/$ModuleName"
$FilesName =  (Get-Culture).TextInfo.ToTitleCase($ModuleName)

$Folders = @(
    # (Domain)
    "$BasePath/domain/errors",

    # (Application)
    "$BasePath/application",

    # (Infrastructure)
    "$BasePath/infrastructure/controller",
    "$BasePath/infrastructure/mapper",
    "$BasePath/infrastructure/repository"
)

$Files = @(
    # (Domain)
    "$BasePath/domain/${FilesName}.kt",
    "$BasePath/domain/errors/${FilesName}Error.kt",

    # (Application)
    "$BasePath/application/${FilesName}Service.kt",

    # (Infrastructure)
    "$BasePath/infrastructure/controller/${FilesName}Controller.kt",
    "$BasePath/infrastructure/mapper/${FilesName}Mapper.kt",
    "$BasePath/infrastructure/repository/${FilesName}Repository.kt"
)

foreach ($Folder in $Folders) {
    if (-not (Test-Path $Folder)) {
        New-Item -ItemType Directory -Path $Folder -Force | Out-Null
    }
}

foreach ($File in $Files) {
    if (-not (Test-Path $File)) {
        $FileDir = Split-Path $File -Parent
        $RelativeDir = $FileDir -replace "src\\main\\kotlin\\", ""
        $PackageName = $RelativeDir -replace "[/\\]", "."

        New-Item -ItemType File -Path $File -Value "package $PackageName`n`n" | Out-Null
    }
}

Write-Host "Module created at $BasePath!" -ForegroundColor Green