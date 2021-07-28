package com.tngtech.valueprovider;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

import static com.tngtech.valueprovider.ValueProvider.Builder.createSuffix;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.time.temporal.ChronoUnit.SECONDS;

@SuppressWarnings("WeakerAccess")
public class ValueProviderInitialization {
    private static final int MAX_FIXED_VALUE_DATE_OFFSET_IN_DAYS = 100;
    private final RandomValues random;
    private final String suffix;
    private final LocalDateTime referenceLocalDateTime;

    public static ValueProviderInitialization createRandomInitialization() {
        RandomValues random = new RandomValues(new Random().nextLong());
        String suffix = createSuffix(random);
        LocalDateTime referenceLocalDateTime = now();
        return new ValueProviderInitialization(random, suffix, referenceLocalDateTime);
    }

    public static ValueProviderInitialization createReproducibleInitialization(long seed) {
        RandomValues random = new RandomValues(seed);
        String suffix = createSuffix(random);
        LocalDateTime fixedReferenceDateTime = parse("2013-01-01T00:00:00")
                .plusDays(seed % MAX_FIXED_VALUE_DATE_OFFSET_IN_DAYS);
        return new ValueProviderInitialization(random, suffix, fixedReferenceDateTime);
    }

    public ValueProviderInitialization(RandomValues random, String suffix, LocalDateTime referenceLocalDateTime) {
        this.random = random;
        this.suffix = suffix;
        this.referenceLocalDateTime = referenceLocalDateTime.truncatedTo(SECONDS);
    }

    public RandomValues getRandom() {
        return random;
    }

    public String getSuffix() {
        return suffix;
    }

    public LocalDateTime getReferenceLocalDateTime() {
        return referenceLocalDateTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(random, suffix, referenceLocalDateTime);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "(random=" + random
                + ", suffix=" + suffix
                + ", referenceLocalDateTime=" + referenceLocalDateTime
                + ")";
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
        ValueProviderInitialization that = (ValueProviderInitialization) obj;
        return Objects.equals(random, that.random)
                && Objects.equals(suffix, that.suffix)
                && Objects.equals(referenceLocalDateTime, that.referenceLocalDateTime);

    }
}
