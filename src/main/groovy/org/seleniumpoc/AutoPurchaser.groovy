package org.seleniumpoc

import groovyjarjarcommonscli.MissingArgumentException
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class AutoPurchaser {

  private static final REQUIRED_ARGS = ['URI', 'Username', 'Password']

  private static ChromeDriverService service

  private static WebDriver driver

  private static WebDriverWait wait

  private static def argsMap = [
    uri: '',
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

    initialize()



    attemptToPurchase()

    driver.quit()
    service.stop()
  }

  private static void initialize() {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader()
    def chromeDriverPath = classloader.getResource('chromedriver.exe').path

    ChromeOptions options = new ChromeOptions()

    // options.addArguments('--headless')

    service = new ChromeDriverService.Builder()
      .usingDriverExecutable(new File(chromeDriverPath))
      .usingAnyFreePort()
      .build()

    service.start()

    driver = new ChromeDriver(service, options)
    wait = new WebDriverWait(driver, 10)
  }

  private static void attemptToPurchase() {
    driver.get(argsMap.uri)
    buyWhenEnabled()
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

      List<WebElement> cartLoaded = driver.findElements(By.cssSelector(CssSelectors.CART_COUNT.get()))

      if (cartLoaded.size() > 0 && cartLoaded.get(0).text.toInteger() > 0) {

        List<WebElement> geekSquadPopup = driver.findElements(By.cssSelector(CssSelectors.GO_TO_CART_GS_POPUP.get()))

        if (geekSquadPopup.size() > 0 && geekSquadPopup.get(0).displayed) {
          geekSquadPopup.get(0).click()
        } else {
          driver.findElement(By.cssSelector(CssSelectors.GO_TO_CART.get())).click()
        }

        clickOnElement(CssSelectors.SHIPPING_RADIO_BUTTON.get())
        clickOnElement(CssSelectors.CHECKOUT_BUTTON.get())
        clickOnElement(CssSelectors.PAYMENT_BUTTON.get())
        clickOnElement(CssSelectors.PLACE_ORDER_BUTTON.get())

      } else {
        buyWhenEnabled()
      }
    } else {
      println("Outta stock!")
      Thread.sleep(15000)
      println('Refreshing...')
      driver.navigate().refresh()
      attemptToPurchase()
    }
  }

  private static void clickOnElement(String cssSelector) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)))

    WebElement element = driver.findElement(By.cssSelector(cssSelector))

    element.click()
  }
}
