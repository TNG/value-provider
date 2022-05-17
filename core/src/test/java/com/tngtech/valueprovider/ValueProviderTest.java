package com.tngtech.valueprovider;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.Data;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.tngtech.valueprovider.ValueProvider.Builder;
import static com.tngtech.valueprovider.ValueProviderInitialization.createRandomInitialization;
import static com.tngtech.valueprovider.ValueProviderInitialization.createReproducibleInitialization;
import static com.tngtech.valueprovider.ValueProviderTest.MethodInvocation.assertDifferentResultAsFarAsPossible;
import static com.tngtech.valueprovider.ValueProviderTest.MethodInvocation.assertEqualResult;
import static com.tngtech.valueprovider.ValueProviderTest.MethodInvocation.invoke;
import static com.tngtech.valueprovider.ValueProviderTest.MyBeanTestData.myBeanContained;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.EIGHT;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.ELEVEN;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.FIVE;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.FOUR;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.NINE;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.ONE;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.SEVEN;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.SIX;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.TEN;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.THREE;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.TWELVE;
import static com.tngtech.valueprovider.ValueProviderTest.TestEnum.TWO;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.reflect.MethodUtils.getMatchingAccessibleMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

class ValueProviderTest {

    @Test
    void identical_providers_should_provide_identical_values() {
        int seed = 1;
        LocalDateTime referenceLocalDateTime = now();
        ValueProvider provider = new ValueProvider.Builder(seed)
                .withReferenceLocalDateTime(referenceLocalDateTime)
                .build();
        ValueProvider identical = new ValueProvider.Builder(seed)
                .withReferenceLocalDateTime(referenceLocalDateTime)
                .build();

        assertEqualResult(provider, identical, allMethodInvocations());
    }

    @Test
    void different_providers_should_provide_different_values_most_of_the_time() {
        long seed = 1L;
        LocalDateTime referenceLocalDateTime = now();
        ValueProvider provider = new ValueProvider.Builder(seed)
                .withReferenceLocalDateTime(referenceLocalDateTime)
                .build();
        ValueProvider different = new ValueProvider.Builder(seed + 1L)
                // note that date-related methods require at least a day of difference
                .withReferenceLocalDateTime(referenceLocalDateTime.plusDays(1))
                .build();

        assertDifferentResultAsFarAsPossible(provider, different, allMethodInvocations());
    }

    private static Collection<MethodInvocation> allMethodInvocations() {
        ValueProvider random = withRandomValues();
        int stringLength = random.intNumber(10, 20);
        String address = random.lowercaseString(stringLength);
        int min = random.intNumber(100, 1000);
        int max = random.intNumber(1001, 2000);
        int numLines = random.intNumber(2, 10);

        return newArrayList(
                invoke("booleanValue"),
                invoke("byteArray"),
                invoke("httpsUrl"),
                invoke("httpUrl"),
                invoke("ipAddress"),
                invoke("positiveBigDecimalNumber"),
                invoke("positiveBigIntegerNumber"),
                invoke("positiveIntNumber"),
                invoke("positiveLongNumber"),
                invoke("url"),
                invoke("uuid"),
                invoke("lowercaseString", stringLength),
                invoke("numericString", stringLength),
                invoke("numericString", stringLength, min, max),
                invoke("randomString", stringLength),
                invoke("uppercaseString", stringLength),
                invoke("httpsUrl", address),
                invoke("httpUrl", address),
                invoke("url", address),
                invoke("bigDecimalNumber", min, max),
                invoke("bigIntegerNumber", BigInteger.valueOf(min), BigInteger.valueOf(max)),
                invoke("intNumber", min, max),
                invoke("longNumber", min, max),
                invoke("fixedLocalDate"),
                invoke("localDateBetweenYears", min, max),
                invoke("localTime"),
                invoke("fixedLocalDateTime"),
                invoke("fixedDecoratedStrings", numLines),
                invoke("fixedDecoratedStrings", numLines, random.randomString(stringLength))
        );
    }

    static class MethodInvocation {
        private final Method method;
        private final Object[] methodParams;

        static MethodInvocation invoke(String methodName, Object... methodParams) {
            return new MethodInvocation(methodName, methodParams);
        }

