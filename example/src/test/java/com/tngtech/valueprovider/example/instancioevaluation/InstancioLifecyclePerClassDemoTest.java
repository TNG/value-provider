package com.tngtech.valueprovider.example.instancioevaluation;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.FIXED_SEED;
import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.createNumber;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodName.class)
@ExtendWith(InstancioExtension.class)
class InstancioLifecyclePerClassDemoTest {
    private final NumberWrapper instanceRandom = createNumber();
    private NumberWrapper beforeAllRandom;

    private NumberWrapper beforeEachRandom;

    @BeforeAll
    void beforeAll() {
        beforeAllRandom = createNumber();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createNumber();
    }

    @Seed(FIXED_SEED)
    @Test
    void should_ensure_reproducible_data_creation_in_base_and_derived_test_classes() {
        verifyReproducibleDataCreation();
    }

    @Seed(FIXED_SEED)
    @Test
    void identical_test_to_ensure_proper_test_lifecycle_handling() {
        verifyReproducibleDataCreation();
    }

    private void verifyReproducibleDataCreation() {
        NumberWrapper.verifyReproducibleDataCreation(
                // instance data, NOT covered by InstancioExtension
                // instanceRandom,
                // beforeAllRandom

                // instance data, covered by InstancioExtension
                beforeEachRandom,
                // test method data, covered by InstancioExtension
                createNumber(),
                createNumber()
        );
    }
}