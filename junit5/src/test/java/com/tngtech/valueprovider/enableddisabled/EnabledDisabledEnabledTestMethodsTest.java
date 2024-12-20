package com.tngtech.valueprovider.enableddisabled;

import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodName.class)
@ExtendWith(ValueProviderExtension.class)
public class EnabledDisabledEnabledTestMethodsTest {
    @Test
    void a_test_enabled() {
    }

    @Disabled
    @Test
    void b_test_disabled() {
    }

    @Test
    void c_test_enabled() {
    }
}
