package com.tngtech.valueprovider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.tngtech.valueprovider.ValueProviderAsserter.setSeedProperties;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ValueProviderRuleTest {
    static {
        ensureDefinedFactoryState();
    }

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    /**
     * @see ValueProviderAsserter#setSeedProperties()
     */
    private static void ensureDefinedFactoryState() {
        setSeedProperties();
    }

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
    public void identical_test_to_ensure_proper_handling_of_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestMethodRandomValues(
                        instanceRandom, beforeRandom, createRandomValueProvider(), createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}
