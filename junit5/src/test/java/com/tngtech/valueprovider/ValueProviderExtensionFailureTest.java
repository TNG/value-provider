package com.tngtech.valueprovider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import static com.tngtech.valueprovider.ValueProviderExtensionFailureTest.FailureInformation.expectRootClassOfFailingTestInReproductionInfo;
import static com.tngtech.valueprovider.ValueProviderExtensionFailureTest.FailureInformation.noTestClassInReproductionInfo;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.*;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.*;

class ValueProviderExtensionFailureTest {
    private static final Map<String, FailureInformation> test2FailureInformation = new HashMap<>();
    /**
     * To allow programmatic execution, but avoid running the failing tests as part of the gradle/CI-build.
     */
    private static boolean enableFailureTest = false;

    private static boolean isFailureTestEnabled() {
        return enableFailureTest;
    }

    @BeforeEach
    void enableFailureTest() {
        enableFailureTest = true;
    }

    @AfterEach
    void resetFailureInformation() {
        enableFailureTest = false;
        test2FailureInformation.clear();
    }

    private static Stream<Arguments> failureTests() {
        return Stream.of(
                arguments(named("simple failure tests, Lifecycle PER_METHOD", SimpleFailureTest.class)),
                arguments(named("nested failure tests, Lifecycles PER_METHOD and PER_CLASS", MainOfNestedFailureTest.class))
        );
    }

    @ParameterizedTest
    @MethodSource("failureTests")
    void extension_should_provide_failure_reproduction_info(Class<?> failureTestClass) {
        Events testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(failureTestClass))
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
        Condition<Throwable> expectedRootClassOfFailingTestInReproductionInfo =
                suppressed(0, message(x ->
                {
                    boolean containsRootClassOfFailingTest = x.contains(failureInformation.classOfFailingTest.getName());
                    boolean expectRootClassOfFailingTest = failureInformation.expectRootClassOfFailingTestInReproductionInfo;
                    return containsRootClassOfFailingTest == expectRootClassOfFailingTest;
                }));
        if (failureInformation.expectRootClassOfFailingTestInReproductionInfo)
            testEvents.assertThatEvents()
                    .haveExactly(1,
                            event(test(testName),
                                    finishedWithFailure(
                                            expectedException,
                                            expectedTestFailureMessage,
                                            expectedSuppressedException,
                                            expectedFailureReproductionInfo,
                                            expectedRootClassOfFailingTestInReproductionInfo
                                    )));
    }

    @SuppressWarnings("JUnitMalformedDeclaration") // false positive, class is executed programmatically
    @ExtendWith(ValueProviderExtension.class)
    @EnabledIf("com.tngtech.valueprovider.ValueProviderExtensionFailureTest#isFailureTestEnabled")
    static class SimpleFailureTest {
        @Test
        void simply_failing() {
            String failureMessage = "failing intentionally";
            FailureInformation failureInformation = noTestClassInReproductionInfo(failureMessage, this.getClass());
            addFailureInformation("simply_failing", failureInformation);
            fail(failureMessage);
        }

        @Test
        void failing_with_assertion_error() {
            String failureMessage = "My specific assertion description";
            FailureInformation failureInformation = noTestClassInReproductionInfo(failureMessage, this.getClass());
            addFailureInformation("failing_with_assertion_error", failureInformation);
            assertThat(enableFailureTest).as(failureMessage).isFalse();
        }
    }

    @SuppressWarnings("JUnitMalformedDeclaration") // false positive, class is executed programmatically
    @ExtendWith(ValueProviderExtension.class)
    @EnabledIf("com.tngtech.valueprovider.ValueProviderExtensionFailureTest#isFailureTestEnabled")
    static class MainOfNestedFailureTest {
        @Test
        void failing_in_main() {
            String failureMessage = "failing intentionally";
            FailureInformation failureInformation = noTestClassInReproductionInfo(failureMessage, this.getClass());
            addFailureInformation("failing_in_main", failureInformation);
            fail(failureMessage);
        }

        @Nested
        @TestInstance(PER_CLASS)
        class NestedFailureTestLifecyclePerClass {

            @Test
            void failing_in_nested_with_lifecycle_per_class() {
                String failureMessage = "failing intentionally";
                FailureInformation failureInformation = expectRootClassOfFailingTestInReproductionInfo(failureMessage, this.getClass());
                addFailureInformation("failing_in_nested_with_lifecycle_per_class", failureInformation);
                fail(failureMessage);
            }

            @Nested
            @TestInstance(PER_METHOD)
            class NestedFailureTestChildOfLifecyclePerClass {

                @Test
                void failing_in_child_with_lifecycle_per_method_of_parent_with_lifecycle_per_class() {
                    String failureMessage = "failing intentionally";
                    Class<?> parentWithLifecyclePerClass = NestedFailureTestLifecyclePerClass.class;
                    FailureInformation failureInformation = expectRootClassOfFailingTestInReproductionInfo(failureMessage, parentWithLifecyclePerClass);
                    addFailureInformation("failing_in_child_with_lifecycle_per_method_of_parent_with_lifecycle_per_class",
                            failureInformation);
                    fail(failureMessage);
                }
            }
        }

        @Nested
        class NestedFailureTestLifecyclePerMethod {

            @Test
            void failing_in_nested_with_lifecycle_per_method() {
                String failureMessage = "failing intentionally";
                FailureInformation failureInformation = noTestClassInReproductionInfo(failureMessage, this.getClass());
                addFailureInformation("failing_in_nested_with_lifecycle_per_method", failureInformation);
                fail(failureMessage);
            }
        }
    }

    private static void addFailureInformation(String testName, FailureInformation failureInformation) {
        test2FailureInformation.put(testName, failureInformation);
    }

    private static FailureInformation getFailureInformation(String testName) {
        return test2FailureInformation.get(testName);
    }

    static class FailureInformation {
        private final String expectedTestFailureMessage;
        private final String expectedFailureReproductionInfo;
        private final Class<?> classOfFailingTest;
        private final boolean expectRootClassOfFailingTestInReproductionInfo;

        static FailureInformation noTestClassInReproductionInfo(String expectedTestFailureMessage, Class<?> rootClassOfFailingTest) {
            return new FailureInformation(expectedTestFailureMessage, rootClassOfFailingTest, false);
        }

        static FailureInformation expectRootClassOfFailingTestInReproductionInfo(String expectedTestFailureMessage, Class<?> rootClassOfFailingTest) {
            return new FailureInformation(expectedTestFailureMessage, rootClassOfFailingTest, true);
        }

        private FailureInformation(String expectedTestFailureMessage,
                                   Class<?> rootClassOfFailingTest, boolean expectRootClassOfFailingTestInReproductionInfo) {
            this.expectedTestFailureMessage = expectedTestFailureMessage;
            Optional<Class<?>> testClassToReRunForReproduction = expectRootClassOfFailingTestInReproductionInfo
                    ? Optional.of(rootClassOfFailingTest)
                    : empty();
            this.expectedFailureReproductionInfo = ValueProviderException.provideFailureReproductionInfo(testClassToReRunForReproduction);
            this.classOfFailingTest = rootClassOfFailingTest;
            this.expectRootClassOfFailingTestInReproductionInfo = expectRootClassOfFailingTestInReproductionInfo;
        }
    }
}