        private MethodInvocation(String methodName, Object... methodParams) {
            List<Class<?>> methodParamTypes = stream(methodParams).map(Object::getClass).collect(toList());
            this.method = getMatchingAccessibleMethod(ValueProvider.class, methodName, methodParamTypes.toArray(new Class<?>[]{}));
            this.methodParams = methodParams;
        }

        private void assertEqualResult(ValueProvider provider1, ValueProvider provider2) {
            Object result1 = invokeMethod(method, provider1, methodParams);
            Object result2 = invokeMethod(method, provider2, methodParams);
            assertThat(result1)
                    .as("invoking %s(%s) on %s and %s", method.getName(), Arrays.toString(methodParams), provider1, provider2)
                    .isEqualTo(result2);
        }

        static void assertEqualResult(ValueProvider provider1, ValueProvider provider2, Iterable<MethodInvocation> invocations) {
            for (MethodInvocation invocation : invocations) {
                invocation.assertEqualResult(provider1, provider2);
            }
        }

        private void assertDifferentResultAsFarAsPossible(ValueProvider provider1, ValueProvider provider2) {
            int maxRetries = 5;
            Object result1 = null;
            Object result2 = null;
            for (int i = 0; i < maxRetries && Objects.equals(result1, result2); i++) {
                result1 = invokeMethod(method, provider1, methodParams);
                result2 = invokeMethod(method, provider2, methodParams);
            }

            assertThat(result1)
                    .as("invoking %s(%s) on %s and %s (for %d retries)",
                            method.getName(), Arrays.toString(methodParams), provider1, provider2, maxRetries)
                    .isNotEqualTo(result2);
        }

        static void assertDifferentResultAsFarAsPossible(ValueProvider provider1, ValueProvider provider2, Iterable<MethodInvocation> invocations) {
            for (MethodInvocation invocation : invocations) {
                invocation.assertDifferentResultAsFarAsPossible(provider1, provider2);
            }
        }
    }

    @Test
    void booleanValue_should_create_both_values() {
        ValueProvider random = withRandomValues();

        boolean trueCreated = false;
        boolean falseCreated = false;
        for (int i = 0; i < 1000 && !(trueCreated && falseCreated); i++) {
            if (random.booleanValue()) {
                trueCreated = true;
            } else {
                falseCreated = true;
            }
        }
        assertThat(trueCreated).as("true created").isTrue();
        assertThat(falseCreated).as("false created").isTrue();
    }

    @Test
    void intNumber_should_create_number_between_min_and_max() {
        for (int i = 0; i < 1000; i++) {
            int min = i + 1;
            int max = (2 * i) + 500;
            assertIntNumber(min, max);
        }
    }

