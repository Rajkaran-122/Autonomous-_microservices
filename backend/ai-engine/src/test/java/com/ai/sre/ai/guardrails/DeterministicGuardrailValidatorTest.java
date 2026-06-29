package com.ai.sre.ai.guardrails;

import com.ai.sre.common.event.AnalysisEvent.Recommendation;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DeterministicGuardrailValidatorTest {

    private final DeterministicGuardrailValidator validator = new DeterministicGuardrailValidator();

    @Test
    void testScaleUp_ValidReplicas() {
        Recommendation rec = new Recommendation("SCALE_UP_PODS", "payment-service", "desc", 90, Map.of("replicas", "5"));
        String result = validator.validate(rec);
        assertNull(result, "Should not block valid scale up");
    }

    @Test
    void testScaleUp_ExceedsMaxReplicas() {
        Recommendation rec = new Recommendation("SCALE_UP_PODS", "payment-service", "desc", 90, Map.of("replicas", "15"));
        String result = validator.validate(rec);
        assertNotNull(result);
        assertTrue(result.contains("exceeds maximum hard limit of 10"));
    }

    @Test
    void testPodRestart_ValidTarget() {
        Recommendation rec = new Recommendation("POD_RESTART", "auth-service", "desc", 90, Map.of());
        String result = validator.validate(rec);
        assertNull(result, "Should not block valid target");
    }

    @Test
    void testPodRestart_InvalidTarget() {
        Recommendation rec = new Recommendation("POD_RESTART", "kube-system", "desc", 90, Map.of());
        String result = validator.validate(rec);
        assertNotNull(result);
        assertTrue(result.contains("must end in '-service'"));
    }

    @Test
    void testRollback_ForbiddenTarget() {
        Recommendation rec = new Recommendation("ROLLBACK", "core-db", "desc", 90, Map.of());
        String result = validator.validate(rec);
        assertNotNull(result);
        assertTrue(result.contains("strictly forbidden"));
    }
}
