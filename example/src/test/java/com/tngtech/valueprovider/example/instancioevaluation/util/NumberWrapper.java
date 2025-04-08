package com.tngtech.valueprovider.example.instancioevaluation.util;

import lombok.Value;
import org.instancio.Instancio;

import static org.assertj.core.api.Assertions.assertThat;

@Value
public class NumberWrapper {
    public static final long FIXED_SEED = 12341234L;
    public static final long[] NUMBERS = {361L, 18L, 6854L, 5476L};

    public static NumberWrapper createNumber() {
        return Instancio.create(NumberWrapper.class);
    }
    long number;

    public static void verifyReproducibleDataCreation(NumberWrapper... randoms) {
        for (int i = 0; i < randoms.length; i++) {
            randoms[i].assertNumber(i);
        }
    }

    public void assertNumber(int indexOfExpected) {
        assertThat(getNumber()).as("for index %d", indexOfExpected).isEqualTo(NUMBERS[indexOfExpected]);
    }
}
