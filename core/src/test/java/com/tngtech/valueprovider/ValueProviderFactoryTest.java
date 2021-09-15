package com.tngtech.valueprovider;

import java.util.stream.Stream;

import com.tngtech.valueprovider.InitializationCreator.TestClassInitializationCreator;
import com.tngtech.valueprovider.InitializationCreator.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static com.tngtech.valueprovider.InitializationCreator.Type.DEFAULT;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_CLASS;
import static com.tngtech.valueprovider.InitializationCreator.Type.TEST_METHOD;
import static com.tngtech.valueprovider.ValueProviderFactory.finishTestClassCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.finishTestMethodCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.getTestClassSeed;
import static com.tngtech.valueprovider.ValueProviderFactory.instance;
import static com.tngtech.valueprovider.ValueProviderFactory.resetToDefaultVpiCreator;
import static com.tngtech.valueprovider.ValueProviderFactory.resumeTestClassCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.startTestClassCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.startTestMethodCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.takeTestClassCycleSnapshot;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueProviderFactoryTest {
    @BeforeEach
    void resetFactoryToDefault() {
        resetToDefaultVpiCreator();
    }

    @Test
    void start_and_finish_testClassCycle_should_result_in_proper_creator_activation() {
        assertActiveCreator(DEFAULT);

        startTestClassCycle();
        assertActiveCreator(TEST_CLASS);
        assertPreviousCreator(DEFAULT);

        finishTestClassCycle();
        assertActiveCreator(DEFAULT);

        startTestClassCycle();
        assertActiveCreator(TEST_CLASS);

        finishTestClassCycle();
        assertActiveCreator(DEFAULT);
    }

    @Test
    void resuming_other_testClass_cycle_switches_to_passed_creator() {
        assertActiveCreator(DEFAULT);

        TestCycle classCycle1 = startTestClassCycle();
        assertActiveCreator(classCycle1);
        assertPreviousCreator(DEFAULT);

        TestCycle classCycle2 = startTestClassCycle();
        assertActiveCreator(classCycle2);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycle1);
        assertActiveCreator(classCycle1);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycle2);
        assertActiveCreator(classCycle2);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycle2);
        assertActiveCreator(classCycle2);
        assertPreviousCreator(DEFAULT);

        finishTestClassCycle();
        assertActiveCreator(DEFAULT);

        resumeTestClassCycle(classCycle1);
        assertActiveCreator(classCycle1);
        assertPreviousCreator(DEFAULT);
    }

    @Test
    void resuming_via_testClass_cycle_snapshot_switches_to_passed_creator() {
        assertActiveCreator(DEFAULT);

        TestCycle classCycle1 = startTestClassCycle();
        TestCycleSnapshot classCycleSnapshot1 = takeTestClassCycleSnapshot(classCycle1);
        assertActiveCreator(classCycle1);
        assertPreviousCreator(DEFAULT);

        TestCycle classCycle2 = startTestClassCycle();
        TestCycleSnapshot classCycleSnapshot2 = takeTestClassCycleSnapshot(classCycle2);
        assertActiveCreator(classCycle2);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycleSnapshot1);
        assertActiveCreator(classCycleSnapshot1);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycleSnapshot2);
        assertActiveCreator(classCycleSnapshot2);
        assertPreviousCreator(DEFAULT);

        resumeTestClassCycle(classCycleSnapshot2);
        assertActiveCreator(classCycleSnapshot2);
        assertPreviousCreator(DEFAULT);

        finishTestClassCycle();
        assertActiveCreator(DEFAULT);

        resumeTestClassCycle(classCycleSnapshot1);
        assertActiveCreator(classCycleSnapshot1);
        assertPreviousCreator(DEFAULT);
    }

    @Test
    void getTestClassSeed_returns_seed_of_active_testClass_creator_or_zero() {
        // note that due to the static (thread-local) ValueProviderFactory instance,
        // the initial testClassSeed depends on the actions taken by other tests

        TestCycle classCycle1 = startTestClassCycle();
        assertTestClassSeedIsFrom(classCycle1);

        TestCycle classCycle2 = startTestClassCycle();
        assertTestClassSeedIsFrom(classCycle2);

        resumeTestClassCycle(classCycle1);
        assertTestClassSeedIsFrom(classCycle1);

        resumeTestClassCycle(classCycle2);
        assertTestClassSeedIsFrom(classCycle2);

        resumeTestClassCycle(classCycle2);
        assertTestClassSeedIsFrom(classCycle2);

        finishTestClassCycle();
        assertTestClassSeedIs(0L);

        resumeTestClassCycle(classCycle1);
        assertTestClassSeedIsFrom(classCycle1);

        finishTestClassCycle();
        assertTestClassSeedIs(0L);
    }

    private void assertTestClassSeedIsFrom(TestCycle testCycle) {
        InitializationCreator creator = (InitializationCreator) testCycle;
        assertTestClassSeedIs(creator.getSeed());
    }

    private void assertTestClassSeedIs(long expectedSeed) {
        assertThat(getTestClassSeed()).isEqualTo(expectedSeed);
    }

    @Test
    void start_and_finish_testMethodCycle_should_result_in_proper_creator_activation() {
        assertActiveCreator(DEFAULT);
        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);
        assertPreviousCreator(DEFAULT);

        finishTestMethodCycle();
        assertActiveCreator(DEFAULT);

        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);

        finishTestMethodCycle();
        assertActiveCreator(DEFAULT);

        startTestClassCycle();
        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);
        assertPreviousCreator(TEST_CLASS);

        finishTestMethodCycle();
        assertActiveCreator(TEST_CLASS);

        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);

        finishTestMethodCycle();
        assertActiveCreator(TEST_CLASS);
    }

    @Test
    void illegal_state_transitions_should_be_rejected() {
        assertIllegalStateTransition(ValueProviderFactory::finishTestClassCycle, DEFAULT, TEST_CLASS);
        assertIllegalStateTransition(ValueProviderFactory::finishTestMethodCycle, DEFAULT, TEST_METHOD);

        startTestClassCycle();
        assertIllegalStateTransition(ValueProviderFactory::finishTestMethodCycle, TEST_CLASS, TEST_METHOD);

        startTestMethodCycle();
        assertIllegalStateTransition(ValueProviderFactory::startTestMethodCycle, TEST_METHOD);

        startTestMethodCycle();
        assertIllegalStateTransition(ValueProviderFactory::finishTestClassCycle, TEST_METHOD, TEST_CLASS);

        TestCycle testCycle = startTestClassCycle();
        startTestMethodCycle();
        assertIllegalStateTransition(() -> resumeTestClassCycle(testCycle), TEST_METHOD, TEST_CLASS);

        startTestClassCycle();
        startTestMethodCycle();
        assertIllegalStateTransition(ValueProviderFactory::finishTestClassCycle, TEST_METHOD, TEST_CLASS);

        startTestClassCycle();
        startTestMethodCycle();
        assertIllegalStateTransition(ValueProviderFactory::startTestMethodCycle, TEST_METHOD);
    }

    private void assertIllegalStateTransition(Executable executable, Type... expectedTypesForErrorMessage) {
        Throwable thrown = assertThrows(IllegalStateException.class, executable);
        assertThat(thrown.getMessage()).contains(Stream.of(expectedTypesForErrorMessage).map(Type::name).collect(toList()));
        resetToDefaultVpiCreator();
    }

    @Test
    void resetToDefaultVpiCreator_should_work_regardless_of_previous_and_active_creators() {
        resetToDefaultVpiCreator();
        assertActiveCreator(DEFAULT);
        assertPreviousCreator(DEFAULT);

        startTestClassCycle();
        assertActiveCreator(TEST_CLASS);

        resetToDefaultVpiCreator();
        assertActiveCreator(DEFAULT);
        assertPreviousCreator(DEFAULT);

        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);

        resetToDefaultVpiCreator();
        assertActiveCreator(DEFAULT);
        assertPreviousCreator(DEFAULT);

        startTestClassCycle();
        startTestMethodCycle();
        assertActiveCreator(TEST_METHOD);

        resetToDefaultVpiCreator();
        assertActiveCreator(DEFAULT);
        assertPreviousCreator(DEFAULT);
    }

    private void assertActiveCreator(Type expected) {
        assertThat(instance().activeVpiCreator.getType()).isEqualTo(expected);
    }

    private void assertActiveCreator(TestCycle testCycle) {
        assertThat(instance().activeVpiCreator).isEqualTo(testCycle);
    }

    private void assertActiveCreator(TestCycleSnapshot testCycleSnapshot) {
        TestClassInitializationCreator activeVpiCreator = (TestClassInitializationCreator) instance().activeVpiCreator;
        assertThat(activeVpiCreator.takeSnapshot()).isEqualTo(testCycleSnapshot);
    }

    private void assertPreviousCreator(Type expected) {
        assertThat(instance().previousVpiCreator.getType()).isEqualTo(expected);
    }
}
