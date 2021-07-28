package com.tngtech.valueprovider.dataprovider1;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderClassRule;
import com.tngtech.valueprovider.ValueProviderRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

public class DummyClassRuleTest {
    private static final Logger logger = LoggerFactory.getLogger(DummyClassRuleTest.class);

    static {
        logger.debug("{}: static initialization", DummyClassRuleTest.class.getSimpleName());
    }

    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();
    private static final ValueProvider classRandom = createRandomValueProvider();

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();
    private ValueProvider instanceRandom = createRandomValueProvider();

    @Test
    public void dummyTest() {
        assertThat(classRandom).isNotNull();
        assertThat(instanceRandom).isNotNull();
    }
}
