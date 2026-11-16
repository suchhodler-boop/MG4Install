# SPDX-License-Identifier: GPL-3.0-only
param()
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$manifest=([xml](Get-Content "$root\AndroidManifest.xml" -Raw)).manifest
$version=$manifest.versionName
$name="MG4-Install-$version-car.apk"
$apk=Get-Item -LiteralPath "$root\dist\$name"
$hash=(Get-FileHash -LiteralPath $apk.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
$update=@{schema=1;packageName='com.mg4.install';versionName=$version;versionCode=[long]$manifest.versionCode;apk=@{name=$name;url="https://github.com/suchhodler-boop/MG4Install/releases/download/v$version/$name";size=$apk.Length;sha256=$hash}}
[IO.File]::WriteAllText("$root\dist\update.json",($update|ConvertTo-Json -Depth 4),[Text.UTF8Encoding]::new($false))
[IO.File]::WriteAllText("$root\dist\SHA256SUMS.txt","$hash  $name`n",[Text.UTF8Encoding]::new($false))
Write-Host "Prepared update.json and SHA256SUMS.txt for v$version"
