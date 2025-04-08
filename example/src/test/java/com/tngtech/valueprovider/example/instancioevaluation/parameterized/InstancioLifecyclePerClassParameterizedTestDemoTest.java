package com.tngtech.valueprovider.example.instancioevaluation.parameterized;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@TestMethodOrder(MethodName.class)
@ExtendWith(InstancioExtension.class)
class InstancioLifecyclePerClassParameterizedTestDemoTest {
    private NumberWrapper methodSourceRandom;
    private NumberWrapper beforeEachRandom;
    private NumberWrapper methodRandom;

    @BeforeEach
    void beforeEach() {
        beforeEachRandom = createNumber();
    }

    @AfterEach
    void resetTestMethodRandoms() {
        beforeEachRandom = null;
        methodRandom = null;
    }

    private Stream<Long> testValues1() {
        methodSourceRandom = createNumber();
        return Stream.of(
                methodSourceRandom.getNumber(),
                methodSourceRandom.getNumber() + 1L
        );
    }

    @Seed(FIXED_SEED)
    @ParameterizedTest
    @MethodSource("testValues1")
    void a_should_ensure_reproducible_data_creation_for_MethodSource(Long testValue) {
        assertThat(testValue).isNotNull();
        methodRandom = createNumber();
        verifyReproducibleDataCreation(
                /* @ParameterizedTest with @MethodSource NOT supported by InstancioExtension
                    methodSourceRandom,
                 */
                beforeEachRandom, methodRandom);
    }

    @Seed(FIXED_SEED)
    @Test
    void d_should_ensure_reproducible_data_creation() {
        methodRandom = createNumber();
        verifyReproducibleDataCreation(beforeEachRandom, methodRandom);
    }
}
