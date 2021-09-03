package com.tngtech.valueprovider;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.MILLIS;

class InitializationCreatorSnapshot implements TestCycleSnapshot {
    private static final ZoneOffset ZONE_OFFSET = UTC;
    static final TemporalUnit TIME_RESOLUTION = MILLIS;

    private final long seed;
    private final long referenceDateTimeEpochMills;
    private final long sequenceCounter;

    static LocalDateTime truncateToSupportedResolution(LocalDateTime input) {
        return input.truncatedTo(TIME_RESOLUTION);
    }

    InitializationCreatorSnapshot(long seed, LocalDateTime referenceDateTime, long sequenceCounter) {
        LocalDateTime truncatedToMilliseconds = truncateToSupportedResolution(referenceDateTime);
        checkArgument(referenceDateTime.equals(truncatedToMilliseconds),
                "Illegal referenceDateTime %s, must be truncated to milliseconds", referenceDateTime);
        this.seed = seed;
        this.referenceDateTimeEpochMills = referenceDateTime.toInstant(ZONE_OFFSET).toEpochMilli();
        this.sequenceCounter = sequenceCounter;
    }

    long getSeed() {
        return seed;
    }

    LocalDateTime getReferenceDateTime() {
        return LocalDateTime.ofInstant(ofEpochMilli(referenceDateTimeEpochMills), ZONE_OFFSET);
    }

    long getSequenceCounter() {
        return sequenceCounter;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        InitializationCreatorSnapshot that = (InitializationCreatorSnapshot) other;
        return seed == that.seed
                && referenceDateTimeEpochMills == that.referenceDateTimeEpochMills
                && sequenceCounter == that.sequenceCounter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seed, referenceDateTimeEpochMills, sequenceCounter);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" +
                "seed=" + seed +
                ", referenceDateTimeEpochMillis=" + referenceDateTimeEpochMills +
                ", sequenceCounter=" + sequenceCounter +
                ")";
    }
}
