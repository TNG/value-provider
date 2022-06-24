package com.tngtech.valueprovider.example.customprovider;

import java.time.LocalDateTime;

import com.tngtech.valueprovider.AbstractValueProvider;
import com.tngtech.valueprovider.RandomValues;
import com.tngtech.valueprovider.ValueProviderInitialization;

/**
 * @see CustomValueProviderFactory
 */
@SuppressWarnings("WeakerAccess")
public class CustomValueProvider extends AbstractValueProvider<CustomValueProvider> {
    CustomValueProvider(ValueProviderInitialization initialization) {
        super(initialization);
    }

    private CustomValueProvider(RandomValues random, String prefix, String suffix, LocalDateTime referenceLocalDateTime) {
        super(random, prefix, suffix, referenceLocalDateTime);
    }

    @Override
    protected Builder toBuilder(CustomValueProvider from) {
        return new Builder(from);
    }

    /**
     * Just for demonstration purposes, a project-specific method to provide syntactically valid AGS values
     * (as used by German municipial administration).
     */
    public String myCustomValue() {
        return String.format("%02d%s", intNumber(1, 16), numericString(6));
    }

    static class Builder extends AbstractBuilder<CustomValueProvider, Builder> {

        protected Builder(CustomValueProvider from) {
            super(from);
        }

        @Override
        public CustomValueProvider build() {
            return new CustomValueProvider(random, prefix, suffix, referenceLocalDateTime);
        }
    }
}
