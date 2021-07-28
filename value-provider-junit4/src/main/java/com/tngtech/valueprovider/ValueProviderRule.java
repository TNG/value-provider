package com.tngtech.valueprovider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderFactory.finishTestMethodCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.startTestMethodCycle;
import static java.lang.System.identityHashCode;

/**
 * Rule to provide failure reproduction data for {@link ValueProvider}s created during or after
 * instantiation of the test class.
 *
 * @see ValueProviderClassRule
 */
public class ValueProviderRule implements TestRule {
    private static final Logger logger = LoggerFactory.getLogger(ValueProviderRule.class);

    @SuppressWarnings("WeakerAccess") // is part of public API
    public ValueProviderRule() {
        logger.debug("Instantiation {}", identityHashCode(this));
        startTestMethodCycle();
    }

    public @Nonnull
    Statement apply(@Nonnull final Statement base, @Nonnull final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<>();

                startingQuietly(description, errors);
                try {
                    base.evaluate();
                } catch (org.junit.internal.AssumptionViolatedException e) {
                    errors.add(e);
                } catch (Throwable e) {
                    handleFailure(e, errors);
                } finally {
                    finishedQuietly(description, errors);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    private void startingQuietly(Description description, List<Throwable> errors) {
        try {
            logger.debug("{} starting test {}", identityHashCode(this), getTestMethodName(description));
        } catch (RuntimeException e) {
            errors.add(e);
        }
    }

    private void handleFailure(Throwable e, List<Throwable> errors) {
        e.addSuppressed(new ValueProviderException());
        errors.add(e);
    }

    private void finishedQuietly(Description description, List<Throwable> errors) {
        try {
            logger.debug("{} finished test {}", identityHashCode(this), getTestMethodName(description));
            finishTestMethodCycle();
        } catch (RuntimeException e) {
            errors.add(e);
        }
    }

    private String getTestMethodName(Description description) {
        return description.getClassName() + "." + description.getMethodName();
    }
}
