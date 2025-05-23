Write-Host "Setting up environment variables..."
$env:JAVA_HOME = 'C:\Program Files\jdk-24.0.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "`nVerifying Java installation..."
java -version

Write-Host "`nCreating Maven wrapper..."
if (-not (Test-Path .mvn\wrapper)) {
    New-Item -ItemType Directory -Force -Path .mvn\wrapper
}

Write-Host "`nDownloading Maven..."
if (-not (Test-Path .\mvn.zip)) {
    Invoke-WebRequest -Uri "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile mvn.zip
    Expand-Archive mvn.zip -DestinationPath . -Force
}

Write-Host "`nSetting up Maven environment..."
$env:M2_HOME = ".\apache-maven-3.9.6"
$env:Path = "$env:M2_HOME\bin;$env:Path"

Write-Host "`nCleaning previous build..."
Remove-Item -Path target -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "`nBuilding and running the application..."
& "$env:M2_HOME\bin\mvn" clean spring-boot:run -DskipTests
