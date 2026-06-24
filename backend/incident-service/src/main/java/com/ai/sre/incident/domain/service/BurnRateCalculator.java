package com.ai.sre.incident.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * SLO Burn Rate Calculator — implements the Google SRE multi-window,
 * multi-burn-rate alerting strategy.
 *
 * Burn Rate = (actual error rate) / (allowed error rate)
 *   If burn_rate = 1.0 → consuming budget at exactly the expected rate
 *   If burn_rate = 14.4 → will exhaust 30-day budget in 2 hours
 *
 * Alert Strategy:
 *   FAST_BURN:  burn_rate > 14.4 for 1h  → Severity P1 (paging alert)
 *   SLOW_BURN:  burn_rate > 2.0  for 6h  → Severity P2 (ticket alert)
 *
 * Pure domain service.
 */
public class BurnRateCalculator {

    private static final Logger log = LoggerFactory.getLogger(BurnRateCalculator.class);

    public BurnRateResult calculate(double sloTarget,
                                     long observedGoodEvents,
                                     long observedTotalEvents,
                                     Duration windowDuration,
                                     int budgetWindowDays) {

        if (observedTotalEvents == 0) {
            return new BurnRateResult(0, 0, "NORMAL", 100.0, null, 0);
        }

        double allowedErrorRate = (100.0 - sloTarget) / 100.0;
        double observedErrorRate = 1.0 - ((double) observedGoodEvents / observedTotalEvents);
        double burnRate = (allowedErrorRate == 0) ? 0 : observedErrorRate / allowedErrorRate;

        double totalBudgetMinutes = budgetWindowDays * 24 * 60 * allowedErrorRate;
        double consumedBudgetMinutes = windowDuration.toMinutes() * observedErrorRate;
        double remainingBudgetPct = Math.max(0,
                ((totalBudgetMinutes - consumedBudgetMinutes) / totalBudgetMinutes) * 100);

        String alertLevel = determineAlertLevel(burnRate, windowDuration);

        Instant projectedExhaustion = null;
        if (burnRate > 1.0 && observedErrorRate > 0) {
            double remainingBudget = totalBudgetMinutes - consumedBudgetMinutes;
            double minutesUntilExhaustion = remainingBudget / observedErrorRate;
            projectedExhaustion = Instant.now().plusSeconds((long) (minutesUntilExhaustion * 60));
        }

        double observedSli = ((double) observedGoodEvents / observedTotalEvents) * 100;

        log.debug("Burn rate calculation: slo={}%, sli={}%, burnRate={}, alert={}, budgetRemaining={}%",
                sloTarget, String.format("%.3f", observedSli),
                String.format("%.2f", burnRate), alertLevel,
                String.format("%.1f", remainingBudgetPct));

        return new BurnRateResult(
                Math.round(burnRate * 100.0) / 100.0,
                Math.round(observedSli * 1000.0) / 1000.0,
                alertLevel,
                Math.round(remainingBudgetPct * 10.0) / 10.0,
                projectedExhaustion,
                Math.round(consumedBudgetMinutes * 100.0) / 100.0
        );
    }

    private String determineAlertLevel(double burnRate, Duration windowDuration) {
        long windowHours = windowDuration.toHours();
        if (windowHours <= 1 && burnRate > 14.4) return "FAST_BURN";
        if (windowHours <= 6 && burnRate > 2.0) return "SLOW_BURN";
        if (burnRate > 1.0) return "BUDGET_WARNING";
        return "NORMAL";
    }

    public record BurnRateResult(
            double burnRate,
            double currentSli,
            String alertLevel,
            double remainingBudgetPct,
            Instant projectedExhaustion,
            double consumedBudgetMinutes
    ) {
        public String toSeverity() {
            return switch (alertLevel) {
                case "FAST_BURN" -> "P1";
                case "SLOW_BURN" -> "P2";
                case "BUDGET_WARNING" -> "P3";
                default -> null;
            };
        }
    }
}
