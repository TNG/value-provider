package com.tngtech.valueprovider.example.instancioevaluation.enableddisabled;

import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@TestMethodOrder(MethodName.class)
@ExtendWith(InstancioExtension.class)
public class AllDisabledTestMethodsTest {
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
