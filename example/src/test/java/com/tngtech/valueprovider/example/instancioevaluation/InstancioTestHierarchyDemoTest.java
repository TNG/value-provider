package com.tngtech.valueprovider.example.instancioevaluation;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.FIXED_SEED;
import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.createNumber;

class InstancioTestHierarchyDemoTest extends InstancioTestHierarchyDemoTestBase {
    private static final NumberWrapper classRandom = createNumber();

    private static NumberWrapper beforeAllRandom;

    private final NumberWrapper instanceRandom = createNumber();

    private NumberWrapper beforeEachRandom;

    @BeforeAll
    static void beforeAll() {
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
                // static data, NOT covered by InstancioExtension
                // baseClassRandom, baseBeforeAllRandom,
                // classRandom, beforeAllRandom
                // instance data, NOT covered by InstancioExtension
                // baseInstanceRandom, instanceRandom

                // instance data, covered by InstancioExtension
                baseBeforeEachRandom, beforeEachRandom,
                // test method data, covered by InstancioExtension
                createNumber(),
                createNumber()
        );
    }
}