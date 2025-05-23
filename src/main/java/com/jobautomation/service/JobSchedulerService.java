package com.jobautomation.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSchedulerService {
    private final NaukriService naukriService;

    @PostConstruct
    public void init() {
        log.info("Initializing Job Automation Tool");
        try {
            naukriService.login();
            applyForJobs();
            checkForInvites();
            checkForWalkInDrives();
            log.info("Initial automation tasks completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize automation tasks", e);
        }
    }

    @Scheduled(cron = "0 0 10 * * ?") // Run at 10:00 AM every day
    public void applyForJobs() {
        log.info("Starting daily job application process");
        naukriService.login();
        naukriService.applyForJobs();
    }

    @Scheduled(cron = "0 0/30 * * * ?") // Run every 30 minutes
    public void checkForInvites() {
        log.info("Checking for new invites");
        naukriService.login();
        naukriService.checkForInvites();
    }

    @Scheduled(cron = "0 0 9,14,18 * * ?") // Run at 9 AM, 2 PM, and 6 PM
    public void checkForWalkInDrives() {
        log.info("Checking for walk-in drives");
        naukriService.login();
        naukriService.checkForWalkInDrives();
    }
}
