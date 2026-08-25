package dev.gaphunter.feignfallbackconfigcompanion.config

/**
 * Plain-text scan for whether a Spring Boot config file (`.properties`
 * or YAML) enables Feign's circuit breaker -- the property that makes
 * `@FeignClient(fallback = ...)` actually work (Spring Cloud OpenFeign
 * reference docs: "If Spring Cloud CircuitBreaker is on the classpath
 * and spring.cloud.openfeign.circuitbreaker.enabled=true, Feign will
 * wrap all methods with a circuit breaker"). Checks both the current
 * property name and the legacy `feign.circuitbreaker.enabled` prefix
 * (both still recognized by Spring Cloud OpenFeign).
 *
 * No YAML/properties PSI dependency -- same "indentation scanning,
 * plain text" principle already used catalog-wide for config files
 * (`k8s-resource-limit-companion`, `connection-pool-config-companion`).
 * Deliberately permissive about surrounding whitespace/quotes so a
 * `.properties` line (`key=value`) and a YAML nested key
 * (`  circuitbreaker:\n    enabled: true`) are both recognized,
 * without needing a real YAML parser.
 */
object CircuitBreakerEnabledScanner {

    private val PROPERTIES_STYLE = Regex(
        """(?:spring\.cloud\.openfeign|feign)\.circuitbreaker\.enabled\s*[:=]\s*['"]?true['"]?""",
        RegexOption.IGNORE_CASE,
    )

    // YAML nested form: a `circuitbreaker:` block containing an
    // `enabled: true` line at greater indentation, itself nested under
    // `feign:` or `openfeign:` (under `spring.cloud.` for the latter).
    // v0.1 doesn't do a real YAML parse -- looks for the two keys on
    // separate lines within a bounded window, which is honestly
    // reusable across most real-world formatting styles without
    // needing full indentation-tree logic.
    private val YAML_CIRCUITBREAKER_BLOCK = Regex(
        """circuitbreaker\s*:\s*\n(?:.*\n){0,3}?\s*enabled\s*:\s*['"]?true['"]?""",
        setOf(RegexOption.IGNORE_CASE),
    )

    fun isEnabledIn(configText: String): Boolean {
        if (PROPERTIES_STYLE.containsMatchIn(configText)) return true
        return YAML_CIRCUITBREAKER_BLOCK.containsMatchIn(configText)
    }
}
