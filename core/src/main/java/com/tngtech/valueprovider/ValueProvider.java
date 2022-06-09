package com.tngtech.valueprovider;

import java.time.LocalDateTime;

public class ValueProvider extends AbstractValueProvider<ValueProvider> {
    protected ValueProvider(ValueProviderInitialization initialization) {
        super(initialization);
    }

    protected ValueProvider(RandomValues random, String prefix, String suffix, LocalDateTime referenceLocalDateTime) {
        super(random, prefix, suffix, referenceLocalDateTime);
    }

    public static class Builder extends AbstractBuilder<ValueProvider, Builder> {
        public Builder(ValueProvider from) {
            super(from);
        }

        public Builder(long seed) {
            super(seed);
        }

        public Builder(RandomValues random) {
            super(random);
        }

        public Builder(ValueProviderInitialization initialization) {
            super(initialization);
        }

        @Override
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
