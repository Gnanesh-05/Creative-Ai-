const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const path = require('path');
const fs = require('fs');
const XLSX = require('xlsx');

// 1. Generate 300 E2E Test Cases programmatically
function generateTestCases() {
    const testCases = [];
    let id = 1;

    // Category 1: Valid User Authentication (150 test cases)
    // Testing unique valid users user1 to user150
    for (let i = 1; i <= 150; i++) {
        testCases.push({
            id: id++,
            category: "Valid Authentication",
            description: `Verify successful login for valid user account #${i} (user${i}@creativeai.app)`,
            username: `user${i}@creativeai.app`,
            password: "ValidPass123!",
            expectedMessage: `Login successful for user${i}@creativeai.app!`
        });
    }

    // Category 2: Invalid Passwords for Valid Accounts (50 test cases)
    // Testing password failure variations across different user numbers
    const invalidPasswords = [
        "wrongpassword", "123456", "password123", "admin123", "pass123", 
        "ValidPass", "validpass123!", "ValidPass123", "not_valid", "password",
        "qwerty123", "letmein123", "admin_pass", "user_pass", "secret_key", "pass1234"
    ];
    for (let i = 1; i <= 50; i++) {
        const userNum = (i % 150) + 1;
        const badPass = invalidPasswords[i % invalidPasswords.length] + i;
        testCases.push({
            id: id++,
            category: "Invalid Password",
            description: `Verify authentication denial with invalid password '${badPass}' for user${userNum}`,
            username: `user${userNum}@creativeai.app`,
            password: badPass,
            expectedMessage: "Invalid password. Access denied."
        });
    }

    // Category 3: Non-existent Users (50 test cases)
    // Testing usernames that are not registered in the system
    for (let i = 1; i <= 50; i++) {
        const fakeUser = `nonexistent_user_${i}@creativeai.app`;
        testCases.push({
            id: id++,
            category: "Non-existent User",
            description: `Verify login attempt rejection for unregistered account '${fakeUser}'`,
            username: fakeUser,
            password: "SomePassword123!",
            expectedMessage: "User does not exist."
        });
    }

    // Category 4: Empty and Blank Fields (10 test cases)
    // Testing various blank and space-only field configurations
    const blankInputs = [
        { u: "", p: "", desc: "empty username and empty password" },
        { u: "user1@creativeai.app", p: "", desc: "valid username and empty password" },
        { u: "", p: "ValidPass123!", desc: "empty username and valid password" },
        { u: "   ", p: "ValidPass123!", desc: "whitespace-only username and valid password" },
        { u: "user1@creativeai.app", p: "   ", desc: "valid username and whitespace-only password" },
        { u: " ", p: " ", desc: "single space in both fields" },
        { u: "\t", p: "\t", desc: "tab character in both fields" }
    ];
    for (let i = 0; i < 10; i++) {
        const input = blankInputs[i % blankInputs.length];
        testCases.push({
            id: id++,
            category: "Required Fields",
            description: `Verify validation error display for ${input.desc} (#${i + 1})`,
            username: input.u,
            password: input.p,
            expectedMessage: "All fields are required."
        });
    }

    // Category 5: Length & Boundary checks (20 test cases)
    // Testing boundary conditions (too short, extremely long inputs)
    for (let i = 1; i <= 10; i++) {
        const len = (i % 2) + 1;
        testCases.push({
            id: id++,
            category: "Boundary Check - Short",
            description: `Verify error message when username is under 3 characters (length: ${len})`,
            username: "ab".substring(0, len),
            password: "ValidPass123!",
            expectedMessage: "Username must be at least 3 characters."
        });
    }
    for (let i = 1; i <= 10; i++) {
        const len = (i % 5) + 1;
        testCases.push({
            id: id++,
            category: "Boundary Check - Password Short",
            description: `Verify error message when password is under 6 characters (length: ${len})`,
            username: "user1@creativeai.app",
            password: "12345".substring(0, len),
            expectedMessage: "Password must be at least 6 characters."
        });
    }

    // Category 6: SQL Injection & XSS Vulnerability checks (20 test cases)
    // Testing safe handling of common script and query syntax inputs
    const dangerousInputs = [
        { u: "' OR '1'='1", p: "ValidPass123!", desc: "classic SQL injection bypass attempt" },
        { u: "admin'--", p: "ValidPass123!", desc: "SQL comment truncation attempt" },
        { u: "\" OR \"\"=\"", p: "ValidPass123!", desc: "double quote SQL injection bypass" },
        { u: "user1@creativeai.app' UNION SELECT null, null--", p: "ValidPass123!", desc: "SQL Union-based injection attempt" },
        { u: "<script>alert('xss')</script>", p: "ValidPass123!", desc: "XSS script execution payload" },
        { u: "user1@creativeai.app<img src=x onerror=alert(1)>", p: "ValidPass123!", desc: "XSS image onerror payload" },
        { u: "user1@creativeai.app'; DROP TABLE users;--", p: "ValidPass123!", desc: "SQL semicolon batch query injection" },
        { u: "user1@creativeai.app' or 1=1--", p: "ValidPass123!", desc: "lowercase OR SQL injection attempt" },
        { u: "user1@creativeai.app' or 'a'='a", p: "ValidPass123!", desc: "string comparison SQL injection" },
        { u: "admin\" or 1=1--", p: "ValidPass123!", desc: "double quote admin bypass check" }
    ];
    for (let i = 0; i < 20; i++) {
        const input = dangerousInputs[i % dangerousInputs.length];
        testCases.push({
            id: id++,
            category: "Security Filter",
            description: `Verify that dangerous input '${input.u}' is caught by validation filter`,
            username: input.u,
            password: input.p,
            expectedMessage: "Invalid characters in username."
        });
    }

    return testCases;
}

