package dev.gaphunter.feignfallbackconfigcompanion.config

import junit.framework.TestCase

class CircuitBreakerEnabledScannerTest : TestCase() {

    fun `test properties-style spring cloud openfeign key is detected`() {
        assertTrue(CircuitBreakerEnabledScanner.isEnabledIn("spring.cloud.openfeign.circuitbreaker.enabled=true"))
    }

    fun `test properties-style legacy feign key is detected`() {
        assertTrue(CircuitBreakerEnabledScanner.isEnabledIn("feign.circuitbreaker.enabled=true"))
    }

    fun `test properties-style key set to false is not detected as enabled`() {
        assertFalse(CircuitBreakerEnabledScanner.isEnabledIn("feign.circuitbreaker.enabled=false"))
    }

    fun `test yaml nested spring cloud openfeign block is detected`() {
        val yaml = """
            spring:
              cloud:
                openfeign:
                  circuitbreaker:
                    enabled: true
        """.trimIndent()
        assertTrue(CircuitBreakerEnabledScanner.isEnabledIn(yaml))
    }

    fun `test yaml nested legacy feign block is detected`() {
        val yaml = """
            feign:
              circuitbreaker:
                enabled: true
        """.trimIndent()
        assertTrue(CircuitBreakerEnabledScanner.isEnabledIn(yaml))
    }

    fun `test unrelated config text is not detected as enabled`() {
        val properties = """
            server.port=8080
            spring.application.name=order-service
        """.trimIndent()
        assertFalse(CircuitBreakerEnabledScanner.isEnabledIn(properties))
    }

    fun `test empty config text is not detected as enabled`() {
        assertFalse(CircuitBreakerEnabledScanner.isEnabledIn(""))
    }
}
