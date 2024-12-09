package com.tngtech.valueprovider;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;

import static com.tngtech.valueprovider.InitializationCreator.*;
import static com.tngtech.valueprovider.ValueProviderFactory.*;
import static java.lang.String.format;
import static java.util.Optional.empty;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ValueProviderException extends RuntimeException {
    ValueProviderException() {
        this(empty());
    }

    ValueProviderException(Optional<Class<?>> testClassToReRunForReproduction) {
        super(provideFailureReproductionInfo(testClassToReRunForReproduction));
    }

    @VisibleForTesting
    static String provideFailureReproductionInfo(Optional<Class<?>> testClassToReRunForReproduction) {
        long testClassSeed = getTestClassSeed();
        long testMethodSeed = getTestMethodSeed();
        String referenceDateTime = getFormattedReferenceDateTime();
        return format(
                "If the failure is related to random ValueProviders, %sspecify the following system properties for the JVM to reproduce:%n" +
                        "-D%s=%d%n" +
                        "-D%s=%d%n" +
                        "-D%s=%s",
                formatReRunMessageFor(testClassToReRunForReproduction),
                VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY, testClassSeed,
                VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY, testMethodSeed,
                VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY, referenceDateTime);
    }

    private static String formatReRunMessageFor(Optional<Class<?>> testClassToReRunForReproduction) {
        return testClassToReRunForReproduction.map(testClass ->
                        format("re-run all tests of '%s' and ", testClass.getName()))
                .orElse("");
    }
}
