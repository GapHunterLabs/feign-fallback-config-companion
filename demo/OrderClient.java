// Demo data for Feign Fallback Config Companion -- used with
// `./gradlew runIde` to capture the real Marketplace screenshot. Open
// this file (without an application.properties nearby enabling the
// circuit breaker), the warning should appear on the @FeignClient
// annotation.

@FeignClient(name = "orders", fallback = OrderClientFallback.class)
interface OrderClient {

    Order getOrder(String id);
}
