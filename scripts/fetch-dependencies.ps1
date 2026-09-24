$ErrorActionPreference = 'Stop'
$dependencyDirectory = Join-Path $PSScriptRoot '../libs'
New-Item -ItemType Directory -Path $dependencyDirectory -Force | Out-Null
$dependencies = @(
    @{
        Name = 'Powah-5.0.11-5.0.11.jar'
        Url = 'https://cdn.modrinth.com/data/KZO4S4DO/versions/xDOvxyqP/Powah-5.0.11.jar'
        Hash = '22F16AC88E8064F4F7FD94FB88FAD3C8723DBD6FA5C11B7FD73C078C293184FA'
    },
    @{
        Name = 'jei-1.20.1-forge-15.20.0.112.jar'
        Url = 'https://cdn.modrinth.com/data/u6dRKJwZ/versions/4r3Kp7U7/jei-1.20.1-forge-15.20.0.112.jar'
        Hash = '2D3A86F503DBDE51AFA5142951525C37E227016CE8D9E7DFD10BBDD9233F0204'
    },
    @{
        Name = 'appliedenergistics2-forge-15.3.6.jar'
        Url = 'https://cdn.modrinth.com/data/XxWD5pD3/versions/jshq8JsX/appliedenergistics2-forge-15.3.6.jar'
        Hash = '4DA423065BA24A26F672F3AF72F233CC22A517BAF18FEF9D63352DAA2FE614FE'
    }
)
foreach ($dependency in $dependencies) {
    $destination = Join-Path $dependencyDirectory $dependency.Name
    if (!(Test-Path -LiteralPath $destination)) {
        Invoke-WebRequest -Uri $dependency.Url -OutFile $destination
    }
    if ((Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash -ne $dependency.Hash) {
        throw "SHA-256 mismatch: $destination. Check the file before replacing it and retrying."
    }
    Write-Output "Verified $($dependency.Name)"
}
