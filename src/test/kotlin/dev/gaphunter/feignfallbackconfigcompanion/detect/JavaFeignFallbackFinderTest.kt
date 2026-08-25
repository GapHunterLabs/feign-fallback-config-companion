package dev.gaphunter.feignfallbackconfigcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * `BasePlatformTestCase`'s in-memory fixture has no
 * application.properties/.yml anywhere on disk by default, so every
 * fixture in this test class represents the "no circuitbreaker config
 * reachable" case -- the exact scenario this plugin flags. The
 * complementary "config IS present and correctly enabled -> not
 * flagged" path is covered indirectly by
 * `CircuitBreakerEnabledScannerTest` (the scanner itself, which is
 * what `CircuitBreakerConfigLocator`'s result gets checked against) --
 * a real end-to-end test with an actual on-disk config file needs a
 * heavier fixture (`LightJavaCodeInsightFixtureTestCase` with a real
 * temp project directory), stated honestly as not covered by this
 * unit test class.
 */
class JavaFeignFallbackFinderTest : BasePlatformTestCase() {

    fun `test FeignClient with fallback and no reachable config is flagged`() {
        val file = myFixture.configureByText(
            "OrderClient.java",
            """
            @FeignClient(name = "orders", fallback = OrderClientFallback.class)
            interface OrderClient {
                Order getOrder(String id);
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaFeignFallbackFinder.findAll(file).size)
    }

    fun `test FeignClient with fallbackFactory and no reachable config is flagged`() {
        val file = myFixture.configureByText(
            "OrderClient.java",
            """
            @FeignClient(name = "orders", fallbackFactory = OrderClientFallbackFactory.class)
            interface OrderClient {
                Order getOrder(String id);
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaFeignFallbackFinder.findAll(file).size)
    }

    fun `test FeignClient without any fallback attribute is not flagged`() {
        val file = myFixture.configureByText(
            "OrderClient.java",
            """
            @FeignClient(name = "orders")
            interface OrderClient {
                Order getOrder(String id);
            }
            """.trimIndent(),
        )
        assertTrue(JavaFeignFallbackFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated annotation with a fallback attribute is not flagged`() {
        val file = myFixture.configureByText(
            "RetryConfig.java",
            """
            @Retryable(fallback = RetryFallback.class)
            class RetryConfig {
            }
            """.trimIndent(),
        )
        assertTrue(JavaFeignFallbackFinder.findAll(file).isEmpty())
    }
}
