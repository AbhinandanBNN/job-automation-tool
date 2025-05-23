package com.jobautomation.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.jobautomation.config.AppConfig;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaukriService {
    private final AppConfig appConfig;
    private WebDriver driver;
    private boolean isLoggedIn = false;
    private static final int MAX_RETRIES = 3;
    private static final long RATE_LIMIT_DELAY = 5000; // 5 seconds between applications
    private Set<String> appliedJobs = new HashSet<>();

    @PostConstruct
    public void init() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Add headless mode for cloud execution
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36");
        driver = new ChromeDriver(options);
        log.info("Chrome WebDriver initialized in headless mode");
    }

    public void login() {
        if (isLoggedIn) {
            return;
        }

        try {
            driver.get("https://www.naukri.com/nlogin/login");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Enter your active Email ID / Username']")));
            usernameField.sendKeys(appConfig.getNaukri().getUsername());

            WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Enter your password']"));
            passwordField.sendKeys(appConfig.getNaukri().getPassword());

            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            loginButton.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("nI-gNb-notification")));
            isLoggedIn = true;
            log.info("Logged in to Naukri successfully");
        } catch (Exception e) {
            log.error("Failed to log in to Naukri", e);
            isLoggedIn = false;
            throw new RuntimeException("Login failed", e);
        }
    }

    public void applyForJobs() {
        try {
            driver.get("https://www.naukri.com/jobs-in-" + appConfig.getNaukri().getLocation().toLowerCase());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement experienceFilter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-filter='experience']")));
            experienceFilter.click();
            WebElement experienceOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[contains(text(),'" + appConfig.getNaukri().getExperience() + "')]")));
            experienceOption.click();

            List<WebElement> jobListings = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.className("jobTuple")));

            for (WebElement job : jobListings) {
                try {
                    processJobListing(job, wait);
                } catch (Exception e) {
                    log.error("Error processing job listing", e);
                }
            }

            log.info("Completed applying for jobs");
        } catch (Exception e) {
            log.error("Failed to apply for jobs", e);
        }
    }

    private void processJobListing(WebElement job, WebDriverWait wait) {
        String jobTitle = job.findElement(By.className("title")).getText();
        String company = job.findElement(By.className("companyInfo")).getText();
        String jobId = job.getAttribute("id");
        
        if (appliedJobs.contains(jobId)) {
            log.debug("Already applied to {} at {}", jobTitle, company);
            return;
        }

        if (isJobMatchingSkills(job)) {
            int retryCount = 0;
            while (retryCount < MAX_RETRIES) {
                try {
                    WebElement applyButton = job.findElement(By.className("apply-button"));
                    if (applyButton.isEnabled()) {
                        applyButton.click();
                        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".confirm-apply")))
                            .click();
                        log.info("Successfully applied for: {} at {}", jobTitle, company);
                        appliedJobs.add(jobId);
                        Thread.sleep(RATE_LIMIT_DELAY);
                        break;
                    }
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount == MAX_RETRIES) {
                        log.error("Failed to apply for {} at {} after {} retries", jobTitle, company, MAX_RETRIES, e);
                    } else {
                        log.warn("Retry {} for {} at {}", retryCount, jobTitle, company);
                        try {
                            Thread.sleep(1000 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isJobMatchingSkills(WebElement job) {
        try {
            String jobTitle = job.findElement(By.className("title")).getText().toLowerCase();
            
            WebElement expandButton = job.findElement(By.cssSelector(".detail-button"));
            expandButton.click();
            
            WebDriverWait descWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement description = descWait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("job-description"))
            );
            String jobDescription = description.getText().toLowerCase();

            return Arrays.stream(appConfig.getNaukri().getSkills())
                .anyMatch(skill -> {
                    String skillLower = skill.toLowerCase();
                    return jobTitle.contains(skillLower) || 
                           jobDescription.contains(skillLower);
                });
        } catch (Exception e) {
            log.warn("Failed to check job description, falling back to title-only match", e);
            String jobTitle = job.findElement(By.className("title")).getText().toLowerCase();
            return Arrays.stream(appConfig.getNaukri().getSkills())
                .anyMatch(skill -> jobTitle.contains(skill.toLowerCase()));
        }
    }

    public void checkForInvites() {
        try {
            driver.get("https://www.naukri.com/mnjuser/inbox");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> unreadMessages = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".unread-message")));

            for (WebElement message : unreadMessages) {
                String sender = message.findElement(By.className("sender-name")).getText();
                String subject = message.findElement(By.className("message-subject")).getText();
                log.info("New invite received from {} with subject: {}", sender, subject);
            }

            log.info("Checked for invites successfully");
        } catch (Exception e) {
            log.error("Failed to check for invites", e);
        }
    }

    public void checkForWalkInDrives() {
        try {
            driver.get("https://www.naukri.com/walk-in-jobs-in-" + appConfig.getNaukri().getLocation().toLowerCase());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            List<WebElement> walkInJobs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".walk-in-job")));

            for (WebElement job : walkInJobs) {
                String title = job.findElement(By.className("title")).getText();
                String company = job.findElement(By.className("company")).getText();
                String date = job.findElement(By.className("walk-in-date")).getText();

                if (isITCompany(company)) {
                    log.info("New walk-in drive found: {} at {} on {}", title, company, date);
                }
            }

            log.info("Checked for walk-in drives successfully");
        } catch (Exception e) {
            log.error("Failed to check for walk-in drives", e);
        }
    }

    private boolean isITCompany(String company) {
        String companyLower = company.toLowerCase();
        return companyLower.contains("tech") || 
               companyLower.contains("software") || 
               companyLower.contains("it ") || 
               companyLower.contains("technologies") ||
               companyLower.contains("consulting");
    }

    private void handleError(String operation, Exception e, int retryCount) {
        log.error("Error during {}: {} (Attempt: {}/{})", operation, e.getMessage(), retryCount, MAX_RETRIES);
        if (retryCount >= MAX_RETRIES) {
            log.error("Max retries reached for {}. Stopping operation.", operation);
            throw new RuntimeException("Failed to complete " + operation + " after " + MAX_RETRIES + " attempts", e);
        }
        try {
            long delay = (long) Math.pow(2, retryCount) * 1000; // Exponential backoff
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (driver != null) {
            driver.quit();
            log.info("Chrome WebDriver shutdown complete");
        }
    }
}
