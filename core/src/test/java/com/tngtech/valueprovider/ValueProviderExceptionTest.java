package com.tngtech.valueprovider;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.InitializationCreator.*;
import static com.tngtech.valueprovider.ValueProviderFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

class ValueProviderExceptionTest {
    @Test
    void should_show_seed_values_reference_date_time_and_respective_system_properties_for_failure_reproduction() {
        ValueProviderException exception = new ValueProviderException();

        String message = exception.getMessage();

        long testClassSeed = getTestClassSeed();
        long testMethodSeed = getTestMethodSeed();
        String referenceDateTime = getFormattedReferenceDateTime();
        assertThat(message)
                .contains("" + testClassSeed, "" + testMethodSeed, referenceDateTime)
                .contains(VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY,
                        VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY,
                        VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY);
    }

    @Test
    void should_show_test_class_to_re_run_for_failure_reproduction_if_provided() {
        Class<?> testClassToReRun = this.getClass();
        ValueProviderException exception = new ValueProviderException(Optional.of(testClassToReRun));

        String message = exception.getMessage();

        assertThat(message).contains(testClassToReRun.getName());
    }
}