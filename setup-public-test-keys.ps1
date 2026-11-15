$ErrorActionPreference='Stop'
New-Item -ItemType Directory -Force "$PSScriptRoot\keys" | Out-Null
foreach($name in @('platform.pk8','platform.x509.pem')) {
    $url="https://android.googlesource.com/platform/build/+/refs/tags/android-9.0.0_r61/target/product/security/${name}?format=TEXT"
    $data=(Invoke-WebRequest $url -UseBasicParsing).Content
    if($data -is [byte[]]){$data=[Text.Encoding]::UTF8.GetString($data)}
    [IO.File]::WriteAllBytes("$PSScriptRoot\keys\$name",[Convert]::FromBase64String($data))
}
