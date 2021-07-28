package com.tngtech.valueprovider;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Ignore // only intended for manual execution as all tests will fail
public class IdeIntegrationTest {
    @Rule
    public ValueProviderRule valueProviderRule = new ValueProviderRule();

    @Test
    public void should_show_seed_values_on_test_failure() {
        fail("simply failing");
    }

    @Test
    public void should_show_seed_values_on_comparison_failure() {
        assertThat(1).isEqualTo(2);
    }
}
