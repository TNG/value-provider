package com.tngtech.valueprovider.example.customprovider;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static org.assertj.core.api.Assertions.assertThat;

class CustomValueProviderTest {
    @Test
    void copyWithChangedPrefix_should_return_derived_ValueProvider() {
        CustomValueProvider customSource = new CustomValueProvider(createRandomInitialization());

        Object withChangedPrefix = customSource.copyWithChangedPrefix("newPrefix");

        assertThat(withChangedPrefix).isInstanceOf(CustomValueProvider.class);
    }
}