package com.tngtech.valueprovider;

import java.time.LocalDateTime;

public class ValueProvider extends AbstractValueProvider<ValueProvider> {
    protected ValueProvider(ValueProviderInitialization initialization) {
        super(initialization);
    }

    protected ValueProvider(RandomValues random, String prefix, String suffix, LocalDateTime referenceLocalDateTime) {
        super(random, prefix, suffix, referenceLocalDateTime);
    }

    @Override
    protected Builder toBuilder(ValueProvider from) {
        return new Builder(from);
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
     * @deprecated use {@link AbstractValueProvider#copyWithChangedPrefix(String)} instead to allow copying a custom {@link ValueProvider} as well.
     * @see AbstractValueProvider#copyWithChangedPrefix(String)
     */
    @Deprecated
    public static ValueProvider copyWithChangedPrefix(ValueProvider source, String prefix) {
        return source.copyWithChangedPrefix(prefix);
    }
}
