# value-provider

value-provider is a free library that facilitates writing realistic test data and in turn better tests for your Java
application.  
It works best in conjunction with reusable test data factories that encapsulate creating valid instances for your data
objects.

value-provider consists of two major parts:

* the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) class which populates properties
  of test data objects with random values
* infrastructure for reproducing said random data in case of test failures (JUnit5 extension, JUnit4 rules)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update the tests as appropriate.

For further information, please refer to [CONTRIBUTING.md](CONTRIBUTING.md). For technical details, you may find the
sequence diagrams in the `doc` directory helpful.

## Installation

value-provider has the following prerequisites:

* Java 8 and above
* JUnit 5.5 and above for the JUnit5 infrastructure
* JUnit 4.12 and above for the JUnit4 infrastructure

### Gradle

```groovy
// core library
testImplementation 'com.tngtech.valueprovider:value-provider-core:1.4.0'

// infrastructure
// for JUnit 5
testImplementation 'com.tngtech.valueprovider:value-provider-junit5:1.4.0'
// alternatively, for JUnit 4
testImplementation 'com.tngtech.valueprovider:value-provider-junit4:1.4.0'
```

### Maven

```xml
<!-- ... -->
<dependencies>
    <!-- ... -->

    <!-- core library -->
    <dependency>
        <groupId>com.tngtech.valueprovider</groupId>
        <artifactId>value-provider-core</artifactId>
        <version>1.4.0</version>
        <scope>test</scope>
    </dependency>

    <!-- infrastructure -->
    <!-- for JUnit 5 -->
    <dependency>
        <groupId>com.tngtech.valueprovider</groupId>
        <artifactId>value-provider-junit5</artifactId>
        <version>1.4.0</version>
        <scope>test</scope>
    </dependency>
    <!-- alternatively, for JUnit 4 -->
    <dependency>
        <groupId>com.tngtech.valueprovider</groupId>
        <artifactId>value-provider-junit4</artifactId>
        <version>1.4.0</version>
        <scope>test</scope>
    </dependency>

    <!-- ... -->
</dependencies>
```

## Usage

We strongly recommend implementing reusable __test data factories__ that encapsulate creating valid instances for your
test data objects.

#### A Simple test data factory

Consider a simple [Product](example/src/main/java/com/tngtech/valueprovider/example/Product.java)<sup>1</sup>...:

```java
// ...
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class Product {
    @NonNull
    private final ProductCategory category;
    @NonNull
    private final String name;
    @NonNull
    private final String description;
}
```

... the test data factory would look like... (see
also [ProductTestDataFactory](example/src/test/java/com/tngtech/valueprovider/example/ProductTestDataFactory.java)):

```java
import com.tngtech.valueprovider.ValueProvider;

import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public class ProductTestDataFactory {

    private ProductTestDataFactory() {
    }

    public static Product createProduct() {
        return createProduct(createRandomValueProvider());
    }

    public static Product createProduct(ValueProvider values) {
        return Product.of(
                values.oneOf(ProductCategory.class),
                values.fixedDecoratedString("name"),
                values.fixedDecoratedString("description"));
    }
}
```

The [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) is used to

* select a [ProductCategory](example/src/main/java/com/tngtech/valueprovider/example/ProductCategory.java) at random
* populate the `name` and `description` properties with a so called *decorated* string

What is a decorated string? Let's have a look at the following example output when invoking
`ProductTestDataFactory.createProduct()` multiple times:

```
Product(category=CAR, name=nameaPr, description=descriptionaPr)
Product(category=COMPUTER, name=nameyBp, description=descriptionyBp)
Product(category=COMPUTER, name=namejeM, description=descriptionjeM)
```

The *decoration* is simply a 3 letter suffix that is appended to the base string provided as parameter to the
`fixedDecoratedString()` method.

Note that the suffix of all String properties of
one [Product](example/src/main/java/com/tngtech/valueprovider/example/Product.java) instance stays the same, since the
values are populated using the
same [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) instance. Conversely,
different [Product](example/src/main/java/com/tngtech/valueprovider/example/Product.java)
instances contain properties with different suffixes. This is achieved by invoking `createRandomValueProvider()`, when
calling
the [ProductTestDataFactory](example/src/test/java/com/tngtech/valueprovider/example/ProductTestDataFactory.java).

#### Complex test data factories

Having seen the basics, let's move on to a more complex example. Consider
an [Order](example/src/main/java/com/tngtech/valueprovider/example/Order.java):

