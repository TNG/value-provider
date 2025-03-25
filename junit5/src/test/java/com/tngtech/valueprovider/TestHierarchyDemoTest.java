package com.tngtech.valueprovider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

class TestHierarchyDemoTest extends TestHierarchyDemoTestBase {
    private static final ValueProvider classRandom = createRandomValueProvider();

    private static ValueProvider beforeAllRandom;

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeEachRandom;

    @BeforeAll
    static void beforeAll() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createRandomValueProvider();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation_in_base_and_derived_test_classes() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(baseClassRandom, baseBeforeAllRandom, classRandom, beforeAllRandom)
                .addExpectedTestMethodRandomValues(baseInstanceRandom, instanceRandom,
                        baseBeforeEachRandom, beforeEachRandom, createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}