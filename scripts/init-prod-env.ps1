param(
    [string]$OutputPath,
    [string]$BackendImage = "industria-plus-backend:ci",
    [string]$FrontendImage = "industria-plus-frontend:ci",
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $OutputPath) {
    $OutputPath = Join-Path $repoRoot ".env.prod"
}

if ((Test-Path -LiteralPath $OutputPath) -and -not $Force) {
    throw "Arquivo $OutputPath ja existe. Use -Force para recriar."
}

function New-HexSecret {
    param([int]$ByteCount = 32)

    $bytes = New-Object byte[] $ByteCount
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
        return -join ($bytes | ForEach-Object { $_.ToString("x2") })
    }
    finally {
        $rng.Dispose()
    }
}

$mysqlPassword = New-HexSecret 24
$mysqlRootPassword = New-HexSecret 24

$content = @"
# Production Compose
FRONTEND_PORT=80
MYSQL_DATABASE=industria_plus
MYSQL_USER=industria_app
MYSQL_PASSWORD=$mysqlPassword
MYSQL_ROOT_PASSWORD=$mysqlRootPassword

# Images published by the delivery pipeline
BACKEND_IMAGE=$BackendImage
FRONTEND_IMAGE=$FrontendImage

# Backend / Spring Boot
LOG_LEVEL=INFO
"@

$outputDirectory = Split-Path -Parent $OutputPath
if ($outputDirectory -and -not (Test-Path -LiteralPath $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}

Set-Content -LiteralPath $OutputPath -Value $content -Encoding UTF8

Write-Host "Arquivo $OutputPath criado com senhas fortes."
Write-Host "Revise as imagens e guarde o arquivo fora do controle de versao."
