Write-Host "=== Job Automation Tool Build and Run Script ===" -ForegroundColor Green
Write-Host "Checking Maven installation..." -ForegroundColor Yellow
$mavenVersion = mvn -v
if ($LASTEXITCODE -eq 0) {
    Write-Host "Maven is properly installed" -ForegroundColor Green
    Write-Host $mavenVersion
} else {
    Write-Host "Maven is not properly installed. Please check your Maven installation." -ForegroundColor Red
    exit 1
}

Write-Host "`nChecking Java installation..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "Java is properly installed" -ForegroundColor Green
    Write-Host $javaVersion
} else {
    Write-Host "Java is not properly installed. Please check your Java installation." -ForegroundColor Red
    exit 1
}

Write-Host "`nBuilding the project..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBuild successful!" -ForegroundColor Green
    Write-Host "`nStarting the application..." -ForegroundColor Yellow
    mvn spring-boot:run
} else {
    Write-Host "`nBuild failed. Please check the errors above." -ForegroundColor Red
}
