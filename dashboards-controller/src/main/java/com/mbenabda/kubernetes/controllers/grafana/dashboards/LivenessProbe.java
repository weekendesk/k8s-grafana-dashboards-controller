package com.mbenabda.kubernetes.controllers.grafana.dashboards;

import java.time.Duration;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static java.time.Duration.ofSeconds;
import static java.util.Objects.requireNonNull;

public class LivenessProbe {
    private static final Logger LOGGER = Logger.getLogger(LivenessProbe.class.getSimpleName());
    private static final Duration _10_SECONDS = ofSeconds(10);
    private static final int MAX_OF_3_RETRIES = 3;

    private final Duration initialDelay;
    private final Duration period;
    private final Integer failureThreshold;
    private final Supplier<Boolean> check;
    private Integer trials;
    private Boolean isAlive;

    /*
        initialDelaySeconds: Number of seconds before liveness probe is initiated.
        periodSeconds: How often to perform the probe. Default to 10 seconds. Minimum value is 1.
        failureThreshold: Number of trials performed before giving up. Defaults to 3. Minimum value is 1.
     */
    public LivenessProbe(Duration initialDelay, Duration period, Integer failureThreshold, Supplier<Boolean> check) {
        this.initialDelay = requireNonNull(initialDelay);
        this.period = requireAtLeast(
                requireNonNull(period),
                ofSeconds(1),
                "period must be >= 1 second"
        );
        this.failureThreshold = requireAtLeast(
                requireNonNull(failureThreshold),
                1,
                "failureThreshold must be >= 1"
        );
        this.check = requireNonNull(check);
        this.trials = 0;
    }

    public LivenessProbe(Duration initialDelay, Supplier<Boolean> check) {
        this(initialDelay, _10_SECONDS, MAX_OF_3_RETRIES, check);
    }

    public LivenessProbe(Supplier<Boolean> check) {
        this(_10_SECONDS, _10_SECONDS, MAX_OF_3_RETRIES, check);
    }

    public void waitForCompletion() throws InterruptedException {
        LOGGER.info(format("Waiting for initial delay of %s", initialDelay));
        sleep(initialDelay.toMillis());

        do {
            trials++;
            LOGGER.info(format("Peforming trial %d/%d", trials, failureThreshold));
            isAlive = check.get();

            if(isAlive) {
                break;
            }

            sleep(period.toMillis());
        } while(!giveUp());
    }

    private boolean giveUp() {
        return trials >= failureThreshold;
    }

    private <T extends Comparable<T>> T requireAtLeast(T value, T min, String errorMessage) {
        checkArgument(value.compareTo(min) >= 0 , errorMessage);
        return value;
    }

    public Boolean isAlive() {
        return isAlive;
    }
}
