package com.tngtech.valueprovider.dataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.JUnit5Tests.asListWithoutNulls;
import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodName.class)
@ExtendWith({ValueProviderExtension.class})
class LifecyclePerClassParameterizedTestDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(LifecyclePerClassParameterizedTestDemoTest.class);
    private static final ValueProvider classRandom1;
    private static final ValueProvider classRandom2;

    static {
        logger.debug("{}: static initialization", LifecyclePerClassParameterizedTestDemoTest.class.getSimpleName());
        ensureDefinedFactoryState();
        classRandom1 = createRandomValueProvider();
        classRandom2 = createRandomValueProvider();
    }

    private final ValueProvider instanceRandom = createRandomValueProvider();
    private ValueProvider beforeAllRandom;
    private ValueProvider methodSourceRandom;
    private ValueProvider beforeEachRandom;
    private ValueProvider methodRandom;

    private final List<ValueProvider> randomsOfPreviousTestMethods = new ArrayList<>();

    @BeforeAll
    void beforeAll() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createRandomValueProvider();
    }

    @AfterEach
    void resetTestMethodRandoms() {
        methodSourceRandom = null;
        beforeEachRandom = null;
        methodRandom = null;
    }

    private Stream<String> testValues1() {
        logger.debug("create MethodSource 1");
        methodSourceRandom = createRandomValueProvider();
        return Stream.of(
                methodSourceRandom.fixedDecoratedString("1"),
                methodSourceRandom.fixedDecoratedString("2")
        );
    }

    @ParameterizedTest
    @MethodSource("testValues1")
    void a_should_ensure_reproducible_ValueProvider_creation_for_MethodSource(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(methodSourceRandom, beforeEachRandom, methodRandom);
    }

    @ParameterizedTest
    @MethodSource("testValues1")
    void b_should_ensure_reproducible_ValueProvider_creation_for_same_MethodSource(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        // @MethodSource is invoked for BEFORE EVERY test method using it
        verifyReproducibleValueProviderCreation(methodSourceRandom, beforeEachRandom, methodRandom);
    }

    private Stream<String> testValues2() {
        logger.debug("create MethodSource 2");
        methodSourceRandom = createRandomValueProvider();
        return Stream.of(
                methodSourceRandom.fixedDecoratedString("1"),
                methodSourceRandom.fixedDecoratedString("2")
        );
    }

    @ParameterizedTest
    @MethodSource("testValues2")
    void c_should_ensure_proper_separation_of_test_class_and_test_method_cycles_for_MethodSource(String testValue) {
        assertThat(testValue).isNotEmpty();
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(methodSourceRandom, beforeEachRandom, methodRandom);
    }

    @Test
    void d_should_ensure_reproducible_ValueProvider_creation() {
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(beforeEachRandom, methodRandom);
    }

    @Test
    void e_should_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        methodRandom = createRandomValueProvider();
        verifyReproducibleValueProviderCreation(beforeEachRandom, methodRandom);
    }

    private void verifyReproducibleValueProviderCreation(ValueProvider... additionalMethodRandoms) {
        List<ValueProvider> additionalMethodRandomList = asListWithoutNulls(additionalMethodRandoms);
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2)
                .addExpectedTestMethodRandomValues(instanceRandom, beforeAllRandom)
                .addExpectedTestMethodRandomValues(randomsOfPreviousTestMethods)
                .addExpectedTestMethodRandomValues(additionalMethodRandomList)
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
        randomsOfPreviousTestMethods.addAll(additionalMethodRandomList);
    }
}
