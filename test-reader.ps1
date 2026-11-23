param([string]$Serial='emulator-5582', [string]$Sdk="$env:LOCALAPPDATA\Android\Sdk", [string]$Jdk='C:\Program Files\Android\Android Studio1\jbr')
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$scratch="$root\.scratch\reader-test"
$env:JAVA_HOME=$Jdk
$env:PATH="$Jdk\bin;$env:PATH"
$bt="$Sdk\build-tools\36.0.0"
New-Item -ItemType Directory -Force "$scratch\classes","$scratch\dex" | Out-Null
function Check([string]$step){if($LASTEXITCODE -ne 0){throw "$step failed"}}
@'
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.mg4.install.tests"><uses-sdk android:minSdkVersion="28" android:targetSdkVersion="28"/><application android:label="Reader validation"/><instrumentation android:name="com.mg4.install.tests.ReaderLoadingTest" android:targetPackage="com.mg4.install.emulator"/></manifest>
'@ | Set-Content "$scratch\AndroidManifest.xml" -Encoding utf8
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath "$Sdk\platforms\android-34\android.jar;$root\build\emulator\classes.jar" -d "$scratch\classes" "$root\tests\android\ReaderLoadingTest.java"; Check 'Test compilation'
& "$Jdk\bin\jar.exe" cf "$scratch\test.jar" -C "$scratch\classes" .; Check 'Test jar'
& "$bt\d8.bat" --lib "$Sdk\platforms\android-34\android.jar" --classpath "$root\build\emulator\classes.jar" --min-api 28 --output "$scratch\dex" "$scratch\test.jar"; Check 'Dex'
& "$bt\aapt2.exe" link -o "$scratch\base.apk" --manifest "$scratch\AndroidManifest.xml" -I "$Sdk\platforms\android-34\android.jar"; Check 'Link'
& "$Jdk\bin\jar.exe" uf "$scratch\base.apk" -C "$scratch\dex" classes.dex; Check 'Assembly'
& "$bt\zipalign.exe" -f 4 "$scratch\base.apk" "$scratch\aligned.apk"; Check 'Align'
& "$bt\apksigner.bat" sign --key "$root\keys\platform.pk8" --cert "$root\keys\platform.x509.pem" --out "$scratch\test.apk" "$scratch\aligned.apk"; Check 'Signing'
& "$Sdk\platform-tools\adb.exe" -s $Serial install -r "$scratch\test.apk"; Check 'Install'
try {
 $result=& "$Sdk\platform-tools\adb.exe" -s $Serial shell am instrument -w com.mg4.install.tests/com.mg4.install.tests.ReaderLoadingTest
 $result | Write-Output
 if(($result -join "`n") -notmatch 'PASS:'){throw 'Reader device regression failed'}
} finally { & "$Sdk\platform-tools\adb.exe" -s $Serial uninstall com.mg4.install.tests }
