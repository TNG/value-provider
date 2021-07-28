package com.tngtech.valueprovider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.suppressed;

class ValueProviderExtensionFailureTest {
    private static final Map<String, FailureInformation> test2FailureInformation = new HashMap<>();

    @AfterEach
    void resetFailureInformation() {
        FailureTest.enabled = false;
        test2FailureInformation.clear();
    }

    @Test
    void extension_should_provide_failure_reproduction_info() {
        FailureTest.enabled = true;

        Events testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(FailureTest.class))
                .execute()
                .testEvents();

        Set<String> testNames = test2FailureInformation.keySet();
        assertThat(testNames).as("executed tests").isNotEmpty();
        testEvents.started().assertThatEvents()
                .as("number of started tests").hasSameSizeAs(testNames);
        testNames.forEach(testName ->
                assertExpectedTestFailure(testEvents, testName));
    }

    private void assertExpectedTestFailure(Events testEvents, String testName) {
        FailureInformation failureInformation = getFailureInformation(testName);
        Condition<Throwable> expectedException = instanceOf(AssertionError.class);
        Condition<Throwable> expectedTestFailureMessage =
                message(x -> x.contains(failureInformation.expectedTestFailureMessage));
        Condition<Throwable> expectedSuppressedException =
                suppressed(0, instanceOf(ValueProviderException.class));
        Condition<Throwable> expectedFailureReproductionInfo =
                suppressed(0, message(x -> x.contains(failureInformation.expectedFailureReproductionInfo)));
        testEvents.assertThatEvents()
                .haveExactly(1,
                        event(test(testName),
                                finishedWithFailure(
                                        expectedException,
                                        expectedTestFailureMessage,
                                        expectedSuppressedException,
                                        expectedFailureReproductionInfo
                                )));
    }

    @ExtendWith(ValueProviderExtension.class)
    @EnabledIf("enabled")
    static class FailureTest {
        /**
         * To control test execution and esp. avoid running the test as part of the gradle/CI-build.
         */
        private static boolean enabled = false;

        @SuppressWarnings("unused") // used by JUnit @EnabledIf
        static boolean enabled() {
            return enabled;
        }

        @Test
        void simply_failing() {
            addFailureInformation("simply_failing", "failing intentionally");
            //noinspection ResultOfMethodCallIgnored
            fail("failing intentionally");
        }

        @Test
        void failing_with_assertion_error() {
            addFailureInformation("failing_with_assertion_error", "My specific assertion description");
            //noinspection ConstantConditions
            assertThat(true).as("My specific assertion description").isFalse();
        }
    }

    private static void addFailureInformation(String testName, String expectedFailureMessage) {
        test2FailureInformation.put(testName, new FailureInformation(expectedFailureMessage));
    }

    private static FailureInformation getFailureInformation(String testName) {
        return test2FailureInformation.get(testName);
    }

    private static class FailureInformation {
        private final String expectedTestFailureMessage;
        private final String expectedFailureReproductionInfo;

        private FailureInformation(String expectedTestFailureMessage) {
            this.expectedTestFailureMessage = expectedTestFailureMessage;
            this.expectedFailureReproductionInfo = ValueProviderException.provideFailureReproductionInfo();
        }
    }
}
