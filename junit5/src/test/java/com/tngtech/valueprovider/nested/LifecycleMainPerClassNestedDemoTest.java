package com.tngtech.valueprovider.nested;

import java.util.ArrayList;
import java.util.List;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@TestInstance(PER_CLASS)
@DisplayName("Main test class, Lifecycle PER_CLASS")
@ExtendWith(ValueProviderExtension.class)
class LifecycleMainPerClassNestedDemoTest {
    private static final ValueProvider classRandom;

    static {
        ensureDefinedFactoryState();
        classRandom = createRandomValueProvider();
    }

    private final List<ValueProvider> randomsOfPreviousTestMethods = new ArrayList<>();
    private final ValueProvider mainInstanceRandom = createRandomValueProvider();
    private ValueProvider mainBeforeAllRandom;
    private ValueProvider mainBeforeEachRandom;

    @BeforeAll
    void mainBeforeAll() {
        mainBeforeAllRandom = createRandomValueProvider();
    }

    @BeforeEach
    void mainBeforeEach() {
        mainBeforeEachRandom = createRandomValueProvider();
    }

    @Test
    void should_ensure_reproducible_ValueProvider_creation_in_main_class() {
        verifyReproducibleValueProviderCreation(mainBeforeEachRandom, createRandomValueProvider());
    }

    @Test
    void should_ensure_proper_separation_of_test_class_and_test_method_cycles_in_main_class() {
        verifyReproducibleValueProviderCreation(mainBeforeEachRandom, createRandomValueProvider());
    }

    @Nested
    @TestInstance(PER_METHOD)
    @DisplayName("Nested test class, Lifecycle PER_METHOD")
    class LifecycleNestedPerMethod {
        private final ValueProvider nestedInstanceRandom = createRandomValueProvider();
        private ValueProvider nestedBeforeEachRandom;

        @BeforeEach
        void nestedBeforeEach() {
            nestedBeforeEachRandom = createRandomValueProvider();
        }

        @Test
        void should_ensure_reproducible_ValueProvider_creation_in_nested_class() {
            verifyReproducibleValueProviderCreation(nestedInstanceRandom,
                    mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }

        @Test
        void should_ensure_proper_separation_of_test_class_and_test_method_cycles_in_nested_class() {
            verifyReproducibleValueProviderCreation(nestedInstanceRandom,
                    mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    @TestMethodOrder(MethodName.class)
    @DisplayName("Nested test class, Lifecycle PER_CLASS")
    class LifecycleNestedPerClass {
        private final ValueProvider nestedInstanceRandom = createRandomValueProvider();
        private ValueProvider nestedBeforeAllRandom;
        private ValueProvider nestedBeforeEachRandom;

        @BeforeAll
        void nestedBeforeAll() {
            nestedBeforeAllRandom = createRandomValueProvider();
        }

        @BeforeEach
        void nestedBeforeEach() {
            nestedBeforeEachRandom = createRandomValueProvider();
        }

        @Test
        void a_should_ensure_reproducible_ValueProvider_creation_in_nested_class() {
            verifyReproducibleValueProviderCreation(nestedInstanceRandom, nestedBeforeAllRandom,
                    mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }

        @Test
        void b_should_ensure_proper_separation_of_test_class_and_test_method_cycles_in_nested_class() {
            verifyReproducibleValueProviderCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }
    }

    private void verifyReproducibleValueProviderCreation(ValueProvider... additionalTestMethodRandomValues) {
        List<ValueProvider> additionalTestMethodRamdomValuesList = asList(additionalTestMethodRandomValues);
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom)
                .addExpectedTestMethodRandomValues(mainInstanceRandom, mainBeforeAllRandom)
                .addExpectedTestMethodRandomValues(randomsOfPreviousTestMethods)
                .addExpectedTestMethodRandomValues(additionalTestMethodRamdomValuesList)
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
        randomsOfPreviousTestMethods.addAll(additionalTestMethodRamdomValuesList);
    }
}
