package com.tngtech.valueprovider;

import java.util.Arrays;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import static com.tngtech.valueprovider.ValueProviderRuleFailureTest.StackTraceMatcher.containsMethodNamesInStackTrace;
import static com.tngtech.valueprovider.ValueProviderRuleFailureTest.SuppressedValueProviderExceptionMatcher.hasSuppressedValueProviderException;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.rules.RuleChain.outerRule;

@SuppressWarnings({"TwistedAssertion", "ConstantConditions"})
public class ValueProviderRuleFailureTest {
    // must use a RuleChain, assertThatThrownBy() is run 'inside' the ValueProviderRule
    @SuppressWarnings("deprecation")
    private final ExpectedException thrown = ExpectedException.none();
    private final ValueProviderRule instanceProviders = new ValueProviderRule();

    @Rule
    public RuleChain rules = outerRule(thrown).around(instanceProviders);

    @Test
    public void should_provide_failure_reproduction_info_for_simple_failure() {
        setFailureExpectations("failing intentionally");
        //noinspection ResultOfMethodCallIgnored
        fail("failing intentionally");
    }

    @Test
    public void should_provide_failure_reproduction_info_for_assertion_error() {
        setFailureExpectations("My specific assertion description");
        assertThat(true).as("My specific assertion description").isFalse();
    }

    @Test
    public void should_provide_stacktrace_for_ComparisonFailure() {
        setFailureStackTraceExpectations("should_provide_stacktrace_for_ComparisonFailure");
        assertThat(true).as("My specific assertion description").isFalse();
    }

    @Test
    public void should_provide_stacktrace_for_invoked_method() {
        setFailureStackTraceExpectations("should_provide_stacktrace_for_invoked_method", "produceException");
        produceException();
    }

    private void produceException() {
        throw new RuntimeException("intentional");
    }

    private void setFailureExpectations(String message) {
        thrown.expectMessage(message);
        thrown.expect(hasSuppressedValueProviderException());
    }

    private void setFailureStackTraceExpectations(String... expectedMethodNamesInStackTrace) {
        thrown.expect(containsMethodNamesInStackTrace(expectedMethodNamesInStackTrace));
    }

    @SuppressWarnings("unused")
    static class SuppressedValueProviderExceptionMatcher extends TypeSafeMatcher<Throwable> {
        static SuppressedValueProviderExceptionMatcher hasSuppressedValueProviderException() {
            return new SuppressedValueProviderExceptionMatcher();
        }

        private SuppressedValueProviderExceptionMatcher() {
        }

        @Override
        protected boolean matchesSafely(Throwable failure) {
            return stream(failure.getSuppressed())
                    .anyMatch(suppressed -> suppressed.getClass().equals(ValueProviderException.class));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("to have suppressed exception of type %s",
                    ValueProviderException.class.getName()));
        }
    }

    @SuppressWarnings("unused")
    static class StackTraceMatcher extends TypeSafeMatcher<Throwable> {
        private final String[] expectedMethodNamesInStackTrace;

        static StackTraceMatcher containsMethodNamesInStackTrace(String... expectedMethodNamesInStackTrace) {
            return new StackTraceMatcher(expectedMethodNamesInStackTrace);
        }

        private StackTraceMatcher(String... expectedMethodNamesInStackTrace) {
            this.expectedMethodNamesInStackTrace = expectedMethodNamesInStackTrace;
        }

        @Override
        protected boolean matchesSafely(Throwable failure) {
            String stackTrace = getStackTrace(failure);
            return stream(expectedMethodNamesInStackTrace).allMatch(stackTrace::contains);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("to contain %s in stack trace", Arrays.toString(expectedMethodNamesInStackTrace)));
        }
    }
}
