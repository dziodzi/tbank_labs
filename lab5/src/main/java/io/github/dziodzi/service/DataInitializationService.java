package io.github.dziodzi.service;

import io.github.dziodzi.entity.Category;
import io.github.dziodzi.entity.Location;
import io.github.dziodzi.tools.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationService implements ApplicationListener<ApplicationStartedEvent> {

    private final CategoryService categoryService;
    private final LocationService locationService;
    private final APIClient apiClient;

    @Autowired
    private ExecutorService fixedThreadPool;

    @Autowired
    private ScheduledThreadPoolExecutor scheduledThreadPool;

    @Value("${custom.initialization.schedule}")
    private Duration initializationSchedule;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        initializeData();
        scheduleDataInitialization();
    }

    public void initializeData() {
        long startTime = System.currentTimeMillis();

        try {
            List<Future<?>> futures = new ArrayList<>();

            futures.add(fixedThreadPool.submit(() -> {
                log.info("-> Categories initialization started.");
                List<Category> categories = apiClient.fetchCategories();
                categoryService.initializeCategories(categories);
                log.info("--> Categories initialization finished.");
            }));

            futures.add(fixedThreadPool.submit(() -> {
                log.info("-> Locations initialization started.");
                List<Location> locations = apiClient.fetchLocations();
                locationService.initializeLocations(locations);
                log.info("--> Locations initialization finished.");
            }));

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    log.error("Failed to initialize data", e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize data", e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Data initialization completed in {} ms.", (endTime - startTime));
        }
    }

    private void scheduleDataInitialization() {
        scheduledThreadPool.scheduleWithFixedDelay(this::initializeData, initializationSchedule.toMillis(), initializationSchedule.toMillis(), TimeUnit.MILLISECONDS);
        log.info("Data will be re-initialized after {} s.", initializationSchedule.toSeconds());
    }
}
