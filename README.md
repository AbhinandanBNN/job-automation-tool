# Naukri Job Automation Tool

An automated tool for job applications on Naukri.com using Spring Boot and Selenium WebDriver.

## Features

- Automated login to Naukri.com
- Job search based on configured location and experience
- Automatic job application for matching skills
- Rate limiting to prevent account restrictions
- Headless browser support for cloud deployment
- Scheduled execution every 4 hours

## Configuration

The following environment variables need to be set:
- `NAUKRI_USERNAME`: Your Naukri.com email/username
- `NAUKRI_PASSWORD`: Your Naukri.com password

Other configurations in `application.properties`:
- Location
- Skills
- Experience level

## Running Locally

```bash
mvn spring-boot:run
```

## GitHub Actions

The tool is configured to run automatically every 4 hours using GitHub Actions. Check the `.github/workflows/naukri-automation.yml` file for the workflow configuration.
