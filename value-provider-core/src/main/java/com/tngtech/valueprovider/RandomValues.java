package com.tngtech.valueprovider;

import java.util.Random;

public class RandomValues {
    private final long seed;
    private final Random random;

    RandomValues(long seed) {
        this.seed = seed;
        random = new Random(seed);
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public int hashCode() {
        return (int) seed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(seed=" + seed + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RandomValues other = (RandomValues) obj;
        return seed == other.seed;
    }

    boolean nextBoolean() {
        return random.nextBoolean();
    }

    int nextInt(int bound) {
        return random.nextInt(bound);
    }

    double nextDouble() {
        return random.nextDouble();
    }

    long nextLong() {
        return random.nextLong();
    }
}
