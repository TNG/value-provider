package com.tngtech.valueprovider.example.instancioevaluation.parameterized;

import com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper;
import org.instancio.junit.InstancioExtension;
import org.instancio.junit.InstancioSource;
import org.instancio.junit.Seed;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import static com.tngtech.valueprovider.example.instancioevaluation.util.NumberWrapper.*;

@ExtendWith(InstancioExtension.class)
class InstancioSourceParameterizedTestDemoTest {
    @Seed(FIXED_SEED)
    @ParameterizedTest
    @InstancioSource
    void should_ensure_reproducible_data_creation_for_InstancioSource(NumberWrapper instancioSourceRandom) {
        NumberWrapper methodRandom1 = createNumber();
        NumberWrapper methodRandom2 = createNumber();

        /*
        Note: The values of instancioSourceRandom DO depend on the specified @Seed,
        but it only determines the starting point for the sequence of invocations.
        Therefore, reproducing a test failure requires not only to specify the @Seed
        as provided by Instancio, but also to manually reproduce all the data of the parameter(s)
        of the respective test run.
        This behavior is independent of the chosen JUnit test lifecycle.
         */
        //assertThat(instancioSourceRandom.getNumber()).isEqualTo(213L);
        verifyReproducibleDataCreation(methodRandom1, methodRandom2);
    }
}
