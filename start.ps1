# Set Java Home and Path
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Using Java from: $env:JAVA_HOME"
Write-Host "Java version:"
java -version

Write-Host "`nCurrent directory: $(Get-Location)"

# Clean the target directory
if (Test-Path "target") {
    Write-Host "Cleaning target directory..."
    Remove-Item -Recurse -Force "target"
}

# Build the project
Write-Host "`nBuilding project..."
mvn clean package -DskipTests

# Check if build was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful! Starting application..."
    mvn spring-boot:run
} else {
    Write-Host "`nBuild failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}