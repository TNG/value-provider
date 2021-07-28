package com.tngtech.valueprovider;

import static com.tngtech.valueprovider.ValueProviderAsserter.reinitializeTestClassSeed;
import static com.tngtech.valueprovider.ValueProviderAsserter.setSeedProperties;

public final class JUnit5Tests {
    /**
     * To avoid having to specify the properties on the JVM commandline, must ensure that they take effect,
     * as the {@link ValueProviderExtension}'s beforeAll() method has already run
     * BEFORE this method is invoked as part of the test class loading.
     * This, in turn, requires to invoke {@link ValueProviderAsserter#reinitializeTestClassSeed()}
     *
     * @see ValueProviderAsserter#setSeedProperties()
     */
    public static void ensureDefinedFactoryState() {
        setSeedProperties();
        reinitializeTestClassSeed();
    }
}
