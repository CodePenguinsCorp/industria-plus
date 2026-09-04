param(
    [string]$ComposeFile,
    [string]$EnvFile
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $ComposeFile) {
    $ComposeFile = Join-Path $repoRoot "compose.prod.yaml"
}
if (-not $EnvFile) {
    $EnvFile = Join-Path $repoRoot ".env.prod"
}

if (-not (Test-Path -LiteralPath $ComposeFile)) {
    throw "Arquivo $ComposeFile nao encontrado."
}

if (-not (Test-Path -LiteralPath $EnvFile)) {
    throw "Arquivo $EnvFile nao encontrado. Gere-o com scripts/init-prod-env.ps1."
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker nao foi encontrado no PATH."
}

function Invoke-ProductionCompose {
    param([string[]]$ComposeArgs)

    $dockerArgs = @("compose", "--env-file", $EnvFile, "-f", $ComposeFile) + $ComposeArgs
    & docker @dockerArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao executar 'docker $($dockerArgs -join ' ')'."
    }
}

Invoke-ProductionCompose -ComposeArgs @("pull")
Invoke-ProductionCompose -ComposeArgs @("up", "-d")
Invoke-ProductionCompose -ComposeArgs @("ps")
