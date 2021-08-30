package com.tngtech.valueprovider;

/**
 * Marker interface to allow passing a test (class) cycle between {@link ValueProviderFactory} and test infrastructure.
 * Required for tests using the (JUnit4) DataProvider runner, which loads all test classes in its test discovery phase
 * and executes the DataProvider methods BEFORE ANY test is actually executed.
 * This, in turn, requires the test class cycle data to be created when the test class is loaded,
 * and reused as soon as the test is executed (starting-Method of the ClassRule).
 * For details, please refer to the UML sequence diagrams in the doc folder.
 */
public interface TestCycle {
}
