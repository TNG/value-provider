package com.tngtech.valueprovider.example.instancioevaluation.enableddisabled;

import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * No need for different sequences of enabled/disabled test methods
 * like for {@link Lifecycle#PER_METHOD},
 * as test method cycle is started once in intercepted constructor,
 * and only finished after last test method.
 *
 * @see DisabledEnabledDisabledTestMethodsTest
 * @see EnabledDisabledEnabledTestMethodsTest
 */
@TestInstance(PER_CLASS)
@ExtendWith(InstancioExtension.class)
class LifecyclePerClassAllDisabledTestMethodsTest {
    @Disabled
    @Test
    void a_test_disabled() {
    }

    @Disabled
    @Test
    void b_test_disabled() {
    }

    @Disabled
    @Test
    void c_test_disabled() {
    }
}
