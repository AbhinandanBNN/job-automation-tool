Write-Host "Setting up environment..."
$env:JAVA_HOME = 'C:\Program Files\jdk-24.0.1'
Write-Host "Using Java from: $env:JAVA_HOME"

Write-Host "Cleaning previous build..."
.\mvnw.cmd clean

Write-Host "Building the application..."
.\mvnw.cmd package -DskipTests

Write-Host "Running the application..."
.\mvnw.cmd spring-boot:run