    @Test
    void intNumber_should_create_correct_numbers_for_the_limits_of_the_integer_range() {
        assertIntNumber(0, 0);
        assertIntNumber(0, 1);
        assertIntNumber(1, 1);
        assertIntNumber(1, 2);
        assertIntNumber(2, 2);
        assertIntNumber(2, Integer.MAX_VALUE);
        assertIntNumber(1, Integer.MAX_VALUE);
        assertIntNumber(0, Integer.MAX_VALUE);
        assertIntNumber(Integer.MAX_VALUE - 2, Integer.MAX_VALUE);
        assertIntNumber(Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
        assertIntNumber(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Test
    void longNumber_should_create_number_between_min_and_max_and_outside_integer_range() {
        for (long i = 0; i < 1000; i++) {
            long min = i + 1;
            long max = (2 * i) + 500;
            assertLongNumber(min, max);
            assertLongNumber(min - 20 - Integer.MAX_VALUE, max + 20 + Integer.MAX_VALUE);
        }
    }

    @Test
    void longNumber_should_create_correct_numbers_for_the_limits_of_the_long_range() {
        assertLongNumber(0L, 0L);
        assertLongNumber(0L, 1L);
        assertLongNumber(1L, 1L);
        assertLongNumber(1L, 2L);
        assertLongNumber(2L, 2L);
        assertLongNumber(0L, Long.MAX_VALUE);
        assertLongNumber(1L, Long.MAX_VALUE);
        assertLongNumber(2L, Long.MAX_VALUE);
        assertLongNumber(Long.MAX_VALUE - 2L, Long.MAX_VALUE);
        assertLongNumber(Long.MAX_VALUE - 1L, Long.MAX_VALUE);
        assertLongNumber(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    @Test
    void longNumber_should_create_random_numbers_for_the_entire_long_range() {
        long min = 0L;
        long max = Long.MAX_VALUE;

        int numCycles = 10;
        int numRandomNulls = 0;
        for (int i = 0; i < numCycles; i++) {
            if (withRandomValues().longNumber(min, max) == min) {
                numRandomNulls++;
            }
        }
        assertThat(numRandomNulls)
                .as("Number of 'random' 0s for %d cycles", numCycles)
                .isLessThan(numCycles);
    }

    @Test
    void bigIntegerNumber_should_create_random_numbers_for_the_entire_long_range() {
        long min = 0L;
        long max = Long.MAX_VALUE;

        int numCycles = 10;
        int numRandomNulls = 0;
        for (int i = 0; i < numCycles; i++) {
            if (withRandomValues().bigIntegerNumber(BigInteger.valueOf(min), BigInteger.valueOf(max)).equals(BigInteger.valueOf(min))) {
                numRandomNulls++;
            }
        }
        assertThat(numRandomNulls)
                .as("Number of 'random' 0s for %d cycles", numCycles)
                .isLessThan(numCycles);
    }

    @Test
    void bigIntegerNumber_should_create_number_between_min_and_max_and_exceeding_long_range() {
        for (long i = 0; i < 1000; i++) {
            long longMin = i + 1;
            long longMax = (2 * i) + 500;
            BigInteger min = BigInteger.valueOf(longMin);
            BigInteger max = BigInteger.valueOf(longMax);
            assertBigIntegerNumber(min, max);
            BigInteger maxLongAsBigInteger = BigInteger.valueOf(Long.MAX_VALUE);
            assertBigIntegerNumber(min.subtract(maxLongAsBigInteger), max.add(maxLongAsBigInteger));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void integer_number_methods_should_handle_range_zero_to_one_properly() {
        ValueProvider random = withRandomValues();

        ValueCreation.with(random, (v) -> v.intNumber(1, 10)).expect(IntStream.rangeClosed(1, 10).boxed());
        ValueCreation.with(random, (v) -> v.longNumber(1, 10)).expect(LongStream.rangeClosed(1, 10).boxed());
        ValueCreation.with(random, (v) -> v.bigIntegerNumber(BigInteger.ONE, BigInteger.TEN))
                .expect(LongStream.rangeClosed(1, 10).boxed().map(BigInteger::valueOf));
    }

    private static class ValueCreation<T> {
        private final ValueProvider provider;
        private final Function<ValueProvider, T> valueCreator;

        @SuppressWarnings("rawtypes")
        static <T> ValueCreation with(ValueProvider provider, Function<ValueProvider, T> valueCreator) {
            return new ValueCreation<>(provider, valueCreator);
        }

        ValueCreation(ValueProvider provider, Function<ValueProvider, T> valueCreator) {
            this.provider = provider;
            this.valueCreator = valueCreator;
        }

        void expect(Stream<T> expectedValues) {
            Set<T> expected = expectedValues.collect(toSet());
            Set<T> values = new HashSet<>();
            for (int i = 0; i < expected.size() * 100; i++) {
                values.add(valueCreator.apply(provider));
            }
            assertThat(values).isEqualTo(expected);
        }
    }

    @Test
    void fixedDecoratedString_with_limited_length_should_shorten_suffix_before_shortening_prefix() {
        ValueProvider provider = new Builder(withRandomValues()).withConstantPrefix("1234").withConstantSuffix("5678").build();

        String result = provider.fixedDecoratedString("c", 3);

        assertThat(result).isEqualTo("34c");
    }

    @Test
    void fixedDecoratedString_with_limited_length_should_shorten_suffix_and_prefix_before_shortening_base() {
        ValueProvider provider = new Builder(withFixedValues()).withConstantPrefix("1234").withConstantSuffix("5678").build();

        String result = provider.fixedDecoratedString("cccc", 3);

        assertThat(result).isEqualTo("ccc");
    }

    @Test
    void fixedDecoratedString_with_limited_length_should_use_suffix_to_yield_requested_length() {
        ValueProvider provider = new Builder(withFixedValues()).withConstantPrefix("1").withConstantSuffix("2345").build();

        String result = provider.fixedDecoratedString("c", 3);

        assertThat(result).isEqualTo("1c2");
    }

    @Test
    void fixedDecoratedString_with_limited_length_should_return_base_as_is_if_too_short_for_requested_length() {
        ValueProvider provider = new Builder(withFixedValues()).withConstantSuffix("").build();

        String result = provider.fixedDecoratedString("c", 3);

        assertThat(result).isEqualTo("c");
    }

    @Test
    void fixedDecoratedStrings_should_create_multiple_strings() {
        ValueProvider provider = withRandomValues();

        int numLines = provider.intNumber(2, 5);
        List<String> lines = provider.fixedDecoratedStrings(numLines, "my text");

        assertThat(lines).hasSize(numLines);
        int lineNumber = 0;
        for (String line : lines) {
            assertThat(line).contains("my text", provider.getSuffix(), "" + lineNumber);
            lineNumber++;
        }
    }

    @Test
    void bigDecimalNumber_should_create_number_between_min_and_max() {
        for (long i = 0; i < 1000; i++) {
            long min = i + 1;
            long max = (2 * i) + 500;
            assertBigDecimalNumber(min, max);
            assertBigDecimalNumber(min, max + 20 + Integer.MAX_VALUE);
        }
    }

    @Test
    void bigDecimalNumberWithScale_should_return_numbers_within_specified_range_as_long_as_scale_allows_it() {
        assertBigDecimalNumberWithScale2(1.001, 1.004);
    }

    @TestFactory
    List<DynamicTest> numericString_should_allow_restricting_min_and_max_in_addition_to_length() {
        return newArrayList(
                lengthMinMax("unrestricted", 3, 100, 200),
                lengthMinMax("restricted to range", 3, 120, 180),
                lengthMinMax("restricted to single value", 3, 150, 150)
        );
    }

    private static DynamicTest lengthMinMax(String name, int length, int min, int max) {
        return DynamicTest.dynamicTest(name, () -> {
            String numericString = withRandomValues().numericString(length, min, max);

            assertThat(Integer.valueOf(numericString))
                    .isGreaterThanOrEqualTo(min)
                    .isLessThanOrEqualTo(max);
        });
    }

    @Test
    void numericString_should_reject_invalid_min_max_combinations() {
        int min = 10;
        int max = 9;
        Throwable thrown = assertThrows(IllegalArgumentException.class,
                () -> withRandomValues().numericString(2, min, max));
        assertThat(thrown.getMessage()).contains("" + min, "" + max);
    }

    @Test
    void numericString_should_reject_too_small_length_wrt_min() {
        int min = 10;
        int max = 20;
        int tooSmallLength = 1;
        Throwable thrown = assertThrows(IllegalArgumentException.class,
                () -> withRandomValues().numericString(tooSmallLength, min, max));
        assertThat(thrown.getMessage()).contains("" + tooSmallLength, "" + min);
    }

    @Test
    void oneOf_should_return_single_value() {
        ValueProvider random = withRandomValues();

        String value1 = "1";
        String value2 = "2";
        String value3 = "3";
        List<String> valueList = newArrayList(value1, value2, value3);
        Set<String> valueSet = newHashSet(value1, value2, value3);
        String[] valueArray = {value1, value2, value3};

        String actualValue = random.oneOf(value1, value2, value3);
        assertThat(actualValue).isIn(valueSet);

        actualValue = random.oneOf(valueList);
        assertThat(actualValue).isIn(valueSet);

        actualValue = random.oneOf(valueSet);
        assertThat(actualValue).isIn(valueSet);

        actualValue = random.oneOf(valueArray);
        assertThat(actualValue).isIn(valueSet);

        TestEnum actualEnumValue = random.oneOf(TestEnum.class);
        assertThat(EnumSet.allOf(TestEnum.class)).contains(actualEnumValue);
    }

    @Test
    void oneOf_should_throw_Exception_if_called_with_empty_input() {
        ValueProvider random = withRandomValues();

        String expectedException = "at least one";

        assertThatThrownBy(() -> random.oneOf(emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);

        assertThatThrownBy(() -> random.oneOf(new String[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);

        assertThatThrownBy(() -> random.oneOf(EmptyTestEnum.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);
    }

    @Test
    void oneOfExcluding_should_return_allowed_values_only() {
        ValueProvider random = withRandomValues();

        assertThat(random.oneOfExcluding(newArrayList("1", "2", "3", "4"), "3", "4")).isIn("1", "2").isNotIn("3", "4");

        assertThat(random.oneOfExcluding(ONE, THREE, FIVE, SEVEN, NINE, ELEVEN))
                .isIn(TWO, FOUR, SIX, EIGHT, TEN, TWELVE)
                .isNotIn(ONE, THREE, FIVE, SEVEN, NINE, ELEVEN);
    }

    @Test
    void oneOfExcluding_should_throw_Exception_if_no_elements_are_left_after_exclusion() {
        ValueProvider random = withRandomValues();

        String expectedException = "at least one";

        assertThatThrownBy(() -> random.oneOfExcluding(newArrayList("1", "2", "3"), "1", "2", "3"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);

        assertThatThrownBy(() -> random.oneOfExcluding(emptyList(), "1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);

        assertThatThrownBy(() -> random.oneOfExcluding(TestEnum.values()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedException);
    }

    @Test
    void someOf_should_return_sub_collection() {
        ValueProvider random = withRandomValues();

        String value1 = "1";
        String value2 = "2";
        String value3 = "3";
        List<String> valueList = newArrayList(value1, value2, value3);
        Set<String> valueSet = newHashSet(value1, value2, value3);
        String[] valueArray = {value1, value2, value3};

        Collection<String> actualValues = random.someOf(value1, value2, value3);
        assertThat(actualValues).isSubsetOf(valueSet);

        actualValues = random.someOf(valueList);
        assertThat(actualValues).isSubsetOf(valueSet);

        actualValues = random.someOf(valueSet);
        assertThat(actualValues).isSubsetOf(valueSet);

        actualValues = random.someOf(valueArray);
        assertThat(actualValues).isSubsetOf(valueSet);

        assertThat(random.someOf(TestEnum.class))
                .isSubsetOf(EnumSet.allOf(TestEnum.class));
    }

    @Test
    void someOf_should_return_sub_collection_of_specified_length() {
        ValueProvider random = withRandomValues();

        String value1 = "1";
        String value2 = "2";
        String value3 = "3";
        List<String> valueList = newArrayList(value1, value2, value3);
        Set<String> valueSet = newHashSet(value1, value2, value3);

        int numElements = random.intNumber(1, valueList.size());
        Collection<String> actualValues = random.someOf(valueList, numElements);
        assertThat(actualValues)
                .isSubsetOf(valueSet)
                .hasSize(numElements);

        numElements = random.intNumber(1, valueList.size());
        actualValues = random.someOf(valueSet, numElements);
        assertThat(actualValues)
                .isSubsetOf(valueSet)
                .hasSize(numElements);

        numElements = random.intNumber(1, valueList.size());
        assertThat(random.someOf(TestEnum.class, numElements))
                .isSubsetOf(EnumSet.allOf(TestEnum.class))
                .hasSize(numElements);
    }

    @Test
    void someOf_should_return_duplicates_if_input_contains_duplicates() {
        ValueProvider random = withRandomValues();

        String value1 = "1";
        String value2 = "1";
        List<String> valueList = newArrayList(value1, value2);
        Set<String> valueSet = newHashSet(value1, value2);

        assertThat(random.someOf(valueList, valueList.size()))
                .isSubsetOf(valueSet)
                .hasSize(valueList.size());
    }

    @Test
    void listOf_should_return_a_sensible_number_of_elements() {
        // given
        ValueProvider random = withRandomValues();

        // when
        List<MyBean> myBeans = random.listOf(MyBeanTestData::myBean);

        // then
        assertThat(myBeans).hasSizeLessThanOrEqualTo(5); // 5 is the default
    }

    @Test
    void nonEmptyListOf_should_return_at_least_one_element() {
        // given
        ValueProvider random = withRandomValues();

        // when
        List<MyBean> myBeans = random.nonEmptyListOf(MyBeanTestData::myBean);

        // then
        assertThat(myBeans).isNotEmpty()
                .hasSizeLessThanOrEqualTo(5); // 5 is the default
    }

    @Test
    void listOfContaining_should_return_the_provided_elements_plus_some_randomly_generated_elements() {
        // given
        ValueProvider random = withRandomValues();

        // when
        List<MyBean> myBeans = random.listOfContaining(MyBeanTestData::myBean, myBeanContained(1), myBeanContained(2), myBeanContained(3));

        // then
        assertThat(myBeans).hasSizeLessThanOrEqualTo(7)
                // 3 contained beans + max. 1 random 'spacing' bean between each + max. 1 random bean at the beginning/end
                .contains(myBeanContained(1), myBeanContained(2), myBeanContained(3));
    }

    @Test
    void ipV6Address_should_return_valid_IPv6_address() throws UnknownHostException {
        ValueProvider random = withRandomValues();

        InetAddress address = InetAddress.getByName(random.ipV6Address());

        assertThat(address).isInstanceOf(Inet6Address.class);
    }

    @Test
    void url_related_methods_should_fail_on_malformed_urls() {
        ValueProvider random = withRandomValues();

        assertFailsOnMalformedUrl(random::url);
        assertFailsOnMalformedUrl(random::httpUrl);
        assertFailsOnMalformedUrl(random::httpsUrl);
    }

    private void assertFailsOnMalformedUrl(Function<String, URL> urlCreator) {
        String illegalDomain = ":";
        assertThatThrownBy(() -> urlCreator.apply(illegalDomain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(illegalDomain);
    }

    private static ValueProvider withRandomValues() {
        return new ValueProvider(createRandomInitialization());
    }

    private ValueProvider withFixedValues() {
        return withFixedValues(0L);
    }

    private ValueProvider withFixedValues(long seed) {
        return new ValueProvider(createReproducibleInitialization(seed));
    }

    enum TestEnum {
        ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE
    }

    enum EmptyTestEnum {

    }

    private void assertIntNumber(int min, int max) {
        assertIntNumber(withRandomValues(), min, max);
        assertIntNumber(withFixedValues(), min, max);
    }

    private void assertIntNumber(ValueProvider provider, int min, int max) {
        assertThat(provider.intNumber(min, max))
                .isGreaterThanOrEqualTo(min)
                .isLessThanOrEqualTo(max);
    }

    private void assertLongNumber(long min, long max) {
        assertLongNumber(withRandomValues(), min, max);
        assertLongNumber(withFixedValues(), min, max);
    }

    private void assertLongNumber(ValueProvider provider, long min, long max) {
        long longNumber = provider.longNumber(min, max);
        assertThat(longNumber)
                .isGreaterThanOrEqualTo(min)
                .isLessThanOrEqualTo(max);
    }

    private void assertBigIntegerNumber(BigInteger min, BigInteger max) {
        assertBigIntegerNumber(withRandomValues(), min, max);
        assertBigIntegerNumber(withFixedValues(), min, max);
    }

    private void assertBigIntegerNumber(ValueProvider provider, BigInteger min, BigInteger max) {
        assertThat(provider.bigIntegerNumber(min, max))
                .isGreaterThanOrEqualTo(min)
                .isLessThanOrEqualTo(max);
    }

    private void assertBigDecimalNumber(Number min, Number max) {
        assertBigDecimalNumber(withRandomValues(), min, max);
        assertBigDecimalNumber(withFixedValues(), min, max);
    }

    private void assertBigDecimalNumber(ValueProvider provider, Number min, Number max) {
        assertThat(provider.bigDecimalNumber(min, max))
                .isGreaterThanOrEqualTo(new BigDecimal(min.doubleValue()))
                .isLessThanOrEqualTo(new BigDecimal(max.doubleValue()));
    }

    private void assertBigDecimalNumberWithScale2(Number min, Number max) {
        assertBigDecimalNumberWithScale2(withRandomValues(), min, max);
        assertBigDecimalNumberWithScale2(withFixedValues(), min, max);
    }

    private void assertBigDecimalNumberWithScale2(ValueProvider provider, Number min, Number max) {
        assertThat(provider.bigDecimalNumberWithScale(min, max, 2))
                .isGreaterThanOrEqualTo(new BigDecimal(min.doubleValue() - 0.01))
                .isLessThanOrEqualTo(new BigDecimal(max.doubleValue() + 0.01));
    }

    static class MyBeanTestData {
        public static MyBean myBean(ValueProvider valueProvider) {
            return new MyBean("randomly generated");
        }

        public static MyBean myBeanContained(int counter) {
            return new MyBean("contained" + counter);
        }
    }

    @Data
    static class MyBean {
        private final String value;
    }
}
