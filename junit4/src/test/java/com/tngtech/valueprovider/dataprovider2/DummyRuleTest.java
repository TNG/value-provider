package com.tngtech.valueprovider.dataprovider2;

import com.tngtech.valueprovider.ValueProvider;
import com.tngtech.valueprovider.ValueProviderRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;
import static org.assertj.core.api.Assertions.assertThat;

public class DummyRuleTest {
    private static final Logger logger = LoggerFactory.getLogger(DummyRuleTest.class);

    static {
        logger.debug("{}: static initialization", DummyRuleTest.class.getSimpleName());
    }

    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();
    private ValueProvider instanceRandom = createRandomValueProvider();

    @Test
    public void dummyTest() {
        assertThat(instanceRandom).isNotNull();
    }
}
