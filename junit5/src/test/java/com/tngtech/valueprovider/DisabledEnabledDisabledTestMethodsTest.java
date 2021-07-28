package com.tngtech.valueprovider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodName.class)
@ExtendWith(ValueProviderExtension.class)
public class DisabledEnabledDisabledTestMethodsTest {
    @Disabled
    @Test
    void a_test_disabled() {
    }

    @Test
    void b_test_enabled() {
    }

    @Disabled
    @Test
    void c_test_disabled() {
    }
}
