package com.tngtech.valueprovider;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;

public class ValueProvider extends AbstractValueProvider<ValueProvider> {
    protected ValueProvider(ValueProviderInitialization initialization) {
        super(initialization);
    }

    protected ValueProvider(RandomValues random, String prefix, String suffix, LocalDateTime referenceLocalDateTime) {
        super(random, prefix, suffix, referenceLocalDateTime);
    }

    public static class Builder {
        private final RandomValues random;
        private String prefix;
        private String suffix;
        private LocalDateTime referenceLocalDateTime;

        public Builder(ValueProvider from) {
            random = from.random;
            prefix = from.prefix;
            suffix = from.suffix;
            referenceLocalDateTime = from.referenceLocalDateTime;
        }

        public Builder(long seed) {
            this(new RandomValues(seed));
        }

        public Builder(RandomValues random) {
            this.random = random;
            prefix = "";
            suffix = createSuffix(random);
            referenceLocalDateTime = now().truncatedTo(SECONDS);
        }

        public Builder(ValueProviderInitialization initialization) {
            this.random = initialization.getRandom();
            prefix = "";
            suffix = initialization.getSuffix();
            referenceLocalDateTime = initialization.getReferenceLocalDateTime().truncatedTo(SECONDS);
        }

        static String createSuffix(RandomValues random) {
            return randomCharacters(MIXED_CASE_STRING, SUFFIX_LENGTH, random);
        }

        public Builder withConstantPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder withConstantSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder withReferenceLocalDateTime(LocalDateTime referenceLocalDateTime) {
            this.referenceLocalDateTime = referenceLocalDateTime;
            return this;
        }

        public ValueProvider build() {
            return new ValueProvider(random, prefix, suffix, referenceLocalDateTime);
        }
    }

    /**
     * Create a copy of the {@link ValueProvider} {@code source}, i.e. with the same seed and suffix, but a different {@code prefix}.
     * By convention, the suffix is used to mark test data that belong together, and is therefore shared when creating a hierarchy of test objects.
     * The prefix is used to differentiate multiple instances of a certain kind of data.
     * @param source {@link ValueProvider} to be copied
     * @param prefix to use (replaces the one that is potentially set for {@code source})
     * @return copy of {@link ValueProvider} with same seed an suffix, but changed {@code prefix}.
     */
    public static ValueProvider copyWithChangedPrefix(ValueProvider source, String prefix) {
        return new ValueProvider.Builder(source)
                .withConstantPrefix(prefix)
                .build();
    }
}
