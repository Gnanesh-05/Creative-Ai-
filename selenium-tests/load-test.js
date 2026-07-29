const http = require('http');
const XLSX = require('xlsx');
const path = require('path');

const TARGET_URL = 'http://127.0.0.1:8000/api/v1/health';
const CONCURRENCY = 100; // 100 virtual users
const DURATION = 60000; // 60 seconds (1 minute)

console.log("====================================================");
console.log("       CREATIVE AI - BASELINE LOAD TESTING ENGINE    ");
console.log("====================================================");
console.log(`Target Endpoint:      ${TARGET_URL}`);
console.log(`Virtual Users (VU):  ${CONCURRENCY}`);
console.log(`Test Duration:        ${DURATION / 1000} seconds`);
console.log("Initializing load runner...");

let totalRequests = 0;
let successRequests = 0;
let failedRequests = 0;
const latencies = [];

let stopSignal = false;
const startTime = Date.now();

// Single virtual user request loop
function runVirtualUser() {
    if (stopSignal) return;

    const reqStartTime = Date.now();
    
    const req = http.get(TARGET_URL, (res) => {
        const latency = Date.now() - reqStartTime;
        totalRequests++;
        
        if (res.statusCode === 200) {
            successRequests++;
            latencies.push(latency);
        } else {
            failedRequests++;
        }

        // Consume response data to free up connection socket
        res.on('data', () => {});
        res.on('end', () => {
            // Trigger next request in loop immediately
            runVirtualUser();
        });
    });

    req.on('error', (err) => {
        totalRequests++;
        failedRequests++;
        runVirtualUser();
    });

    req.end();
}

// Start concurrent request loops
for (let i = 0; i < CONCURRENCY; i++) {
    runVirtualUser();
}

// Progress reporting intervals
const progressInterval = setInterval(() => {
    const elapsed = (Date.now() - startTime) / 1000;
    const currentRps = (totalRequests / elapsed).toFixed(1);
    console.log(`Elapsed: ${elapsed.toFixed(0)}s | Requests Sent: ${totalRequests} | Current RPS: ${currentRps}`);
}, 5000);

// Timer to stop load test
setTimeout(() => {
    stopSignal = true;
    clearInterval(progressInterval);
    const totalDurationMs = Date.now() - startTime;
    
    console.log("\nLoad execution complete. Calculating metrics...");
    
    setTimeout(() => {
        // Calculate performance metrics
        const rps = (totalRequests / (totalDurationMs / 1000)).toFixed(2);
        
        let min = 0;
        let max = 0;
        let avg = 0;
        
        if (latencies.length > 0) {
            min = latencies[0];
            max = latencies[0];
            let sum = 0;
            for (let i = 0; i < latencies.length; i++) {
                const val = latencies[i];
                if (val < min) min = val;
                if (val > max) max = val;
                sum += val;
            }
            avg = (sum / latencies.length).toFixed(1);
        }

        console.log("====================================================");
        console.log("                LOAD TESTING REPORT                 ");
        console.log("====================================================");
        console.log(`Total Requests Sent:   ${totalRequests}`);
        console.log(`Successful Responses:  ${successRequests}`);
        console.log(`Failed Responses:      ${failedRequests}`);
        console.log(`Requests per second:   ${rps} req/sec`);
        console.log("----------------------------------------------------");
        console.log("Response Time (Latency):");
        console.log(`  Average:             ${avg}ms`);
        console.log(`  Minimum:             ${min}ms`);
        console.log(`  Maximum:             ${max}ms`);
        console.log("====================================================");

        // Generate Excel report
        const reportPath = path.resolve(__dirname, 'Load_Test_Execution_Report.xlsx');
        console.log(`Saving Excel report to: ${reportPath}`);
        const wb = XLSX.utils.book_new();
        const summaryData = [
            ["CREATIVE AI BACKEND - BASELINE LOAD TESTING REPORT", "", ""],
            ["", "", ""],
            ["METRIC", "VALUE", "DESCRIPTION"],
            ["Target Endpoint", TARGET_URL, "Service health check URL"],
            ["Virtual Users (VU)", CONCURRENCY, "Concurrent loop worker count"],
            ["Test Duration", `${DURATION / 1000}s`, "Continuous execution window"],
            ["Total Requests Sent", totalRequests, "Accumulated request volume"],
            ["Successful Responses", successRequests, "HTTP status 200 count"],
            ["Failed Responses", failedRequests, "Socket errors or non-200 responses"],
            ["Requests Per Second (RPS)", Number(rps), "Throughput speed"],
            ["Average Latency", `${avg}ms`, "Mean response delay"],
            ["Minimum Latency", `${min}ms`, "Fastest response delay"],
            ["Maximum Latency", `${max}ms`, "Slowest response delay"]
        ];
        const wsSummary = XLSX.utils.aoa_to_sheet(summaryData);
        XLSX.utils.book_append_sheet(wb, wsSummary, "Load Test Summary");

        XLSX.writeFile(wb, reportPath);
        console.log("Load testing Excel report written successfully!");
    }, 1000); // Small cooldown buffer for pending request completions
}, DURATION);
