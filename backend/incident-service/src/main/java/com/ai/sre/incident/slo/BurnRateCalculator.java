package com.ai.sre.incident.slo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
 * This is a pure domain service — no Spring/framework dependencies.
 */
@Service
public class BurnRateCalculator {

    private static final Logger log = LoggerFactory.getLogger(BurnRateCalculator.class);

    /**
     * Calculates burn rate for a given SLO and observed error rate.
     *
     * @param sloTarget         The SLO target (e.g., 99.95 for 99.95%)
     * @param observedGoodEvents Number of good events in the measurement window
     * @param observedTotalEvents Number of total events in the measurement window
     * @param windowDuration     Duration of the measurement window
     * @param budgetWindowDays   Total budget window (e.g., 30 days)
     * @return BurnRateResult with burn rate, alert level, and budget forecast
     */
    public BurnRateResult calculate(double sloTarget,
                                     long observedGoodEvents,
                                     long observedTotalEvents,
                                     Duration windowDuration,
                                     int budgetWindowDays) {

        if (observedTotalEvents == 0) {
            return new BurnRateResult(0, 0, "NORMAL", 100.0, null, 0);
        }

        // Step 1: Calculate allowed error rate from SLO
        double allowedErrorRate = (100.0 - sloTarget) / 100.0; // e.g., 0.0005 for 99.95%

        // Step 2: Calculate observed error rate
        double observedErrorRate = 1.0 - ((double) observedGoodEvents / observedTotalEvents);

        // Step 3: Calculate burn rate
        double burnRate = (allowedErrorRate == 0) ? 0 : observedErrorRate / allowedErrorRate;

        // Step 4: Calculate budget consumption
        double totalBudgetMinutes = budgetWindowDays * 24 * 60 * allowedErrorRate;
        double consumedBudgetMinutes = windowDuration.toMinutes() * observedErrorRate;
        double remainingBudgetPct = Math.max(0,
                ((totalBudgetMinutes - consumedBudgetMinutes) / totalBudgetMinutes) * 100);

        // Step 5: Determine alert level using Google SRE thresholds
        String alertLevel = determineAlertLevel(burnRate, windowDuration);

        // Step 6: Project budget exhaustion
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

    /**
     * Google SRE multi-window burn rate alert thresholds.
     */
    private String determineAlertLevel(double burnRate, Duration windowDuration) {
        long windowHours = windowDuration.toHours();

        // Fast burn: short window (≤1h), high burn rate → P1 paging
        if (windowHours <= 1 && burnRate > 14.4) return "FAST_BURN";

        // Slow burn: medium window (≤6h), moderate burn rate → P2 ticket
        if (windowHours <= 6 && burnRate > 2.0) return "SLOW_BURN";

        // Budget exhaustion warning
        if (burnRate > 1.0) return "BUDGET_WARNING";

        return "NORMAL";
    }

    // ==================== Result Record ====================

    public record BurnRateResult(
            double burnRate,
            double currentSli,
            String alertLevel,      // FAST_BURN, SLOW_BURN, BUDGET_WARNING, NORMAL
            double remainingBudgetPct,
            Instant projectedExhaustion,
            double consumedBudgetMinutes
    ) {
        /**
         * Maps alert level to incident severity.
         */
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