```java
// ...
@Getter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Order {
    @Singular
    @NonNull
    private final ImmutableList<OrderItem> orderItems;
    @NonNull
    private final Customer customer;
    @NonNull
    private final Address shippingAddress;
    @NonNull
    private final Optional<Address> billingAddress;

    public Address getBillingAddress() {
        return billingAddress.orElse(shippingAddress);
    }
    // ...
}
```

This time, let's start with the example output of invoking `OrderTestDataFactory.createOrder()`:

```
Order(
orderItems=[
OrderItem(product=Product(category=COMPUTER, name=AnameFhG, description=AdescriptionFhG), quantity=66),
OrderItem(product=Product(category=FOOD, name=BnameFhG, description=BdescriptionFhG), quantity=8),
OrderItem(product=Product(category=COMPUTER, name=CnameFhG, description=CdescriptionFhG), quantity=35),
OrderItem(product=Product(category=BOOK, name=DnameFhG, description=DdescriptionFhG), quantity=89)],
customer=Customer(firstName=firstNameFhG, lastName=lastNameFhG, birthDate=1975-05-06),
shippingAddress=Address(zip=96874, city=S-cityFhG, street=S-streetFhG, number=315),
billingAddress=Address(zip=32924, city=B-cityFhG, street=B-streetFhG, number=120))
Order(
orderItems=[
OrderItem(product=Product(category=MAGIC_EQUIPMENT, name=Anamerwk, description=Adescriptionrwk), quantity=1),
OrderItem(product=Product(category=MAGIC_EQUIPMENT, name=Bnamerwk, description=Bdescriptionrwk), quantity=69),
OrderItem(product=Product(category=COMPUTER, name=Cnamerwk, description=Cdescriptionrwk), quantity=85)],
customer=Customer(firstName=firstNamerwk, lastName=lastNamerwk, birthDate=2002-06-08),
shippingAddress=Address(zip=08583, city=cityrwk, street=streetrwk, number=86),
billingAddress=Address(zip=08583, city=cityrwk, street=streetrwk, number=86))
Order(
orderItems=[
OrderItem(product=Product(category=COMPUTER, name=Anamekwh, description=Adescriptionkwh), quantity=73)],
customer=Customer(firstName=firstNamekwh, lastName=lastNamekwh, birthDate=1929-08-23),
shippingAddress=Address(zip=81571, city=S-citykwh, street=S-streetkwh, number=174),
billingAddress=Address(zip=71331, city=B-citykwh, street=B-streetkwh, number=169))
```

Note that the 3 letter suffix that we already saw in
the [Product](example/src/main/java/com/tngtech/valueprovider/example/Product.java) example is shared for the entire
hierarchy of objects that
comprise an order. It therefore eases recognizing objects that belong together.

The output also demonstrates further aspects of randomization in test data factories. The 3 order objects all have a
different number of order items, and e.g., the shipping and billing addresses have
random zip codes or house numbers.

Last but not least, note the second aspect of string *decoration*. The products in the order items have an additional
prefix in their string properties (e.g., 'A', 'B', ...). This is required to
differentiate multiple objects of the same kind. The same applies to the shipping and billing addresses. They have
different prefixes, if they differ ('S-' vs. 'B-'), but no prefix, if they are the
same.

Let's take a look at
[OrderTestDataFactory](example/src/test/java/com/tngtech/valueprovider/example/OrderTestDataFactory.java) - how to
achieve all this:

```java
import com.tngtech.valueprovider.example.Order.OrderBuilder;
import com.tngtech.valueprovider.ValueProvider;

import static com.tngtech.valueprovider.example.AddressTestDataFactory.createAddress;
import static com.tngtech.valueprovider.example.CustomerTestDataFactory.createCustomer;
import static com.tngtech.valueprovider.example.OrderItemTestDataFactory.createOrderItem;
import static com.tngtech.valueprovider.ValueProviderFactory.createRandomValueProvider;

public final class OrderTestDataFactory {

    private OrderTestDataFactory() {
    }

    public static Order createOrder() {
        return createOrder(createRandomValueProvider());
    }

    public static Order createOrder(ValueProvider values) {
        return createOrderBuilder(values).build();
    }

    public static OrderBuilder createOrderBuilder() {
        return createOrderBuilder(createRandomValueProvider());
    }

    public static OrderBuilder createOrderBuilder(ValueProvider values) {
        OrderBuilder builder = Order.builder()
                .customer(createCustomer(values))
                .orderItems(createItems(values));
        setAddresses(builder, values);
        return builder;
    }

    private static List<OrderItem> createItems(ValueProvider values) {
        return values.collection()
                .numElements(1, 5)
                .replacePrefixVia(i -> format("%c", (char) ('A' + i)))
                .listOf(OrderItemTestDataFactory::createOrderItem);
    }

    private static void setAddresses(OrderBuilder builder, ValueProvider values) {
        boolean useDifferentBillingAddress = values.booleanValue();
        if (useDifferentBillingAddress) {
            builder
                    .shippingAddress(createAddress(values.copyWithChangedPrefix("S-")))
                    .billingAddress(createAddress(values.copyWithChangedPrefix("B-")));

        } else {
            builder
                    .shippingAddress(createAddress(values));
        }
    }
}
```

