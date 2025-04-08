package com.tngtech.valueprovider.example.instancioevaluation.nested;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;

@TestInstance(PER_CLASS)
@DisplayName("Main test class, Lifecycle PER_CLASS")
@ExtendWith(InstancioExtension.class)
class InstancioLifecycleMainPerClassNestedDemoTest {
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
    @TestInstance(PER_METHOD)
    @DisplayName("Nested test class, Lifecycle PER_METHOD")
    class LifecycleNestedPerMethod {
        private NumberWrapper nestedBeforeEachRandom;

        @BeforeEach
        void nestedBeforeEach() {
            nestedBeforeEachRandom = createNumber();
        }

        @Seed(FIXED_SEED)
        @Test
        void should_ensure_reproducible_data_creation_in_nested_class() {
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

    @Nested
    @TestInstance(PER_CLASS)
    @TestMethodOrder(MethodName.class)
    @DisplayName("Nested test class, Lifecycle PER_CLASS")
    class LifecycleNestedPerClass {
        private NumberWrapper nestedBeforeEachRandom;

        @BeforeEach
        void nestedBeforeEach() {
            nestedBeforeEachRandom = createNumber();
        }

        @Seed(FIXED_SEED)
        @Test
        void a_should_ensure_reproducible_data_creation_in_nested_class() {
            verifyReproducibleDataCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createNumber());
        }

        @Seed(FIXED_SEED)
        @Test
        void b_should_ensure_proper_test_lifecycle_handling_in_nested_class() {
            verifyReproducibleDataCreation(mainBeforeEachRandom, nestedBeforeEachRandom,
                    createNumber());
        }
    }
}
