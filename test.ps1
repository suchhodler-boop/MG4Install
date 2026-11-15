param([string]$Jdk='C:\Program Files\Android\Android Studio1\jbr', [string]$LiveFixture='')
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
New-Item -ItemType Directory -Force "$root\build\tests" | Out-Null
& "$Jdk\bin\javac.exe" -d "$root\build\tests" "$root\src\com\mg4\install\FeedParser.java" "$root\tests\FeedParserTest.java"
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
if($LiveFixture){ & "$Jdk\bin\java.exe" -cp "$root\build\tests" FeedParserTest $LiveFixture }
else { & "$Jdk\bin\java.exe" -cp "$root\build\tests" FeedParserTest }
if($LASTEXITCODE -ne 0){throw 'Tests failed'}
