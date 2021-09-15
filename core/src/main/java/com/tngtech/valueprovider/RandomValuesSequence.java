package com.tngtech.valueprovider;

import java.util.Objects;

public class RandomValuesSequence {
    private final RandomValues random;
    private long sequenceCounter;

    RandomValuesSequence(long seed) {
        random = new RandomValues(seed);
        sequenceCounter = 0L;
    }

    RandomValues nextRandomValues() {
        sequenceCounter++;
        return new RandomValues(random.nextLong());
    }

    long getSequenceCounter() {
        return sequenceCounter;
    }

    long getSeed() {
        return random.getSeed();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        RandomValuesSequence that = (RandomValuesSequence) other;
        return Objects.equals(sequenceCounter, that.sequenceCounter)
                && Objects.equals(random, that.random);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceCounter, random);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(random=" + random + ", sequenceCounter=" + sequenceCounter + ")";
    }
}
