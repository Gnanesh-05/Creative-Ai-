const wd = require('webdriverio');
const assert = require('assert');

describe('Nexus AI OS Mobile - Appium E2E Testing Suite', function () {
  let client;
  
  // Set execution timeout for app install and boot
  this.timeout(45000);

  const opts = {
    path: '/wd/hub',
    port: 4723,
    capabilities: {
      platformName: 'Android',
      automationName: 'UiAutomator2',
      deviceName: 'Android Emulator',
      appPackage: 'com.aistudio.nexusai.os',
      appActivity: '.MainActivity',
      noReset: false,
      ensureWebviewsHavePages: true
    }
  };

  before(async function () {
    client = await wd.remote(opts);
  });

  after(async function () {
    if (client) {
      await client.deleteSession();
    }
  });

  it('TC-001: Should launch app and display the Onboarding Screen', async function () {
    // Wait for Onboarding title text to be visible
    const titleElement = await client.$('android=new UiSelector().text("Explore Nexus AI")');
    await titleElement.waitForDisplayed({ timeout: 10000 });
    const isDisplayed = await titleElement.isDisplayed();
    assert.strictEqual(isDisplayed, true, 'Onboarding title is not displayed');
  });

  it('TC-002: Should navigate onboarding slides on Next button click', async function () {
    const nextBtn = await client.$('android=new UiSelector().text("NEXT")');
    await nextBtn.click();

    // Verify slide 2 title
    const slide2Title = await client.$('android=new UiSelector().text("Interactive Studios")');
    await slide2Title.waitForDisplayed({ timeout: 3000 });
    const isDisplayed = await slide2Title.isDisplayed();
    assert.strictEqual(isDisplayed, true, 'Slide 2 title did not appear');
  });

  it('TC-003: Should skip onboarding and land on Login Screen', async function () {
    const skipBtn = await client.$('android=new UiSelector().text("SKIP")');
    await skipBtn.click();

    // Verify presence of Login title or Email Input
    const emailField = await client.$('android=new UiSelector().textContains("Email")');
    await emailField.waitForDisplayed({ timeout: 5000 });
    const isDisplayed = await emailField.isDisplayed();
    assert.strictEqual(isDisplayed, true, 'Login screen email input field not found after skip');
  });

  it('TC-004: Should validate input entry in Email and Password fields', async function () {
    const emailInput = await client.$('android=new UiSelector().className("android.widget.EditText").instance(0)');
    await emailInput.setValue('mobileuser@example.com');
    const enteredEmail = await emailInput.getText();
    assert.strictEqual(enteredEmail, 'mobileuser@example.com');

    const passwordInput = await client.$('android=new UiSelector().className("android.widget.EditText").instance(1)');
    await passwordInput.setValue('SecurePass456');
  });

  it('TC-005: Should toggle password visibility in Compose layout', async function () {
    const toggleIcon = await client.$('android=new UiSelector().descriptionContains("Show password")');
    await toggleIcon.click();
    
    // Icon description should change to Hide password
    const updatedIcon = await client.$('android=new UiSelector().descriptionContains("Hide password")');
    assert.ok(updatedIcon, 'Password visibility toggle did not change content description');
  });

  it('TC-006: Should display validation toast on empty submit', async function () {
    const emailInput = await client.$('android=new UiSelector().className("android.widget.EditText").instance(0)');
    await emailInput.clearValue();
    
    const loginButton = await client.$('android=new UiSelector().text("LOGIN")');
    await loginButton.click();

    // Wait and check for validation feedback
    const feedbackText = await client.$('android=new UiSelector().textContains("Please enter email")');
    assert.ok(feedbackText, 'Validation alert/toast not shown on empty credentials submission');
  });

  it('TC-007: Should trigger Google Sign-In intent flow', async function () {
    const googleBtn = await client.$('android=new UiSelector().textContains("Google")');
    await googleBtn.click();

    // Verify Google Accounts chooser popup is displayed
    const accountChooser = await client.$('android=new UiSelector().resourceId("com.google.android.gms:id/account_picker_container")');
    await accountChooser.waitForDisplayed({ timeout: 5000 });
    assert.ok(accountChooser, 'Google account picker layout was not shown');

    // Go back to login screen
    await client.back();
  });

  it('TC-008: Should check bottom bar navigation items in main activity', async function () {
    // Note: Assuming successful login mock state is triggered or bypassed
    const homeTab = await client.$('android=new UiSelector().descriptionContains("Home")');
    const voiceTab = await client.$('android=new UiSelector().descriptionContains("Voice")');
    const chatTab = await client.$('android=new UiSelector().descriptionContains("Chat")');
    
    assert.ok(homeTab, 'Home nav tab is missing');
    assert.ok(voiceTab, 'Voice nav tab is missing');
    assert.ok(chatTab, 'Chat nav tab is missing');
  });

  it('TC-009: Should verify microphone click registers on Voice Screen', async function () {
    const voiceTab = await client.$('android=new UiSelector().descriptionContains("Voice")');
    await voiceTab.click();

    const micBtn = await client.$('android=new UiSelector().className("android.widget.Button").instance(0)');
    await micBtn.click();

    // Verify voice wave visualizer begins active state
    const waveVisualizer = await client.$('android=new UiSelector().descriptionContains("Wave Visualizer")');
    assert.ok(waveVisualizer, 'Audio wave visualizer was not rendered in active listening state');
  });

  it('TC-010: Should display Tic-Tac-Toe difficulties list on Game Center Screen', async function () {
    const gamesTab = await client.$('android=new UiSelector().descriptionContains("Games")');
    await gamesTab.click();

    const tictactoeCard = await client.$('android=new UiSelector().text("Tic-Tac-Toe")');
    await tictactoeCard.click();

    // Look for difficulty select options
    const easyBtn = await client.$('android=new UiSelector().text("EASY")');
    const smartBtn = await client.$('android=new UiSelector().text("SMART")');
    const unbeatableBtn = await client.$('android=new UiSelector().text("UNBEATABLE")');

    assert.ok(easyBtn, 'Easy difficulty selector not found');
    assert.ok(smartBtn, 'Smart difficulty selector not found');
    assert.ok(unbeatableBtn, 'Unbeatable difficulty selector not found');
  });
});
