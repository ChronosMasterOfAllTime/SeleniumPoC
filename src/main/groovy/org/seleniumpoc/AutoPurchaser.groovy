package org.seleniumpoc

import groovyjarjarcommonscli.MissingArgumentException
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class AutoPurchaser {

  private static final REQUIRED_ARGS = ['URI', 'Login_URI', 'Username', 'Password']

  private static ChromeDriverService service

  private static WebDriver driver

  private static WebDriverWait wait

  private static Boolean initalCartCheck = true

  private static def argsMap = [
    uri: '',
    login_uri: '',
    username: '',
    password: '',
  ]

  /**
   * Initialize WebDriver and attempt to purchase desired item based on URI
   * @param args order of args is URI, Username, Password
   */
  static void main(String[] args) {

    // validate and set arguments the groovy way
    args.eachWithIndex{ arg, i ->
        if (arg && arg instanceof String) {
          argsMap."${REQUIRED_ARGS[i].toLowerCase()}" = arg
        } else {
          throw new MissingArgumentException("Required argument '${REQUIRED_ARGS[i]}' missing or invalid type")
        }
    }

    initialize(false)
    login()
    attemptToPurchase()

    driver.quit()
    service.stop()
  }

  private static void initialize(Boolean useChrome) {
    if (useChrome) {
      String chromeDriverPath = getDriver('chromedriver.exe')

      ChromeOptions options = new ChromeOptions()
      options.pageLoadStrategy = PageLoadStrategy.NONE
      options.headless = true

      service = new ChromeDriverService.Builder()
              .usingDriverExecutable(new File(chromeDriverPath))
              .usingAnyFreePort()
              .build()
      service.start()

      driver = new ChromeDriver(service, options)
    } else {
      String geckoDriverPath = getDriver('geckodriver.exe')

      System.setProperty("webdriver.gecko.driver", geckoDriverPath)

      FirefoxOptions options = new FirefoxOptions()
      options.binary = 'C:\\Program Files\\Mozilla Firefox\\firefox.exe'
      options.pageLoadStrategy = PageLoadStrategy.EAGER
      // options.headless = true
      driver = new FirefoxDriver(options)
    }

    wait = new WebDriverWait(driver, 10)

  }

  private static String getDriver(String driver) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader()
    def driverPath = classloader.getResource(driver).path
    driverPath
  }

  private static void login(Boolean populateUser = true) {
    if (populateUser) {
      driver.get(argsMap.login_uri)
      populateField(CssSelectors.USERNAME_FIELD.get(), argsMap.username)
      Thread.sleep(150)
    }
    populateField(CssSelectors.PASSWORD_FIELD.get(), argsMap.password)
    Thread.sleep(150)
    clickOnElement(CssSelectors.LOGIN_SUBMIT.get())
  }

  private static void populateField(String selector, String data) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)))
    driver.findElement(By.cssSelector(selector)).sendKeys(data)
  }

  private static void attemptToPurchase() {
    Thread.sleep(1000)
    driver.get(argsMap.uri)
    Thread.sleep(1000)

    if (isCartLoaded()) {
      goToCart()
      attemptCheckout()
    } else {
      initalCartCheck = false
      buyWhenEnabled()
    }
  }

  private static WebElement getCartButton() {

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CssSelectors.ADD_TO_CART.get())))

    WebElement addToCartButton = driver.findElement(By.cssSelector(CssSelectors.ADD_TO_CART.get()))
    addToCartButton
  }

  private static void buyWhenEnabled() {
    WebElement addToCartButton = getCartButton()

    while (!addToCartButton.isEnabled()) {
      println("Outta stock! Rechecking in 30s")
      Thread.sleep(30000)
      println('Refreshing...')
      driver.navigate().refresh()
      addToCartButton = getCartButton()
    }

    println('Buying naow!')

    while (!isCartLoaded()) {
      addToCartButton.click()
      Thread.sleep(1000)
    }

    goToCart()
    attemptCheckout()
  }

  private static Boolean isCartLoaded() {
    List<WebElement> cartLoaded = driver.findElements(By.cssSelector(CssSelectors.CART_COUNT.get()))

    Boolean isCartLoaded = cartLoaded.size() > 0 && cartLoaded.get(0).text.toInteger() > 0

    if (!isCartLoaded && !initalCartCheck) {
      println('Add to cart failed, reattempting in 30s...')
      Thread.sleep(30000)
    }

    isCartLoaded
  }

  private static void goToCart() {
    List<WebElement> geekSquadPopup = driver.findElements(By.cssSelector(CssSelectors.GO_TO_CART_GS_POPUP.get()))

    if (geekSquadPopup.size() > 0 && geekSquadPopup.get(0).displayed) {
      geekSquadPopup.get(0).click()
    } else {
      driver.findElement(By.cssSelector(CssSelectors.GO_TO_CART.get())).click()
    }

    Thread.sleep(2000)
    if (driver.findElements(By.cssSelector(CssSelectors.SHIPPING_RADIO_BUTTON.get())).size() > 0) {
      clickOnElement(CssSelectors.SHIPPING_RADIO_BUTTON.get())
    }
  }

  private static void attemptCheckout() {
    Boolean isAlertVisible = true

    while (isAlertVisible) {
      clickOnElement(CssSelectors.CHECKOUT_BUTTON.get())
      Thread.sleep(2500)

      isAlertVisible = !driver.findElements(By.cssSelector(CssSelectors.ALERT_BANNER.get())).empty

      if (isAlertVisible) {
        println('Checkout Failed, reattempting in 30s')
        Thread.sleep(30000)
        checkIfSessionEnded() ? login(false) : println('Still logged in! Phew!')
      }
    }

    println('Check it out naow!')
    Thread.sleep(3000)
    clickOnElement(CssSelectors.PAYMENT_BUTTON.get())
    Thread.sleep(7500)
    clickOnElement(CssSelectors.PLACE_ORDER_BUTTON.get())
    Thread.sleep(15000)
  }

  private static Boolean checkIfSessionEnded() {
    !driver.findElements(By.cssSelector(CssSelectors.USERNAME_FIELD.get())).empty
  }

  private static void clickOnElement(String cssSelector) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)))

    WebElement element = driver.findElement(By.cssSelector(cssSelector))

    element.click()
  }
}
