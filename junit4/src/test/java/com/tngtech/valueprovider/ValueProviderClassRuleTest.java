package com.tngtech.valueprovider;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static com.tngtech.valueprovider.ValueProviderAsserter.setSeedProperties;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ValueProviderClassRuleTest {

    static {
        ensureDefinedFactoryState();
    }

    /**
     * @see ValueProviderAsserter#setSeedProperties()
     */
    private static void ensureDefinedFactoryState() {
        setSeedProperties();
    }

    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();
    private static final ValueProvider classRandom1 = createRandomValueProvider();
    private static final ValueProvider classRandom2 = createRandomValueProvider();

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeRandom;

    @Before
    public void createBeforeRandom() {
        beforeRandom = createRandomValueProvider();
    }

    @Test
    public void should_ensure_reproducible_ValueProvider_creation() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    public void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2)
                .addExpectedTestMethodRandomValues(
                        instanceRandom, beforeRandom, createRandomValueProvider(), createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}
