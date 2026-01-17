package com.tngtech.valueprovider;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.tngtech.valueprovider.CollectionGenerator.PrefixHandling.APPEND_TO_EXISTING;
import static com.tngtech.valueprovider.CollectionGenerator.PrefixHandling.REPLACE;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

/**
 * Provides a fluent API for fine-grained control when creating collections of objects.
 * Instances of this class are created via {@link AbstractValueProvider#collection()}.
 *
 * @param <VP> the {@link AbstractValueProvider} type to be used for element generation.
 */
public class CollectionGenerator<VP extends AbstractValueProvider<VP>> {
    static final int DEFAULT_MIN_COLLECTION_SIZE = 0;
    static final int DEFAULT_MIN_NONEMPTY_COLLECTION_SIZE = 1;
    static final int DEFAULT_MAX_COLLECTION_SIZE = 5;
    private static final int MAX_NUM_RETRIES_TO_CREATE_DIFFERENT_ELEMENTS = 5000;

    enum PrefixHandling {
        REPLACE,
        APPEND_TO_EXISTING
    }

    private final VP values;
    private int numElements;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Function<Integer, String>> elementIndexToPrefix;
    private PrefixHandling prefixHandling;

    static <VP extends AbstractValueProvider<VP>> CollectionGenerator<VP> of(VP values) {
        return new CollectionGenerator<>(values);
    }

    private CollectionGenerator(VP values) {
        this.values = values;
        numElements(values.intNumber(DEFAULT_MIN_COLLECTION_SIZE, DEFAULT_MAX_COLLECTION_SIZE));
        this.elementIndexToPrefix = empty();
    }

    /**
     * Controls the range from which the number of collection elements is chosen via {@link AbstractValueProvider#intNumber(int, int)}.
     *
     * @param minNumElements the minimum number (inclusive) of collection elements to create.
     * @param maxNumElements the maximum number (inclusive) of collection elements to create.
     *
     * @return this generator instance for further method invocation.
     *
     * @throws IllegalArgumentException if {@code minNumElements} &gt; {@code maxNumElements}.
     * @throws IllegalArgumentException for negative values of {@code minNumElements} (number chosen via {@link AbstractValueProvider#intNumber(int, int)} to be exact).
     *
     * @see #numElements(int)
     * @see #listOf(Function)
     */
    public CollectionGenerator<VP> numElements(int minNumElements, int maxNumElements) {
        return numElements(values.intNumber(minNumElements, maxNumElements));
    }

    /**
     * Controls the exact number of collection elements.
     *
     * @param numElements the number of collection elements to create.
     *
     * @return this generator instance for further method invocation.
     *
     * @throws IllegalArgumentException for negative values of {@code numElements}
     * @see #numElements(int, int)
     * @see #listOf(Function)
     */
    public CollectionGenerator<VP> numElements(int numElements) {
        checkArgument(numElements >= 0, "numElements %s must >= 0", numElements);
        this.numElements = numElements;
        return this;
    }

    /**
     * Controls the creation of the prefix for {@link AbstractValueProvider#copyWithChangedPrefix(String)} based on the zero-based element index.
     * The result replaces any currently set prefix of the {@link AbstractValueProvider} that created this generator.
     *
     * @param elementIndexToPrefix the {@link Function} to create the prefix number of collection elements to create.
     *
     * @return this generator instance for further method invocation.
     *
     * @see #appendToPrefixVia(Function)
     * @see #listOf(Function)
     */
    public CollectionGenerator<VP> replacePrefixVia(Function<Integer, String> elementIndexToPrefix) {
        return createPrefixVia(elementIndexToPrefix, REPLACE);
    }

    /**
     * Controls the creation of the prefix for {@link AbstractValueProvider#copyWithChangedPrefix(String)} based on the zero-based element index.
     * The result is appended to the currently set prefix of the {@link AbstractValueProvider} that created this generator.
     *
     * @param elementIndexToPrefix the {@link Function} to create the prefix number of collection elements to create.
     *
     * @return this generator instance for further method invocation.
     *
     * @see #replacePrefixVia(Function)
     * @see #listOf(Function)
     */
    public CollectionGenerator<VP> appendToPrefixVia(Function<Integer, String> elementIndexToPrefix) {
        return createPrefixVia(elementIndexToPrefix, APPEND_TO_EXISTING);
    }

    private CollectionGenerator<VP> createPrefixVia(Function<Integer, String> elementIndexToPrefix, PrefixHandling prefixHandling) {
        this.elementIndexToPrefix = Optional.of(elementIndexToPrefix);
        this.prefixHandling = prefixHandling;
        return this;
    }

    /**
     * Creates a {@link List} of &lt;T&gt; (by means of {@code elementGenerator}).
     * <p>
     * Example:
     * <pre>
     *          static class MyBeanTestDataFactory {
     *              public static MyBean myBean(ValueProvider valueProvider) {
     *                  // builds and returns your bean
     *              }
     *          }
     *
     *         ValueProvider vp = ValueProviderFactory.createRandomValueProvider();
     *         vp.collection()
     *           .numElements(1, 3)
     *           .replacePrefixVia(i -> String.format("%d-", (i+1)))
     *           .listOf(MyBeanTestDataFactory::myBean); // -> List[myBean_generated_prefix_1-, myBean_generated_prefix_2-]
     * </pre>
     * </p>
     *
     * @param elementGenerator a generator {@link Function} to generate T given an implementation of {@link AbstractValueProvider}.
     * @param <T>              the type of list elements.
     *
     * @return the created {@link List}.
     */
    public <T> List<T> listOf(Function<VP, T> elementGenerator) {
        return IntStream.range(0, numElements)
                .mapToObj(elementIndex -> createElement(elementGenerator, elementIndex))
                .collect(toList());
    }

