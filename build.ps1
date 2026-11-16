param([switch]$Emulator, [string]$Sdk="$env:LOCALAPPDATA\Android\Sdk", [string]$Jdk='C:\Program Files\Android\Android Studio1\jbr')
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$env:JAVA_HOME=$Jdk
$env:PATH="$Jdk\bin;$env:PATH"
$bt=Join-Path $Sdk 'build-tools\36.0.0'
$android=Join-Path $Sdk 'platforms\android-34\android.jar'
$variant=if($Emulator){'emulator'}else{'car'}
$build=Join-Path $root "build\$variant"
New-Item -ItemType Directory -Force -Path $build,"$build\classes","$build\dex","$root\dist" | Out-Null
function Check([string]$step){if($LASTEXITCODE -ne 0){throw "$step failed ($LASTEXITCODE)"}}
$manifest=Get-Content "$root\AndroidManifest.xml" -Raw
if($Emulator){$manifest=$manifest.Replace(' android:sharedUserId="android.uid.system"','').Replace('com.mg4.install','com.mg4.install.emulator').Replace('android:name=".MainActivity"','android:name="com.mg4.install.MainActivity"').Replace('android:name=".InstallService"','android:name="com.mg4.install.InstallService"').Replace('android:name=".InstallResult"','android:name="com.mg4.install.InstallResult"').Replace('android:name=".UpdateReceiver"','android:name="com.mg4.install.UpdateReceiver"')}
[IO.File]::WriteAllText("$build\AndroidManifest.xml",$manifest)
& "$bt\aapt2.exe" compile --dir "$root\res" -o "$build\resources.zip"; Check 'Resource compilation'
& "$bt\aapt2.exe" link -o "$build\base.apk" --manifest "$build\AndroidManifest.xml" -I $android "$build\resources.zip"; Check 'Resource linking'
$sources=Get-ChildItem "$root\src" -Filter *.java -Recurse | ForEach-Object { '"'+$_.FullName.Replace('\','/')+'"' }
[IO.File]::WriteAllLines("$build\sources.txt",$sources)
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath $android -d "$build\classes" "@$build\sources.txt"; Check 'Java compilation'
& "$Jdk\bin\jar.exe" cf "$build\classes.jar" -C "$build\classes" .; Check 'Classes jar'
& "$bt\d8.bat" --lib $android --min-api 28 --output "$build\dex" "$build\classes.jar"; Check 'Dex compilation'
Copy-Item "$build\base.apk" "$build\unsigned.apk" -Force
& "$Jdk\bin\jar.exe" uf "$build\unsigned.apk" -C "$build\dex" classes.dex; Check 'APK assembly'
& "$bt\zipalign.exe" -f 4 "$build\unsigned.apk" "$build\aligned.apk"; Check 'Alignment'
$version=([xml](Get-Content "$root\AndroidManifest.xml" -Raw)).manifest.versionName
$out="$root\dist\MG4-Install-$version-$variant.apk"
& "$bt\apksigner.bat" sign --key "$root\keys\platform.pk8" --cert "$root\keys\platform.x509.pem" --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled false --out $out "$build\aligned.apk"; Check 'Signing'
& "$bt\apksigner.bat" verify --verbose $out; Check 'Signature verification'
Write-Host "Built $out"