Sharing the same suffix for the entire object hierarchy is easy, just pass
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) instance to each invoked test data
factory as in:

```java
// ...
public final class OrderTestDataFactory {
    // ...
    public static OrderBuilder createOrderBuilder(ValueProvider values) {
        OrderBuilder builder = Order.builder()
                .customer(createCustomer(values))
        // ...
        return builder;
    }
    // ...
}
```

If you need multiple objects of the same type, e.g. different shipping and billing addresses,
you can achieve different string properties via a prefix:

```java
// ...
public final class OrderTestDataFactory {
    // ...
    private static void setAddresses(OrderBuilder builder, ValueProvider values) {
        boolean useDifferentBillingAddress = values.booleanValue();
        if (useDifferentBillingAddress) {
            builder
                    .shippingAddress(createAddress(values.copyWithChangedPrefix("S-")))
                    .billingAddress(createAddress(values.copyWithChangedPrefix("B-")));
        } else {
            builder
                    .shippingAddress(createAddress(values));
        }
    }
    // ...
}
```

The `copyWithChangedPrefix()` method takes the suffix of
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) for which it is called, and creates
a new instance with the passed prefix. Like the suffix, the prefix remains the same for the lifetime of
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java).

If you need a collection of objects of the same type, e.g. the order items, start with the `collection()` method.
The returned [CollectionGenerator](core/src/main/java/com/tngtech/valueprovider/CollectionGenerator.java)
provides a fluent API to specify the details of the collection:

```java
// ...
public final class OrderTestDataFactory {
    // ...
    private static List<OrderItem> createItems(ValueProvider values) {
        return values.collection()
                .numElements(1, 5)
                .replacePrefixVia(i -> format("%c", (char) ('A' + i)))
                .listOf(OrderItemTestDataFactory::createOrderItem);
    }
    // ...
}
```

In addition to the number of elements, the generation of the prefix for each collection element based on the element index can be controlled.
The [CollectionGenerator](core/src/main/java/com/tngtech/valueprovider/CollectionGenerator.java)
supports replacing any previously set prefix as in the example above. Alternatively, appending to the previously set prefix is possible as well.
This will yield unique string properties for nested collections.
Finally, the `listOf()` method creates the desired list of order items via the passed element generator function.
The [CollectionGenerator](core/src/main/java/com/tngtech/valueprovider/CollectionGenerator.java)
also provides a `setOf()` method for creating sets.

A final aspect that is related to the use of lombok for the example objects:
As opposed to a [Product](example/src/main/java/com/tngtech/valueprovider/example/Product.java), creating
an [Order](example/src/main/java/com/tngtech/valueprovider/example/Order.java) is done via a
builder rather than via a factory method.
The [OrderTestDataFactory](example/src/test/java/com/tngtech/valueprovider/example/OrderTestDataFactory.java) therefore
has 4 methods, a pair
of `createOrder()` and a pair of `createOrderBuilder()`
methods. Again, one of each pair has
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) as parameter to allow passing it on
to invoked test data factories. The other
one without parameter creates a new
random [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java).

### Using factories in tests

Now comes the easy part: If you need a valid data object for your test, but don't care about its content, create one. If
you need more than one data object with different but valid data, create
another one:

```java
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrder;
// ...

class MyOrderTest {
    @Test
    void do_something_with_a_single_order() {
        Order anOrder = createOrder();
        // ...
    }

    @Test
    void do_something_with_two_different_orders() {
        Order anOrder = createOrder();
        Order anotherOrder = createOrder();
        // ...
    }
}
```

If you want to control specific aspects of a data object that are important for your test, restrict your test code to
only these aspects:

```java
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrderBuilder;
// ...

class MyOrderTest {
    // ...
    @Test
    void shipping_address_is_used_as_default_for_billing_address() {
        Order useShippingAddressAsBillingAddress = createOrderBuilder()
                .billingAddress(empty())
                .build();
        // ...
    }
}
```