    /**
     * Creates a {@link Set} of &lt;T&gt; (by means of {@code elementGenerator}).
     * <p>
     * Example:
     * <pre>
     *          static class MyBeanTestDataFactory {
     *              public static MyBean myBean(ValueProvider valueProvider) {
     *                  // builds and returns your bean
     *              }
     *          }
     *
     *         ValueProvider vp = ValueProviderFactory.createRandomValueProvider();
     *         vp.collection()
     *           .numElements(2)
     *           .replacePrefixVia(i -> String.format("%c", (char) ('A' + i)))
     *           .setOf(MyBeanTestDataFactory::myBean); // -> Set[myBean_generated_prefix_A, myBean_generated_prefix_B]
     * </pre>
     * </p>
     *
     * @param elementGenerator a generator {@link Function} to generate T given an implementation of {@link AbstractValueProvider}.
     * @param <T>              the type of set elements.
     *
     * @return the created {@link Set}.
     *
     * @throws IllegalArgumentException if creating enough different elements fails (wrt. {@link #equals(Object)} of type &lt;T&gt;).
     */
    public <T> Set<T> setOf(Function<VP, T> elementGenerator) {
        int i = 0;
        int numRetriesToCreateDifferentElement = 0;
        Set<T> result = new HashSet<>();
        while (result.size() < numElements && numRetriesToCreateDifferentElement < MAX_NUM_RETRIES_TO_CREATE_DIFFERENT_ELEMENTS) {
            boolean different = result.add(createElement(elementGenerator, i));
            if (!different) {
                numRetriesToCreateDifferentElement++;
            }
            i++;
        }
        if (numRetriesToCreateDifferentElement >= MAX_NUM_RETRIES_TO_CREATE_DIFFERENT_ELEMENTS) {
            String message = format("Unable to create %d different elements, giving up after %d retries",
                    numElements, numRetriesToCreateDifferentElement);
            throw new IllegalArgumentException(message);
        }
        return result;
    }

    /**
     * Convenience class for {@link Map} entries, i.e. key/value pairs.
     * @param <K> key type
     * @param <V> value type
     */
    public static class MapEntry<K, V> {
        private final K key;
        private final V value;

        /**
         * Factory method for key/value pair.
         * @return a {@link MapEntry} containing the provided {@code key} and {@code value}.
         * @param <K> key type, must not be {@code null}.
         * @param <V> value type
         * @throws IllegalArgumentException for {@code null} value for {@code key}
         */
        public static <K, V> MapEntry<K, V> entryOf(K key, V value) {
            checkArgument(key != null, "key must not be null");
            return new MapEntry<>(key, value);
        }

        private MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        void putInto(Map<K, V> map) {
            map.put(this.key, this.value);
        }

        /**
         * A {@link Map} of the required size can only be ensured by providing a respective number of different keys.
         * The method therefore only considers the key, and ignores the value.
         */
        @Override
        public int hashCode() {
            return key.hashCode();
        }

        /**
         * @see #hashCode()
         */
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MapEntry<?, ?> mapEntry = (MapEntry<?, ?>) o;
            return Objects.equals(key, mapEntry.key);
        }
    }

    /**
     * Creates a {@link Map} of &lt;K&gt;/&lt;V&gt; pairs (by means of {@code elementGenerator}).
     * <p>
     * Example:
     * <pre>
     *          static class MyBeanTestDataFactory {
     *              public static MyBean myBean(ValueProvider valueProvider) {
     *                  // builds and returns your bean
     *              }
     *          }
     *
     *         ValueProvider vp = ValueProviderFactory.createRandomValueProvider();
     *         vp.collection()
     *           .numElements(2)
     *           .replacePrefixVia(i -> String.format("%c", (char) ('A' + i)))
     *           .mapOf(vp -> {
     *             String key = vp.fixedDecoratedString("key");
     *             MyBean value = MyBeanTestDataFactory.myBean(vp);
     *             return MapEntry.entryOf(key, value);
     *           }); // -> Map[Akey -> myBean_generated_prefix_A, Bkey -> myBean_generated_prefix_B]
     * </pre>
     * </p>
     *
     * @param elementGenerator a generator {@link Function} to generate a {@link MapEntry} given an implementation of {@link AbstractValueProvider}.
     * @param <K> the key type of the generated {@link MapEntry} and created  {@link Map}.
     * @param <V> the value type of the generated {@link MapEntry} and created  {@link Map}.
     *
     * @return the created {@link Map}.
     *
     * @see MapEntry
     *
     * @throws IllegalArgumentException if creating enough different keys fails (wrt. {@link #equals(Object)} of type &lt;K&gt;).
     */
    public <K, V> Map<K, V> mapOf(Function<VP, MapEntry<K, V>> elementGenerator) {
        Set<MapEntry<K, V>> elements = setOf(elementGenerator);
        Map<K, V> result = new HashMap<>();
        elements.forEach(entry -> entry.putInto(result));
        return result;
    }

    private <T> T createElement(Function<VP, T> elementGenerator, int elementIndex) {
        VP prefixedValues = createValueProviderFor(elementIndex);
        return elementGenerator.apply(prefixedValues);
    }

    private VP createValueProviderFor(int elementIndex) {
        if (!elementIndexToPrefix.isPresent()) {
            return values;
        }
        String prefix = elementIndexToPrefix.get().apply(elementIndex);

        if (prefixHandling == APPEND_TO_EXISTING) {
            prefix = values.getPrefix() + prefix;
        }
        return values.copyWithChangedPrefix(prefix);
    }
}
