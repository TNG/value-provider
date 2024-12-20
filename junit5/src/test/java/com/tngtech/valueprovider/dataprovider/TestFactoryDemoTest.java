package com.tngtech.valueprovider.dataprovider;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@ExtendWith(ValueProviderExtension.class)
class TestFactoryDemoTest {
    private static final ValueProvider classRandom;

    static {
        ensureDefinedFactoryState();
        classRandom = createRandomValueProvider();
    }

    private static ValueProvider beforeAllRandom;

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeEachRandom;

    @BeforeAll
    static void createBeforeAllRandom() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void createBeforeEachRandom() {
        beforeEachRandom = createRandomValueProvider();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation() {
        new AssertionContext(this)
                .verifyReproducibleForTestLifecycle();
    }

    @Test
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        new AssertionContext(this)
                .verifyReproducibleForTestLifecycle();
    }

    @TestFactory
    List<DynamicTest> should_ensure_reproducible_ValueProvider_creation_for_TestFactory() {
        AssertionContext asserter = new AssertionContext(this);
        return ImmutableList.of(
                verifyReproducibleForFirstTestFactoryTest("TestFactory11", asserter),
                verifyReproducibleForSubsequentTestFactoryTest("TestFactory12", asserter),
                verifyReproducibleForSubsequentTestFactoryTest("TestFactory13", asserter));
    }

    @TestFactory
    List<DynamicTest> identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles_for_TestFactory() {
        AssertionContext asserter = new AssertionContext(this);
        return ImmutableList.of(
                verifyReproducibleForFirstTestFactoryTest("TestFactory21", asserter),
                verifyReproducibleForSubsequentTestFactoryTest("TestFactory22", asserter),
                verifyReproducibleForSubsequentTestFactoryTest("TestFactory23", asserter));
    }

    /**
     * Note that test lifecycle is only executed per {@link TestFactory}, but NOT per each {@link DynamicTest}
     * (see e.g. https://junit.org/junit5/docs/snapshot/user-guide/#writing-tests-dynamic-tests).
     */
    private static DynamicTest verifyReproducibleForFirstTestFactoryTest(String name, AssertionContext asserter) {
        return dynamicTest(name, asserter::verifyReproducibleForTestLifecycle);
    }

    private static DynamicTest verifyReproducibleForSubsequentTestFactoryTest(String name, AssertionContext asserter) {
        return dynamicTest(name, asserter::verifyReproducibleForTestMethod);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static class AssertionContext {
        private final TestFactoryDemoTest testInstance;
        private final ValueProviderAsserter asserter = new ValueProviderAsserter();

        private AssertionContext(TestFactoryDemoTest testInstance) {
            this.testInstance = testInstance;
        }

        private AssertionContext verifyReproducibleForTestLifecycle() {
            return addExpectedForTestLifecycle()
                    .addExpectedForTestMethod()
                    .verifyAll();
        }

        private AssertionContext verifyReproducibleForTestMethod() {
            return addExpectedForTestMethod()
                    .verifyAll();
        }

        private AssertionContext addExpectedForTestLifecycle() {
            asserter.addExpectedTestClassRandomValues(classRandom, beforeAllRandom)
                    .addExpectedTestMethodRandomValues(testInstance.instanceRandom, testInstance.beforeEachRandom);
            return this;
        }

        private AssertionContext addExpectedForTestMethod() {
            asserter.addExpectedTestMethodRandomValues(createRandomValueProvider(), createRandomValueProvider());
            return this;
        }

        private AssertionContext verifyAll() {
            asserter.assertAllTestClassRandomValues()
                    .assertAllTestMethodRandomValues()
                    .assertAllSuffixes();
            return this;
        }
    }
}