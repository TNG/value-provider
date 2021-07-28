package com.tngtech.valueprovider;

/**
 * Marker interface to allow passing the minimal data to initialize a test (class) cycle between {@link ValueProviderFactory} and test infrastructure.
 * Required for tests using the (JUnit4) DataProvider runner.
 * For details, please refer to the {@link TestCycle} documentation or the UML sequence diagrams in the doc folder.
 */
public interface TestCycleSnapshot {
}
