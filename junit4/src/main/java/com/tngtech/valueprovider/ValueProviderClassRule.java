package com.tngtech.valueprovider;

import javax.annotation.Nonnull;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderFactory.resetToDefaultVpiCreator;
import static com.tngtech.valueprovider.ValueProviderFactory.resumeTestClassCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.startTestClassCycle;
import static com.tngtech.valueprovider.ValueProviderFactory.takeTestClassCycleSnapshot;
import static java.lang.System.identityHashCode;

/**
 * Rule to provide failure reproduction data for statically created {@link ValueProvider}s.
 *
 * @see ValueProviderRule
 */
public class ValueProviderClassRule extends TestWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ValueProviderClassRule.class);
    private TestCycle testCycle;
    private TestCycleSnapshot testCycleSnapshot;

    public ValueProviderClassRule() {
        logger.debug("Instantiation {}", identityHashCode(this));
        testCycle = startTestClassCycle();
    }

    @Override
    protected void starting(@Nonnull Description description) {
        if (testCycle != null) {
            logger.debug("starting {}, first execution after loading test class",
                    description.getTestClass().getSimpleName());
            // First execution after loading test class,
            // reuse test (class) cycle to ensure reliable data for failure reproduction and unique VP-suffixes.
            resumeTestClassCycle(testCycle);
            // take minimal 'snapshot' of test (class) cycle state required for (potential) further executions
            testCycleSnapshot = takeTestClassCycleSnapshot(testCycle);
            // delete reference to allow garbage collection of test (class) cycle
            // and only keep the minimal 'snapshot'
            testCycle = null;
        } else {
            logger.debug("starting {}, second or further execution",
                    description.getTestClass().getSimpleName());
            // Second or further execution after loading test class,
            // restore & resume test (class) cycle from minimal 'snapshot' data
            resumeTestClassCycle(testCycleSnapshot);
        }
    }

    @Override
    protected void finished(@Nonnull Description description) {
        logger.debug("finished {}", description.getTestClass().getSimpleName());
        resetToDefaultVpiCreator();
    }
}
