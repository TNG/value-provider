package com.tngtech.valueprovider.example.customprovider;

import com.tngtech.valueprovider.ValueProviderInitialization;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProviderInitialization;
import static com.tngtech.valueprovider.ValueProviderFactory.createReproducibleValueProviderInitialization;

/**
 * Demo code for project specific customization of {@link com.tngtech.valueprovider.ValueProvider} functionality.
 * <p>
 * This factory opens the generic {@link com.tngtech.valueprovider.ValueProviderFactory} for project specific extensions
 * of {@link com.tngtech.valueprovider.ValueProvider}s while maintaining its functionality
 * to reproduce test failures in spite of using random data in tests.
 */
@SuppressWarnings("WeakerAccess")
public class CustomValueProviderFactory {
    private CustomValueProviderFactory() {
    }

    public static CustomValueProvider createRandomValueProvider() {
        ValueProviderInitialization randomInitialization = createRandomValueProviderInitialization();
        return new CustomValueProvider(randomInitialization);
    }

    public static CustomValueProvider createReproducibleValueProvider(long seed) {
        ValueProviderInitialization reproducibleInitialization = createReproducibleValueProviderInitialization(seed);
        return new CustomValueProvider(reproducibleInitialization);
    }
}
