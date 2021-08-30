package com.tngtech.valueprovider;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_REFERENCE_DATE_TIME_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY;
import static com.tngtech.valueprovider.ValueProviderFactory.getFormattedReferenceDateTime;
import static com.tngtech.valueprovider.ValueProviderFactory.getTestClassSeed;
import static com.tngtech.valueprovider.ValueProviderFactory.getTestMethodSeed;
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
}