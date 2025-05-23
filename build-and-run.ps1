Write-Host "Building the project with Maven..."
try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nBuild successful! Starting the application...`n"
        mvn spring-boot:run
    } else {
        Write-Host "`nBuild failed. Please check the errors above.`n"
    }
} catch {
    Write-Host "An error occurred: $_"
}