Please refer to [OrderTest](example/src/test/java/com/tngtech/valueprovider/example/OrderTest.java) for more examples,
and to
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java)
and its Javadoc to learn more about the methods it offers to populate the properties of your data objects.

### Reproducing test failures caused by random data

As you have learned by now, using randomness helps minimize the code for creating test data. However, this comes at a
price: If you want to reproduce test failures that might be related to random
data, especially fom your CI suite of hundreds or even thousands of tests, it is vital to use the same data.

value-provider supports this use case out of the box by providing infrastructure for reproducing test failures.

#### Infrastructure

##### JUnit 5

For __JUnit5__, use the
[ValueProviderExtension](junit5/src/main/java/com/tngtech/valueprovider/ValueProviderExtension.java):

```java
import com.tngtech.valueprovider.ValueProviderExtension;
// ...

@ExtendWith(ValueProviderExtension.class)
class MyOrderTest {
    // ...
}
```

If your test class is __derived__ from a base class,
the [ValueProviderExtension](junit5/src/main/java/com/tngtech/valueprovider/ValueProviderExtension.java)
may be specified in the __base class__ of the inheritance hierarchy,
so that it need not be specified in every derived test class.

###### JUnit 5 @TestInstance Lifecycle

JUnit 5 allows to control the instantiation (lifecycle in JUnit terms) of the test class.
As an alternative to the default lifecycle `PER_METHOD`, i.e. new instantiation of the test class for each test method
execution,
it supports `PER_CLASS`, i.e. single instantiation of the test class for execution of all test method  
(see [JUnit documentation](https://junit.org/junit5/docs/current/user-guide/#writing-tests-test-instance-lifecycle)
for details).

The [ValueProviderExtension](junit5/src/main/java/com/tngtech/valueprovider/ValueProviderExtension.java) supports both
lifecycles since version 1.3.0. There are some subtle differences
when it comes to reproducing test failures. Please refer to
the [respective part of this documentation](#reproducing-test-failures-and-test-lifecycle) for details

##### JUnit 4

For __JUnit4__, use the
[ValueProviderRule](junit4/src/main/java/com/tngtech/valueprovider/ValueProviderRule.java):

```java
import com.tngtech.valueprovider.ValueProviderRule;
// ...

public class MyOrderTest {
    @Rule
    public ValueProviderRule valueProviderRule = new ValueProviderRule();
    // ...
}
```

If your test uses __static__ test data created by using
a [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java), use the
[ValueProviderClassRule](junit4/src/main/java/com/tngtech/valueprovider/ValueProviderClassRule.java) in addition:

```java
import com.tngtech.valueprovider.ValueProviderClassRule;
import com.tngtech.valueprovider.ValueProviderRule;
// ...
import static com.tngtech.valueprovider.example.OrderTestDataFactory.createOrder;
// ...

public class MyOrderTest {
    @ClassRule
    public static final ValueProviderClassRule staticProviders = new ValueProviderClassRule();
    @Rule
    public ValueProviderRule instanceProviders = new ValueProviderRule();

    // static test data
    private static final Order DEFAULT_ORDER = createOrder();
    // ...
}
```

If your test class is __derived__ from a base class, make sure to specify the rule(s)
in the __base class__ of the inheritance hierarchy. Otherwise, your test (or other tests in a CI suite) may fail.

#### Reproducing test failures

If a test using the infrastructure fails, it provides information about the seed values used for generating the random
data as shown in the following example:

```
org.junit.ComparisonFailure:
Expected :"testUserName1"
Actual   :"testUserName"
<Click to see difference>
...
    Suppressed: com.tngtech.valueprovider.ValueProviderException: If the failure is related to random ValueProviders, specify the following system properties for the JVM to reproduce:
-Dvalue.provider.factory.test.class.seed=0
-Dvalue.provider.factory.test.method.seed=-1608847119246027406
-Dvalue.provider.factory.reference.date.time=2021-06-04T15:28:34.004
at com.tngtech.valueprovider.ValueProviderRule.handleFailure(ValueProviderRule.java:57)
at com.tngtech.valueprovider.ValueProviderRule.access$100(ValueProviderRule.java:17)
at com.tngtech.valueprovider.ValueProviderRule$1.evaluate(ValueProviderRule.java:38)
...
```

If the failure is related to random data, you can easily reproduce it. Just specify the above shown JVM system
properties in the command line when you re-run the failed test, e.g., in your IDE:

```
-Dvalue.provider.factory.test.class.seed=0
-Dvalue.provider.factory.test.method.seed=-1608847119246027406
-Dvalue.provider.factory.reference.date.time=2021-06-04T15:28:34.004
```

##### Reproducing test failures and test lifecycle

For JUnit 4 and JUnit 5 with the default test lifecycle `PER_METHOD`, seed values relate to __individual test methods__
within a test class, even if they have been run in a CI build together with other tests. Thus, it is sufficient to only
rerun __the individual test method__ to reproduce the failure.

For the alternative JUnit 5 lifecycle `PER_CLASS`, seed values relate to __individual test classes__. Thus, you have to
rerun __all test methods__ of the test class (up to and including the failed one) to reproduce the failure. In addition,
we would highly recommend to ensure a defined execution sequence of the test methods via a respective `@TestMethodOrder`
annotation.

JUnit 5 also supports inner `@Nested` test classes to ease structuring your tests (see
[JUnit documentation](https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested)
for details). The nesting may be arbitrarily deep, i.e. `@Nested` classes may contain further `@Nested` classes. The
test lifecycle may be chosen individually for the main test class as well as for each `@Nested` test class. As long as
the main test class and all `@Nested` test classes use the default test lifecycle `PER_METHOD`, it is again sufficient
to rerun the individual test method, regardless if it is in the main class or any nested class. As soon as the lifecycle
`PER_CLASS` is used for one or more classes in the nesting hierarchy where the failure occured, you have to re-run __all
test methods of this hierarchy__ of test classes to reproduce the failure. For convenience, the failure message
generated by the infrastructure provides the name of the root test class of this hierarchy in addition to the seed
values as shown in the following example:

```
"If the failure is related to random ValueProviders, re-run all tests of 'com.tngtech.valueprovider.ValueProviderExceptionTest' and specify the following system properties for the JVM to reproduce:
-Dvalue.provider.factory.test.class.seed=0
-Dvalue.provider.factory.test.method.seed=-5385145878463633929
-Dvalue.provider.factory.reference.date.time=2024-12-09T17:02:50.109"
```

### Reproducible ValueProviders

The above example code always used `ValueProviderFactory.createRandomValueProvider()`
to create a [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) that, in turn, generates
random data. To be more precise,
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java)
is initialized with a random seed and will generate exactly the same data, if it is initialized with the same seed, and
the same sequence of method invocations is executed. As you may have guessed
already, reproducing test failures is based on this functionality.

So, if you need reproducible test data, e.g., to test a transformation of Java data to XML by verifying against a
previously stored XML file,
use `ValueProviderFactory.createReproducibleValueProvider()`
and provide a seed value of your choice to create
the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java). Your test data factories will
accept this [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) just
as any other one.

