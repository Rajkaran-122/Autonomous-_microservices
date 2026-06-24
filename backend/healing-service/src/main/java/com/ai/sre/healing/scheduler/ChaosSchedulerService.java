package com.ai.sre.healing.scheduler;

import com.ai.sre.healing.model.ChaosExperimentEntity;
import com.ai.sre.healing.repository.ChaosExperimentRepository;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChaosSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ChaosSchedulerService.class);

    private final Scheduler scheduler;
    private final ChaosExperimentRepository repository;

    public ChaosSchedulerService(Scheduler scheduler, ChaosExperimentRepository repository) {
        this.scheduler = scheduler;
        this.repository = repository;
    }

    @PostConstruct
    public void scheduleActiveExperiments() {
        try {
            log.info("Initializing Chaos Engineering Scheduler...");
            List<ChaosExperimentEntity> activeExperiments = repository.findByEnabledTrueAndCronExpressionIsNotNull();
            
            for (ChaosExperimentEntity exp : activeExperiments) {
                scheduleExperiment(exp);
            }
            log.info("Successfully scheduled {} active chaos experiments.", activeExperiments.size());
        } catch (Exception e) {
            log.error("Failed to initialize Chaos Scheduler", e);
        }
    }

    private void scheduleExperiment(ChaosExperimentEntity exp) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(ChaosJob.class)
                .withIdentity(exp.getId().toString(), "chaos-experiments")
                .usingJobData("experimentType", exp.getExperimentType())
                // In a real system, we'd look up the target service name via ID from a ServiceRepository
                // For simplicity here, we assume targetNamespace can hold the service name or we use a hardcoded value 
                // if targetServiceId isn't resolved to a name. We will use the experiment name or namespace as a proxy.
                .usingJobData("targetService", exp.getTargetNamespace() != null ? exp.getTargetNamespace() : "default-service")
                .usingJobData("experimentName", exp.getName())
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(exp.getId().toString() + "-trigger", "chaos-experiments")
                .withSchedule(CronScheduleBuilder.cronSchedule(exp.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.debug("Scheduled chaos experiment: {} with cron: {}", exp.getName(), exp.getCronExpression());
    }
}
