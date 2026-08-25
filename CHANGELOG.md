<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Feign Fallback Config Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning icon on a @FeignClient fallback/fallbackFactory whose
  project has no spring.cloud.openfeign.circuitbreaker.enabled=true
  (or the legacy feign.circuitbreaker.enabled=true) in a reachable
  config file -- the fallback silently never triggers without it.
- 100% static text/PSI analysis, Java and Kotlin, no network calls,
  no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/feign-fallback-config-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/feign-fallback-config-companion/commits/0.1.0
