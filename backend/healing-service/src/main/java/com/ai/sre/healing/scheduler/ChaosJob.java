package com.ai.sre.healing.scheduler;

import com.ai.sre.healing.engine.ChaosEngineeringModule;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChaosJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ChaosJob.class);

    private final ChaosEngineeringModule chaosEngineeringModule;

    public ChaosJob(ChaosEngineeringModule chaosEngineeringModule) {
        this.chaosEngineeringModule = chaosEngineeringModule;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String experimentType = context.getJobDetail().getJobDataMap().getString("experimentType");
        String targetService = context.getJobDetail().getJobDataMap().getString("targetService");
        String experimentName = context.getJobDetail().getJobDataMap().getString("experimentName");
        
        log.info("Executing scheduled Chaos Experiment [{}]: {} on {}", experimentName, experimentType, targetService);

        try {
            if ("NETWORK_LATENCY".equals(experimentType)) {
                chaosEngineeringModule.injectNetworkLatency(targetService, 500, 5);
            } else if ("POD_KILL".equals(experimentType)) {
                chaosEngineeringModule.killPod(targetService);
            } else {
                log.warn("Unknown chaos experiment type: {}", experimentType);
            }
        } catch (Exception e) {
            log.error("Error executing chaos experiment", e);
            throw new JobExecutionException(e);
        }
    }
}
