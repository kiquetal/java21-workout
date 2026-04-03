# Observability

## Metrics in Quarkus

Quarkus exposes metrics out of the box via Micrometer + Prometheus registry. No code needed — just the extension:

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

Metrics are available at:

```
http://localhost:8080/q/metrics
```

## What You Get for Free

Quarkus auto-instruments these without any code:

| Category | Example metrics |
|---|---|
| HTTP | `http_server_requests_seconds_count`, `http_server_requests_seconds_sum`, `http_server_requests_seconds_max` |
| JVM Memory | `jvm_memory_used_bytes`, `jvm_memory_committed_bytes`, `jvm_buffer_memory_used_bytes` |
| JVM Threads | `jvm_threads_live_threads`, `jvm_threads_daemon_threads`, `jvm_threads_states_threads` |
| GC | `jvm_gc_pause_seconds_count`, `jvm_gc_pause_seconds_sum` |
| Datasource | `agroal_active_count`, `agroal_available_count`, `agroal_awaiting_count` |
| Hibernate | `hibernate_sessions_open_total`, `hibernate_queries_total`, `hibernate_entities_inserts_total` |

### HTTP Metrics Breakdown

Every REST endpoint is tracked automatically. In Prometheus you can query:

```promql
# Request rate per endpoint
rate(http_server_requests_seconds_count{uri="/api/lendings"}[5m])

# Average response time
rate(http_server_requests_seconds_sum{uri="/api/lendings"}[5m])
  / rate(http_server_requests_seconds_count{uri="/api/lendings"}[5m])

# Error rate (4xx + 5xx)
rate(http_server_requests_seconds_count{uri="/api/lendings", status=~"4..|5.."}[5m])
```

## Adding Custom Metrics

Inject `MeterRegistry` when you need business-level metrics:

```java
@ApplicationScoped
public class LendingService {

    @Inject
    MeterRegistry registry;

    @Transactional
    public LendingResult lend(LendCommand command) {
        var result = // ... business logic
        switch (result) {
            case LendingResult.Success s -> registry.counter("lending.success").increment();
            case LendingResult.BookNotAvailable b -> registry.counter("lending.rejected", "reason", "book_unavailable").increment();
            case LendingResult.MemberNotFound m -> registry.counter("lending.rejected", "reason", "member_not_found").increment();
        }
        return result;
    }
}
```

Then query in Prometheus:

```promql
# Total successful lendings
lending_success_total

# Rejections by reason
lending_rejected_total{reason="book_unavailable"}
```

## Dev Mode

In `quarkus dev`, metrics are available immediately:

```bash
# See all metrics
curl http://localhost:8080/q/metrics

# Filter for HTTP metrics
curl -s http://localhost:8080/q/metrics | grep http_server
```

The Quarkus Dev UI (`http://localhost:8080/q/dev-ui`) also shows metrics under the Micrometer card.

## Docker Stack

```bash
./mvnw package -DskipTests
docker compose up --build
```

| Service | URL | Purpose |
|---|---|---|
| App | `http://localhost:8080` | REST API |
| Metrics | `http://localhost:8080/q/metrics` | Raw Prometheus format |
| Prometheus | `http://localhost:9090` | Query and store metrics |
| Grafana | `http://localhost:3000` | Dashboards (admin/admin) |

Prometheus scrapes `/q/metrics` every 15s. Grafana has Prometheus pre-configured as datasource.

## Useful Grafana Dashboard Panels

Once in Grafana, create a dashboard and add panels with these queries:

**Request Rate:**
```promql
sum(rate(http_server_requests_seconds_count[5m])) by (uri, method)
```

**Response Time (p95):**
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

**Active DB Connections:**
```promql
agroal_active_count
```

**JVM Heap Usage:**
```promql
jvm_memory_used_bytes{area="heap"}
```