### Extending ValueProvider functionality

The [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) offers a considerable amount of
common methods to fill properties of test data objects. Sooner or later however,
the need will arise to add project specific functionality.

We advise the following approach:

* Create your own [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) class. Let it extend
  the [AbstractValueProvider](core/src/main/java/com/tngtech/valueprovider/AbstractValueProvider.java) provided by this
  library, and add the methods you need
* Create your own [ValueProviderFactory](core/src/main/java/com/tngtech/valueprovider/ValueProviderFactory.java) class
  and implement 2 static methods that create instances of
  your derived [AbstractValueProvider](core/src/main/java/com/tngtech/valueprovider/AbstractValueProvider.java) class
    * `createRandomValueProvider()`
    * `createReproducibleValueProvider()`
* Use your own classes instead of the ones provided by this library in your test data factories and other test code.

Refer
to [CustomValueProvider](example/src/main/java/com/tngtech/valueprovider/example/customprovider/CustomValueProvider.java)
,
[CustomValueProviderFactory](example/src/main/java/com/tngtech/valueprovider/example/customprovider/CustomValueProviderFactory.java)
,
and [CustomValueProviderFactoryTest](example/src/test/java/com/tngtech/valueprovider/example/customprovider/CustomValueProviderFactoryTest.java)
for a fully functional example.

### Limitations wrt. multithreaded tests

The infrastructure uses thread-local data to store the seed values and can therefore be used in parallel CI builds
without any problems.

However, reproducing test failures is __not possible for multithreaded test code__. Likewise,
a [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java)
initialized with a fixed seed value will not necessarily generate the same sequence of data if it is used by multiple
threads,
as the sequence of method invocations from different threads is not reproducible,
and neither the [ValueProvider](core/src/main/java/com/tngtech/valueprovider/ValueProvider.java) nor the infrastructure
provide any synchronisation.

## License

value-provider is published under the Apache License 2.0, see [license file](LICENSE) for details.

<sup>1</sup>Please note that we use [lombok](https://projectlombok.org/) and immutable data objects in our examples for
convenience, but this is not a requirement for using value-provider.
