package com.tngtech.valueprovider;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomValuesSequenceTest {
    @Test
    void should_create_reproducible_sequence_of_RandomValues() {
        RandomValuesSequence sequence = new RandomValuesSequence(42L);
        RandomValuesSequence sameSeed = new RandomValuesSequence(42L);

        assertThat(sequence.nextRandomValues()).isEqualTo(sameSeed.nextRandomValues());
        assertThat(sequence.nextRandomValues()).isEqualTo(sameSeed.nextRandomValues());
        assertThat(sequence.nextRandomValues()).isEqualTo(sameSeed.nextRandomValues());
        assertThat(sequence.nextRandomValues()).isEqualTo(sameSeed.nextRandomValues());
    }

    @Test
    void sequenceCounter_should_be_incremented_for_every_created_RandomValues() {
        RandomValuesSequence sequence = new RandomValuesSequence(42L);

        assertThat(sequence.getSequenceCounter()).isEqualTo(0L);
        sequence.nextRandomValues();
        assertThat(sequence.getSequenceCounter()).isEqualTo(1L);
        sequence.nextRandomValues();
        assertThat(sequence.getSequenceCounter()).isEqualTo(2L);
        sequence.nextRandomValues();
        assertThat(sequence.getSequenceCounter()).isEqualTo(3L);
    }
}