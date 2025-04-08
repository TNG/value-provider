package com.tngtech.valueprovider.example.instancioevaluation.nested;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@DisplayName("Main test class, default Lifecycle PER_METHOD")
@ExtendWith(InstancioExtension.class)
class InstancioLifecycleMainPerMethodNestedDemoTest {
    private NumberWrapper mainBeforeEachRandom;

    @BeforeEach
    void mainBeforeEach() {
        mainBeforeEachRandom = createNumber();
    }

    @Seed(FIXED_SEED)
    @Test
    void should_ensure_reproducible_data_creation_in_main_class() {
        verifyReproducibleDataCreation(mainBeforeEachRandom, createNumber());
    }

    @Seed(FIXED_SEED)
    @Test
    void should_ensure_proper_test_lifecycle_handling_in_main_class() {
        verifyReproducibleDataCreation(mainBeforeEachRandom, createNumber());
    }

    @Nested
    @DisplayName("Nested test class (level 1), default Lifecycle PER_METHOD")
    class LifecycleNestedLevel1PerMethod {
        private NumberWrapper nestedLevel1BeforeEachRandom;

        @BeforeEach
        void nestedLevel1BeforeEach() {
            nestedLevel1BeforeEachRandom = createNumber();
        }

        @Nested
        @DisplayName("Nested test class (level 2), default Lifecycle PER_METHOD")
        class LifecycleNestedLevel2PerMethod {
            private NumberWrapper nestedLevel2BeforeEachRandom;

            @BeforeEach
            void nestedLevel2BeforeEach() {
                nestedLevel2BeforeEachRandom = createNumber();
            }

            @Seed(FIXED_SEED)
            @Test
            void should_ensure_reproducible_data_creation_with_more_than_one_nesting_level() {
                verifyReproducibleDataCreation(mainBeforeEachRandom, nestedLevel1BeforeEachRandom, nestedLevel2BeforeEachRandom,
                        createNumber());
            }

            @Seed(FIXED_SEED)
            @Test
            void should_ensure_proper_test_lifecycle_handling_with_more_than_one_nesting_level() {
                verifyReproducibleDataCreation(mainBeforeEachRandom, nestedLevel1BeforeEachRandom, nestedLevel2BeforeEachRandom,
                        createNumber());
            }
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    @DisplayName("Nested test class, Lifecycle PER_CLASS")
    class LifecycleNestedPerClass {
        private NumberWrapper nestedBeforeEachRandom;

        @BeforeEach
        void nestedBeforeEach() {
            nestedBeforeEachRandom = createNumber();
        }

        @Seed(FIXED_SEED)
        @Test
        void should_ensure_reproducible_data_creation_with_more_than_one_nesting_level() {
            verifyReproducibleDataCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createNumber());
        }

        @Seed(FIXED_SEED)
        @Test
        void should_ensure_proper_test_lifecycle_handling_in_nested_class() {
            verifyReproducibleDataCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createNumber());
        }
    }
}
