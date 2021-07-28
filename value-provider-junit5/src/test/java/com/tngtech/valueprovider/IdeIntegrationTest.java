package com.tngtech.valueprovider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Disabled // only intended for manual execution as all tests will fail
@ExtendWith(ValueProviderExtension.class)
class IdeIntegrationTest {
    @Test
    void should_show_seed_values_on_test_failure() {
        fail("simply failing");
    }

    @Test
    void should_show_seed_values_on_comparison_failure() {
        assertThat(1).isEqualTo(2);
    }
}
