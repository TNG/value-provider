package com.tngtech.valueprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static com.tngtech.valueprovider.ValueProviderInitialization.createReproducibleInitialization;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

class ValueProviderInitializationTest {

    @Test
    void createRandomInitialization_should_create_mostly_different_data_for_multiple_invocations() throws InterruptedException {
        ValueProviderInitialization reference = createRandomInitialization();

        // required due to resolution of reference date/time truncated to seconds
        sleep(1200);

        List<ValueProviderInitialization> expectSomeDifferentData = new ArrayList<>();
        int numRetries = 10;
        boolean foundDifferent = false;
        for (int i = 0; i < numRetries; i++) {
            ValueProviderInitialization expectDifferentData = createRandomInitialization();
            expectSomeDifferentData.add(expectDifferentData);

            boolean differentRandom = !reference.getRandom().equals(expectDifferentData.getRandom());
            boolean differentSuffix = !reference.getSuffix().equals(expectDifferentData.getSuffix());
            boolean differentReferenceDateTime = !reference.getReferenceLocalDateTime().equals(expectDifferentData.getReferenceLocalDateTime());
            if (differentRandom && differentSuffix && differentReferenceDateTime) {
                foundDifferent = true;
                break;
            }
        }
        assertThat(foundDifferent)
                .as("Data different from %s expected in %s", reference, expectSomeDifferentData)
                .isTrue();
    }

    @Test
    void createReproducibleInitialization_should_create_same_data_for_multiple_invocations_with_same_seed() {
        long seed1 = new Random().nextLong();
        long seed2 = new Random().nextLong();
        ValueProviderInitialization reference1 = createReproducibleInitialization(seed1);
        ValueProviderInitialization reference2 = createReproducibleInitialization(seed2);

        assertThat(reference1).isEqualTo(createReproducibleInitialization(seed1));
        assertThat(reference2).isEqualTo(createReproducibleInitialization(seed2));
    }
}