package com.tngtech.valueprovider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ValueProviderClassRuleTestHierarchyTest extends ValueProviderClassRuleTestHierarchyTestBase {
    private static final ValueProvider classRandom = createRandomValueProvider();

    private static ValueProvider beforeClassRandom;

    @BeforeClass
    public static void createBeforeClassRandom() {
        beforeClassRandom = createRandomValueProvider();
    }

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeRandom;

    @Before
    public void createBeforeRandom() {
        beforeRandom = createRandomValueProvider();
    }

    @Test
    public void should_ensure_reproducible_ValueProvider_creation_in_base_and_derived_test_classes() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    public void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(
                        baseClassRandom, baseBeforeClassRandom, classRandom, beforeClassRandom)
                .addExpectedTestMethodRandomValues(
                        baseInstanceRandom, instanceRandom, baseBeforeRandom, beforeRandom, createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}