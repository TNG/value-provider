package com.tngtech.valueprovider.dataprovider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ValueProviderExtension.class)
public class ParameterizedTestDemoTest {
    private static final Logger logger = LoggerFactory.getLogger(ParameterizedTestDemoTest.class);
    private static final ValueProvider classRandom1;
    private static final ValueProvider classRandom2;

    static {
        logger.debug("{}: static initialization", ParameterizedTestDemoTest.class.getSimpleName());
        ensureDefinedFactoryState();
        classRandom1 = createRandomValueProvider();
        classRandom2 = createRandomValueProvider();
    }

    // as execution sequence of tests may vary
    private static final List<ValueProvider> methodSourceRandoms = new ArrayList<>();
    private static ValueProvider beforeAllRandom;

    private ValueProvider instanceRandom = createRandomValueProvider();

    private ValueProvider beforeEachRandom;

    @BeforeAll
    static void beforeAll() {
        beforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createRandomValueProvider();
    }

    private static Stream<String> testValues1() {
        logger.debug("create DataProvider 1");
        ValueProvider methodSourceRandom = createRandomValueProvider();
        methodSourceRandoms.add(methodSourceRandom);
        return Stream.of(
                methodSourceRandom.fixedDecoratedString("1"),
                methodSourceRandom.fixedDecoratedString("2")
        );
    }

    @ParameterizedTest
    @MethodSource("testValues1")
    void should_ensure_reproducible_ValueProvider_creation_for_ParameterizedTest(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    private static Stream<String> testValues2() {
        logger.debug("create DataProvider 2");
        ValueProvider methodSourceRandom = createRandomValueProvider();
        methodSourceRandoms.add(methodSourceRandom);
        return Stream.of(
                methodSourceRandom.fixedDecoratedString("1"),
                methodSourceRandom.fixedDecoratedString("2")
        );
    }

    @ParameterizedTest
    @MethodSource("testValues2")
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles_for_ParameterizedTest(String testValue) {
        assertThat(testValue).isNotEmpty();
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation() {
        verifyReproducibleValueProviderCreation();
    }

    @Test
    void identical_test_to_ensure_proper_separation_of_test_class_and_test_method_cycles() {
        verifyReproducibleValueProviderCreation();
    }

    private void verifyReproducibleValueProviderCreation() {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom1, classRandom2, beforeAllRandom)
                .addExpectedTestClassRandomValues(methodSourceRandoms)
                .addExpectedTestMethodRandomValues(
                        instanceRandom, beforeEachRandom, createRandomValueProvider(), createRandomValueProvider())
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }
}
