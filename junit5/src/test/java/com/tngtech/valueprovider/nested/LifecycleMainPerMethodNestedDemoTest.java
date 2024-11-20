package com.tngtech.valueprovider.nested;

import java.util.ArrayList;
import java.util.List;

import com.tngtech.valueprovider.RandomValues;
import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderAsserter;
import com.tngtech.valueprovider.ValueProviderExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.JUnit5Tests.ensureDefinedFactoryState;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("Main test class, default Lifecycle PER_METHOD")
@ExtendWith(ValueProviderExtension.class)
class LifecycleMainPerMethodNestedDemoTest {
    private static final ValueProvider classRandom;

    static {
        ensureDefinedFactoryState();
        classRandom = createRandomValueProvider();
    }

    private static ValueProvider mainBeforeAllRandom;
    private final ValueProvider mainInstanceRandom = createRandomValueProvider();
    private ValueProvider mainBeforeEachRandom;

    @BeforeAll
    static void mainBeforeAll() {
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
    @DisplayName("Nested test class (level 1), default Lifecycle PER_METHOD")
    class LifecycleNestedLevel1PerMethod {
        private final ValueProvider nestedLevel1InstanceRandom = createRandomValueProvider();
        private ValueProvider nestedLevel1BeforeEachRandom;

        @BeforeEach
        void nestedLevel1BeforeEach() {
            nestedLevel1BeforeEachRandom = createRandomValueProvider();
        }

        @Nested
        @DisplayName("Nested test class (level 2), default Lifecycle PER_METHOD")
        class LifecycleNestedLevel2PerMethod {
            private final ValueProvider nestedLevel2InstanceRandom = createRandomValueProvider();
            private ValueProvider nestedLevel2BeforeEachRandom;

            @BeforeEach
            void nestedLevel2BeforeEach() {
                nestedLevel2BeforeEachRandom = createRandomValueProvider();
            }

            @Test
            void should_ensure_reproducible_ValueProvider_creation_with_more_than_one_nesting_level() {
                verifyReproducibleValueProviderCreation(nestedLevel1InstanceRandom, nestedLevel2InstanceRandom,
                        mainBeforeEachRandom, nestedLevel1BeforeEachRandom, nestedLevel2BeforeEachRandom,
                        createRandomValueProvider());
            }

            @Test
            void should_ensure_proper_separation_of_test_class_and_test_method_cycles_with_more_than_one_nesting_level() {
                verifyReproducibleValueProviderCreation(nestedLevel1InstanceRandom, nestedLevel2InstanceRandom,
                        mainBeforeEachRandom, nestedLevel1BeforeEachRandom, nestedLevel2BeforeEachRandom,
                        createRandomValueProvider());
            }
        }
    }

    private void verifyReproducibleValueProviderCreation(ValueProvider... additionalTestMethodRandomValues) {
        new ValueProviderAsserter()
                .addExpectedTestClassRandomValues(classRandom, mainBeforeAllRandom)
                .addExpectedTestMethodRandomValues(mainInstanceRandom)
                .addExpectedTestMethodRandomValues(asList(additionalTestMethodRandomValues))
                .assertAllTestClassRandomValues()
                .assertAllTestMethodRandomValues()
                .assertAllSuffixes();
    }

    @Nested
    @TestInstance(PER_CLASS)
    @DisplayName("Nested test class, Lifecycle PER_CLASS")
    class LifecycleNestedPerClass {
        private final List<ValueProvider> randomsOfPreviousTestMethods = new ArrayList<>();
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
        void should_ensure_reproducible_ValueProvider_creation_with_more_than_one_nesting_level() {
            verifyReproducibleValueProviderCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }

        @Test
        void should_ensure_proper_separation_of_test_class_and_test_method_cycles_in_nested_class() {
            verifyReproducibleValueProviderCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createRandomValueProvider());
        }

        /**
         * Note: {@link Lifecycle#PER_CLASS} requires separate verification method
         * that adds the just verified {@link RandomValues}, as the test method cycle must not be reset
         * before completion of the last test method.
         */
        private void verifyReproducibleValueProviderCreation(ValueProvider... additionalTestMethodRandomValues) {
            List<ValueProvider> additionalTestMethodRamdomValuesList = asList(additionalTestMethodRandomValues);
            new ValueProviderAsserter()
                    .addExpectedTestClassRandomValues(classRandom, mainBeforeAllRandom)
                    .addExpectedTestMethodRandomValues(mainInstanceRandom, nestedInstanceRandom, nestedBeforeAllRandom)
                    .addExpectedTestMethodRandomValues(randomsOfPreviousTestMethods)
                    .addExpectedTestMethodRandomValues(additionalTestMethodRamdomValuesList)
                    .assertAllTestClassRandomValues()
                    .assertAllTestMethodRandomValues()
                    .assertAllSuffixes();
            randomsOfPreviousTestMethods.addAll(additionalTestMethodRamdomValuesList);
        }
    }
}
