package com.tngtech.valueprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.asList;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY;
import static com.tngtech.valueprovider.InitializationCreator.VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY;
import static java.lang.System.setProperty;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helper class for {@link ValueProviderFactory} rule/extension tests.
 */
public class ValueProviderAsserter {
    private static final Logger logger = LoggerFactory.getLogger(ValueProviderAsserter.class);

    private static final long TEST_CLASS_SEED = 1L;
    private static final long TEST_METHOD_SEED = 2L;

    /**
     * If tests using rule/extension are run together with others which do NOT use the ValueProviderClassRule, ValueProviderRule,
     * must ensure reproducible start-state all the same.
     * To avoid having to specify the properties on the JVM commandline, must ensure that they take effect,
     * regardless of the time when the rules/extensions are instantiated/invoked.
     * respective commandline options: -DVALUE_PROVIDER_FACTORY_TEST_CLASS_SEED=-5890316868874211342 -DVALUE_PROVIDER_FACTORY_TEST_METHOD_SEED=4419675984363176317
     */
    static void setSeedProperties() {
        setProperty(VALUE_PROVIDER_FACTORY_TEST_CLASS_SEED_PROPERTY, "" + TEST_CLASS_SEED);
        setProperty(VALUE_PROVIDER_FACTORY_TEST_METHOD_SEED_PROPERTY, "" + TEST_METHOD_SEED);
    }

    static void reinitializeTestClassSeed() {
        ValueProviderFactory.initializeTestClassSeed();
    }

    private RandomValuesSequence testClassSequence;
    private RandomValuesSequence testMethodSequence;
    private final List<ValueProvider> testClassRandomValues;
    private final List<ValueProvider> testMethodRandomValues;

    public ValueProviderAsserter() {
        testClassRandomValues = new ArrayList<>();
        testMethodRandomValues = new ArrayList<>();
    }

    public ValueProviderAsserter addExpectedTestClassRandomValues(ValueProvider valueProvider1, ValueProvider... furtherValueProviders) {
        return addExpectedTestClassRandomValues(asList(valueProvider1, furtherValueProviders));
    }

    public ValueProviderAsserter addExpectedTestClassRandomValues(List<ValueProvider> valueProviders) {
        testClassRandomValues.addAll(valueProviders);
        return this;
    }

    public ValueProviderAsserter assertAllTestClassRandomValues() {
        testClassSequence = new RandomValuesSequence(TEST_CLASS_SEED);
        return assertHasNextTestClassRandomValues(testClassRandomValues);
    }

    public ValueProviderAsserter addExpectedTestMethodRandomValues(ValueProvider valueProvider1, ValueProvider... furtherValueProviders) {
        return addExpectedTestMethodRandomValues(asList(valueProvider1, furtherValueProviders));
    }

    public ValueProviderAsserter addExpectedTestMethodRandomValues(List<ValueProvider> valueProviders) {
        testMethodRandomValues.addAll(valueProviders);
        return this;
    }

    public ValueProviderAsserter assertAllTestMethodRandomValues() {
        testMethodSequence = new RandomValuesSequence(TEST_METHOD_SEED);
        return assertHasNextTestMethodRandomValues(testMethodRandomValues);
    }

    @SuppressWarnings("UnusedReturnValue")
    public ValueProviderAsserter assertAllSuffixes() {
        List<ValueProvider> allProviders = new ArrayList<>(testClassRandomValues);
        allProviders.addAll(testMethodRandomValues);
        return assertSuffixes(allProviders);
    }

    private ValueProviderAsserter assertHasNextTestClassRandomValues(List<ValueProvider> providers) {
        providers.forEach(this::assertHasNextTestClassRandomValues);
        return this;
    }

    /**
     * Note that this method is coupled to the implementation of {@link InitializationCreator} wrt. {@link RandomValues} creation.
     * It does currently NOT support the extra cycles that may be required for unique VP suffixes,
     * as those can easily be avoided be appropriate initial seed values, at least for a small number of created VPs.
     */
    private ValueProviderAsserter assertHasNextTestClassRandomValues(ValueProvider provider) {
        assertThat(provider.getRandom()).isEqualTo(expectedNextTestClassRandomValues());
        return this;
    }

    private ValueProviderAsserter assertHasNextTestMethodRandomValues(List<ValueProvider> providers) {
        providers.forEach(this::assertHasNextTestMethodRandomValues);
        return this;
    }

    /**
     * @see #assertHasNextTestClassRandomValues(ValueProvider)
     */
    @SuppressWarnings("UnusedReturnValue")
    private ValueProviderAsserter assertHasNextTestMethodRandomValues(ValueProvider provider) {
        assertThat(provider.getRandom()).isEqualTo(expectedNextTestMethodRandomValues());
        return this;
    }

    private RandomValues expectedNextTestClassRandomValues() {
        return expectedNextRandomValues("test-CLASS", testClassSequence);
    }

    private RandomValues expectedNextTestMethodRandomValues() {
        return expectedNextRandomValues("test-METHOD", testMethodSequence);
    }

    private RandomValues expectedNextRandomValues(String testCycle, RandomValuesSequence sequence) {
        RandomValues random = sequence.nextRandomValues();
        logger.debug("expectedNextRandomValues({}), {}({})", testCycle, sequence.getSequenceCounter(), random.getSeed());
        return random;
    }

    private ValueProviderAsserter assertSuffixes(Collection<ValueProvider> valueProviders) {
        String[] expectedSuffixes = valueProviders.stream()
                .map(ValueProvider::getSuffix)
                .toArray(String[]::new);
        assertThat(ValueProviderFactory.instance().activeVpiCreator.suffixes).containsOnly(expectedSuffixes);
        return this;
    }
}