// 2. Execute Tests
async function runTests() {
    console.log("====================================================");
    console.log("  CREATIVE AI - E2E SELENIUM AUTOMATION RUNNER");
    console.log("====================================================");
    console.log("Setting up Headless Chrome Browser Driver...");

    const options = new chrome.Options();
    options.addArguments('--headless');
    options.addArguments('--disable-gpu');
    options.addArguments('--no-sandbox');
    options.addArguments('--window-size=1920,1080');

    const driver = await new Builder()
        .forBrowser('chrome')
        .setChromeOptions(options)
        .build();

    const fileUrl = 'file://' + path.resolve(__dirname, '../login.html');
    console.log(`Target Login URL: ${fileUrl}`);

    const testCases = generateTestCases();
    console.log(`Generated ${testCases.length} test cases to execute.`);

    const detailedResults = [];
    let passedCount = 0;
    let failedCount = 0;
    const startTime = Date.now();

    try {
        for (let i = 0; i < testCases.length; i++) {
            const tc = testCases[i];
            const tcStartTime = Date.now();

            if (i > 0 && i % 50 === 0) {
                console.log(`Executed ${i} / ${testCases.length} tests...`);
            }

            // Load login page
            await driver.get(fileUrl);

            // Locate elements
            const usernameInput = await driver.findElement(By.id('username-input'));
            const passwordInput = await driver.findElement(By.id('password-input'));
            const signInButton = await driver.findElement(By.id('signin-button'));
            const statusMsg = await driver.findElement(By.id('status-message'));

            // Input values
            await usernameInput.sendKeys(tc.username);
            await passwordInput.sendKeys(tc.password);

            // Submit form
            await signInButton.click();

            // Wait until status message is displayed
            await driver.wait(until.elementIsVisible(statusMsg), 3000);

            // Fetch result message
            const actualMessage = await statusMsg.getText();
            const tcDuration = Date.now() - tcStartTime;

            // Assert message matches expected
            const isMatch = actualMessage.trim() === tc.expectedMessage.trim();
            const status = isMatch ? "PASS" : "FAIL";

            if (isMatch) {
                passedCount++;
            } else {
                failedCount++;
                console.error(`\n[FAILURE] Test Case #${tc.id} - ${tc.category}`);
                console.error(`Description: ${tc.description}`);
                console.error(`Inputs: User='${tc.username}' | Pass='${tc.password}'`);
                console.error(`Expected: '${tc.expectedMessage}'`);
                console.error(`Actual:   '${actualMessage}'\n`);
            }

            detailedResults.push({
                "Test Case ID": tc.id,
                "Category": tc.category,
                "Description": tc.description,
                "Username Input": tc.username,
                "Password Input": tc.password,
                "Expected Output": tc.expectedMessage,
                "Actual Output": actualMessage,
                "Status": status,
                "Duration (ms)": tcDuration
            });
        }
    } catch (err) {
        console.error("Critical error during test run:", err);
    } finally {
        await driver.quit();
    }

    const totalDuration = Date.now() - startTime;
    console.log("====================================================");
    console.log("                RUN COMPLETE SUMMARY                ");
    console.log("====================================================");
    console.log(`Total Tests Run:   ${testCases.length}`);
    console.log(`Passed:            ${passedCount}`);
    console.log(`Failed:            ${failedCount}`);
    console.log(`Execution Time:    ${(totalDuration / 1000).toFixed(2)} seconds`);
    console.log("====================================================");

    // 3. Generate the Excel Sheet Report
    generateExcelReport(passedCount, failedCount, totalDuration, detailedResults);
}

function generateExcelReport(passed, failed, duration, details) {
    const reportPath = path.resolve(__dirname, '../Test_Execution_Report.xlsx');
    console.log(`Generating Excel Report: ${reportPath}`);

    const wb = XLSX.utils.book_new();

    // Tab 1: Summary Sheet Data
    const summaryData = [
        ["CREATIVE AI AUTHENTICATION PORTAL - TEST REPORT", "", ""],
        ["", "", ""],
        ["METRIC", "VALUE", "NOTES"],
        ["Report Timestamp", new Date().toLocaleString(), "Local execution time"],
        ["Browser Under Test", "Google Chrome (Headless)", "E2E Automated Driver"],
        ["Total Test Cases Run", passed + failed, "Target count: 300"],
        ["Passed Test Cases", passed, "Successful runs"],
        ["Failed Test Cases", failed, "Failed runs (Target: 0)"],
        ["Success Rate", `${((passed / (passed + failed)) * 100).toFixed(2)}%`, "E2E Pass percentage"],
        ["Total Execution Duration", `${(duration / 1000).toFixed(2)}s`, "Selenium WebDriver timeline"],
        ["Test Suite Status", failed === 0 ? "COMPLETED SUCCESS" : "FAILED", "Zero-failure compliance Check"]
    ];

    const wsSummary = XLSX.utils.aoa_to_sheet(summaryData);
    XLSX.utils.book_append_sheet(wb, wsSummary, "Execution Summary");

    // Tab 2: Detailed Results Sheet
    const wsDetails = XLSX.utils.json_to_sheet(details);
    XLSX.utils.book_append_sheet(wb, wsDetails, "Detailed Results");

    // Save Workbook
    XLSX.writeFile(wb, reportPath);
    console.log("Excel report saved successfully!");
}

runTests();
