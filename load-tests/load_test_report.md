# Baseline Load Testing Summary Report

## Execution Metadata
- **Target URL**: `http://localhost:5173/`
- **Simulated Concurrent Users (VUs)**: 100
- **Target Duration**: 60 seconds
- **Actual Run Duration**: 60.03 seconds

---

## Performance Summary Metrics

| Metric | Result | Meaning |
| :--- | :--- | :--- |
| **Total Requests Sent** | 97597 | Total queries processed |
| **Requests Per Second (RPS)** | 1626 req/sec | Average API throughput |
| **Pass Rate** | 100.0% | Percentage of successful assertions |
| **Min Latency** | 39 ms | Fastest response time |
| **Average Latency** | 61 ms | Standard expected response time |
| **Max Latency** | 352 ms | Slowest response time |

---

## QA Assertion Summary
All requests matched the target expectation (response validation, HTTP status codes equivalent). 
**Test Status: PASSED**
