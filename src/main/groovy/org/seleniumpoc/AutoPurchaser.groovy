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

  private static void login() {
    driver.get(argsMap.login_uri)
    populateField(CssSelectors.USERNAME_FIELD.get(), argsMap.username)
    Thread.sleep(100)
    populateField(CssSelectors.PASSWORD_FIELD.get(), argsMap.password)
    Thread.sleep(100)
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
    checkIfCartLoaded()
  }

  private static WebElement getCartButton() {

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CssSelectors.ADD_TO_CART.get())))

    WebElement addToCartButton = driver.findElement(By.cssSelector(CssSelectors.ADD_TO_CART.get()))
    addToCartButton
  }

  private static void buyWhenEnabled() {
    WebElement addToCartButton = getCartButton()
    if (addToCartButton.isEnabled()) {
      println('Buying naow!')

      addToCartButton.click()


    } else {
      println("Outta stock!")
      Thread.sleep(30000)
      println('Refreshing...')
      driver.navigate().refresh()
      attemptToPurchase()
    }
  }

  private static void checkIfCartLoaded() {
    // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CssSelectors.CART_COUNT.get())))
    List<WebElement> cartLoaded = driver.findElements(By.cssSelector(CssSelectors.CART_COUNT.get()))

    Thread.sleep(1000)
    if (cartLoaded.size() > 0 && cartLoaded.get(0).text.toInteger() > 0) {

      List<WebElement> geekSquadPopup = driver.findElements(By.cssSelector(CssSelectors.GO_TO_CART_GS_POPUP.get()))

      if (geekSquadPopup.size() > 0 && geekSquadPopup.get(0).displayed) {
        geekSquadPopup.get(0).click()
      } else {
        driver.findElement(By.cssSelector(CssSelectors.GO_TO_CART.get())).click()
      }

      // clickOnElement(CssSelectors.SHIPPING_RADIO_BUTTON.get())
      attemptCheckout()

    } else {
      buyWhenEnabled()
    }
  }

  private static void attemptCheckout() {
    clickOnElement(CssSelectors.CHECKOUT_BUTTON.get())
    Thread.sleep(1000)
    if (driver.findElements(By.cssSelector(CssSelectors.ALERT_BANNER.get())).empty) {
      println('Check it out naow!')
      Thread.sleep(1500)
      clickOnElement(CssSelectors.PAYMENT_BUTTON.get())
      Thread.sleep(1500)
      clickOnElement(CssSelectors.PLACE_ORDER_BUTTON.get())
    } else {
      println('Checkout Failed, reattempting in 30s')
      Thread.sleep(30000)
      attemptCheckout()
    }
  }

  private static void clickOnElement(String cssSelector) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)))

    WebElement element = driver.findElement(By.cssSelector(cssSelector))

    element.click()
  }
}
