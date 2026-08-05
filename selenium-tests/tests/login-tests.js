const { Builder, By, until } = require('selenium-webdriver');
const assert = require('assert');

describe('Nexus AI OS - E2E Web Login Flow Tests', function () {
  let driver;
  const baseUrl = 'http://localhost:5173';

  // Increase timeout for E2E browser setups
  this.timeout(15000);

  before(async function () {
    // Launch Chrome Headless/Normal
    driver = await new Builder().forBrowser('chrome').build();
  });

  after(async function () {
    if (driver) {
      await driver.quit();
    }
  });

  it('TC-001: Should load login page successfully and display branding', async function () {
    await driver.get(baseUrl);
    
    // Wait for the main glassmorphic panel to render
    const logoElement = await driver.wait(
      until.elementLocated(By.xpath("//h1[contains(text(), 'NEXUS AI OS')]")), 
      5000
    );
    const text = await logoElement.getText();
    assert.strictEqual(text, 'NEXUS AI OS');
  });

  it('TC-002: Should validate email field presence and typing', async function () {
    const emailInput = await driver.findElement(By.xpath("//input[@type='email']"));
    assert.ok(emailInput, 'Email input not found');
    
    await emailInput.clear();
    await emailInput.sendKeys('testuser@example.com');
    const value = await emailInput.getAttribute('value');
    assert.strictEqual(value, 'testuser@example.com');
  });

  it('TC-003: Should validate password field presence and typing', async function () {
    const passwordInput = await driver.findElement(By.xpath("//input[@type='password']"));
    assert.ok(passwordInput, 'Password input not found');
    
    await passwordInput.clear();
    await passwordInput.sendKeys('SecurePass123');
    const value = await passwordInput.getAttribute('value');
    assert.strictEqual(value, 'SecurePass123');
  });

  it('TC-004: Should toggle password text visibility on eye icon click', async function () {
    const passwordInput = await driver.findElement(By.xpath("//input[@placeholder='At least 6 characters']"));
    const toggleButton = await driver.findElement(By.xpath("//input[@placeholder='At least 6 characters']/following-sibling::button"));
    
    // Initially password type
    let typeAttribute = await passwordInput.getAttribute('type');
    assert.strictEqual(typeAttribute, 'password');

    // Click toggle
    await toggleButton.click();
    
    // Check type changed to text
    typeAttribute = await passwordInput.getAttribute('type');
    assert.strictEqual(typeAttribute, 'text');

    // Click toggle back
    await toggleButton.click();
    typeAttribute = await passwordInput.getAttribute('type');
    assert.strictEqual(typeAttribute, 'password');
  });

  it('TC-005: Should switch to Sign-up tab and render registration fields', async function () {
    const signupTabButton = await driver.findElement(By.xpath("//button[contains(text(), 'Sign up')]"));
    await signupTabButton.click();

    // Verify Name, UPI ID, Phone Number fields appear
    const nameInput = await driver.wait(
      until.elementLocated(By.xpath("//input[@placeholder='Karan']")), 
      3000
    );
    assert.ok(nameInput, 'Name input field not found on Sign-up tab');
    
    const upiInput = await driver.findElement(By.xpath("//input[@placeholder='name@upi']"));
    assert.ok(upiInput, 'UPI ID input not found');

    const phoneInput = await driver.findElement(By.xpath("//input[@placeholder='+91 98765 43210']"));
    assert.ok(phoneInput, 'Phone number input not found');
  });

  it('TC-006: Should display error validation on short passwords during registration', async function () {
    const emailInput = await driver.findElement(By.xpath("//input[@type='email']"));
    const passwordInput = await driver.findElement(By.xpath("//input[@placeholder='At least 6 characters']"));
    const submitButton = await driver.findElement(By.xpath("//button[@type='submit']"));

    await emailInput.clear();
    await emailInput.sendKeys('newuser@example.com');
    await passwordInput.clear();
    await passwordInput.sendKeys('123'); // Short password

    await submitButton.click();

    // Look for error banner
    const errorBanner = await driver.wait(
      until.elementLocated(By.xpath("//div[contains(., 'Password must be at least 6 characters')]")), 
      3000
    );
    assert.ok(errorBanner, 'Error validation banner for short password did not show');
  });

  it('TC-007: Should toggle back to Log-in tab successfully', async function () {
    const loginTabButton = await driver.findElement(By.xpath("//button[contains(text(), 'Log in')]"));
    await loginTabButton.click();

    // Verify Name field is removed from layout
    const nameFields = await driver.findElements(By.xpath("//input[@placeholder='Karan']"));
    assert.strictEqual(nameFields.length, 0, 'Name field should be hidden in login state');
  });

  it('TC-008: Should check presence of Google Sign-in button', async function () {
    const googleButton = await driver.findElement(By.xpath("//button[contains(text(), 'Google Sign-In')]"));
    assert.ok(googleButton, 'Google Sign-in button not found');
  });

  it('TC-009: Should handle invalid logins and show firebase credential errors', async function () {
    const emailInput = await driver.findElement(By.xpath("//input[@type='email']"));
    const passwordInput = await driver.findElement(By.xpath("//input[@placeholder='At least 6 characters']"));
    const submitButton = await driver.findElement(By.xpath("//button[@type='submit']"));

    await emailInput.clear();
    await emailInput.sendKeys('nonexistent@example.com');
    await passwordInput.clear();
    await passwordInput.sendKeys('invalidpassword');

    await submitButton.click();

    const errorBanner = await driver.wait(
      until.elementLocated(By.xpath("//div[contains(., 'Firebase')]")), 
      5000
    );
    assert.ok(errorBanner, 'Error banner for invalid credentials not displayed');
  });

  it('TC-010: Should render correctly on mobile viewports', async function () {
    await driver.manage().window().setSize({ width: 375, height: 812 }); // iPhone X dimensions
    
    const container = await driver.findElement(By.xpath("//div[contains(@style, 'max-width: 440px')]"));
    assert.ok(container, 'Main login card container is missing in mobile view');
    
    // Restore window size
    await driver.manage().window().setSize({ width: 1280, height: 800 });
  });
});
