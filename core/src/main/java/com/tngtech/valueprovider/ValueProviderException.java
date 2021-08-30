package com.tngtech.valueprovider;

import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY;
import static com.tngtech.valueprovider.ValueProviderFactory.getFormattedReferenceDateTime;
import static com.tngtech.valueprovider.ValueProviderFactory.getTestClassSeed;
import static com.tngtech.valueprovider.ValueProviderFactory.getTestMethodSeed;
import static java.lang.String.format;

public class ValueProviderException extends RuntimeException {
    ValueProviderException() {
        super(provideFailureReproductionInfo());
    }

    static String provideFailureReproductionInfo() {
        long testClassSeed = getTestClassSeed();
        long testMethodSeed = getTestMethodSeed();
        String referenceDateTime = getFormattedReferenceDateTime();
        return format(
                "If the failure is related to random ValueProviders, specify the following system properties for the JVM to reproduce:%n" +
                        "-D%s=%d%n" +
                        "-D%s=%d%n" +
                        "-D%s=%s",
                VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY, testClassSeed,
                VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY, testMethodSeed,
                VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY, referenceDateTime);
    }
}
