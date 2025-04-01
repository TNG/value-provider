package com.tngtech.valueprovider.example.customprovider;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static org.assertj.core.api.Assertions.assertThat;

class CustomValueProviderTest {
    @Test
    void copyWithChangedPrefix_should_return_derived_ValueProvider() {
        CustomValueProvider customValues = new CustomValueProvider(createRandomInitialization());

        Object withChangedPrefix = customValues.copyWithChangedPrefix("newPrefix");

        assertThat(withChangedPrefix).isInstanceOf(CustomValueProvider.class);
    }

    @Test
    void collection_should_use_derived_ValueProvider() {
        CustomValueProvider customValues = new CustomValueProvider(createRandomInitialization());

        customValues.collection()
                .listOf(values -> {
                    assertThat(values).isInstanceOf(CustomValueProvider.class);
                    return 1;
                });
    }
}