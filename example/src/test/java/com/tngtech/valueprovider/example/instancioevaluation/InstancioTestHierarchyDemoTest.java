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
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleDataCreation();
    }

    private void verifyReproducibleDataCreation() {
        // static data, NOT covered by InstancioExtension
        //        baseClassRandom.assertNumber(42L);
        //        baseBeforeAllRandom.assertNumber(42L);
        //        classRandom.assertNumber(42L);
        //        beforeAllRandom.assertNumber(42L);

        // instance data, NOT covered by InstancioExtension
        //        baseInstanceRandom.assertNumber(42L);
        //        instanceRandom.assertNumber(42L);

        // instance data, covered by InstancioExtension
        baseBeforeEachRandom.assertNumber(0);
        beforeEachRandom.assertNumber(1);

        // test method data, covered by InstancioExtension
        createNumber().assertNumber(2);
        createNumber().assertNumber(3);
    }
}