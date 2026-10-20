package com.eventhub.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SampleScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(SampleScheduledJob.class);

    /**
     * Sample background job demonstrating scheduled tasks.
     * Runs once every hour (or configurable via cron in application.yml).
     */
    @Scheduled(cron = "${app.jobs.cleanup-cron:0 0 * * * *}")
    public void executeTask() {
        log.debug("[Scheduled Job] Executing periodic maintenance job at {}", LocalDateTime.now());
        // Future logic: Clean up expired sessions, archive past events, refresh cache, etc.
    }
}
