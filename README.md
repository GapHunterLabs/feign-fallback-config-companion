# Feign Fallback Config Companion

Warning icon on a `@FeignClient(..., fallback = X.class)` or
`fallbackFactory = X.class` annotation whose project has no
`spring.cloud.openfeign.circuitbreaker.enabled=true` (or the legacy
`feign.circuitbreaker.enabled=true`) anywhere in a reachable config
file — Spring Cloud OpenFeign's own reference docs are explicit: "If
Spring Cloud CircuitBreaker is on the classpath and
spring.cloud.openfeign.circuitbreaker.enabled=true, Feign will wrap
all methods with a circuit breaker." Without that property, the
fallback/fallbackFactory attribute is silently never triggered — a
real, documented footgun.

## Why it exists

`@FeignClient(name = "orders", fallback = OrderClientFallback.class)`
compiles fine and looks correct — but if the one property that
actually activates Feign's circuit breaker wrapping is missing from
config, that fallback class is dead code: it's never called, and the
real exception from a failed call propagates straight through
instead. Nothing in the compiler or the annotation itself warns you.

## Why built this way

- **100% static text/PSI analysis** — matches the annotation name by
  simple text and cross-checks `application.properties`/`.yml`/`.yaml`
  via a bounded upward directory walk (never a full-project index
  scan). Java and Kotlin.
- **Confirmed gap**: no competing JetBrains Marketplace plugin covers
  this specific config cross-check — the existing Feign-related
  plugins (SoFast Feign Client Generator, Feign-Helper, FeignClient
  Assistant) are code generators/navigators, not correctness checks.

## v0.1 scope — stated honestly, not exhaustively

Matches by simple annotation name, not real type resolution. The
config lookup only walks up from the annotated file's own directory —
a multi-module project where config lives in an unrelated sibling
module can produce a false positive.

## Usage

Open any Java/Kotlin file declaring a `@FeignClient` with a fallback.
If the circuit breaker isn't enabled in a reachable config file, a
warning icon shows.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
