package dev.gaphunter.feignfallbackconfigcompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/** See [JavaFeignFallbackFinderTest]'s class KDoc for why every case here has no reachable config. */
class KotlinFeignFallbackFinderTest : BasePlatformTestCase() {

    fun `test FeignClient with fallback and no reachable config is flagged`() {
        val file = myFixture.configureByText(
            "OrderClient.kt",
            """
            @FeignClient(name = "orders", fallback = OrderClientFallback::class)
            interface OrderClient {
                fun getOrder(id: String): Order
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinFeignFallbackFinder.findAll(file).size)
    }

    fun `test FeignClient without any fallback attribute is not flagged`() {
        val file = myFixture.configureByText(
            "OrderClient.kt",
            """
            @FeignClient(name = "orders")
            interface OrderClient {
                fun getOrder(id: String): Order
            }
            """.trimIndent(),
        )
        assertTrue(KotlinFeignFallbackFinder.findAll(file).isEmpty())
    }
}
