package com.tngtech.valueprovider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;

import static com.tngtech.valueprovider.ValueProviderAsserter.setSeedProperties;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ValueProviderClassRuleTestHierarchyTestBase {

    static {
        ensureDefinedFactoryState();
    }

    private static void ensureDefinedFactoryState() {
        setSeedProperties();
    }

    // MUST be in base class, if base class creates VPs during static initialization
    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();

    static final ValueProvider baseClassRandom = createRandomValueProvider();

    static ValueProvider baseBeforeClassRandom;

    // MUST be in base class, if base class creates VPs during instance initialization
    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    ValueProvider baseInstanceRandom = createRandomValueProvider();

    ValueProvider baseBeforeRandom;

    @BeforeClass
    public static void baseBeforeClass() {
        baseBeforeClassRandom = createRandomValueProvider();
    }

    @Before
    public void baseBefore() {
        baseBeforeRandom = createRandomValueProvider();
    }

}