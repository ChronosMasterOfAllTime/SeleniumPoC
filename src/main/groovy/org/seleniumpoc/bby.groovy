package org.seleniumpoc

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

class bby {

  private static ChromeDriverService service

  private static WebDriver driver

  static void main(String[] args) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader()
    def chromeDriverPath = classloader.getResource('chromedriver.exe').path

    ChromeOptions options = new ChromeOptions()

    options.addArguments('--headless')

    service = new ChromeDriverService.Builder()
      .usingDriverExecutable(new File(chromeDriverPath))
      .usingAnyFreePort()
      .build()

    service.start()

    driver = new ChromeDriver(service, options)

    driver.get('https://www.bestbuy.com/site/super-smash-bros-ultimate-special-edition-nintendo-switch/6255361.p?skuId=6255361')

    attemptToPurchase(driver)

    driver.quit()
    service.stop()
  }

  private static void attemptToPurchase(ChromeDriver driver) {
    WebDriverWait wait = new WebDriverWait(driver, 10)
    WebElement addToCartButton = getCartButton(driver, wait)

    buyWhenEnabled(addToCartButton, driver, wait)
  }

  private static WebElement getCartButton(ChromeDriver driver, WebDriverWait wait) {

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector('.add-to-cart-button button')))

    WebElement addToCartButton = driver.findElement(By.cssSelector('.add-to-cart-button button'))
    addToCartButton
  }

  private static void buyWhenEnabled(WebElement addToCartButton, ChromeDriver driver, WebDriverWait wait) {
    if (addToCartButton.isEnabled()) {
      println('Buying naow!')

      addToCartButton.click()

      List<WebElement> cartLoaded = driver.findElements(By.cssSelector('.count-container .count'))

      if (cartLoaded.size() > 0 && cartLoaded.get(0).text.toInteger() > 0) {

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector('.go-to-cart.v-fw-medium')))

        WebElement geekSquadPopup = driver.findElement(By.cssSelector('.go-to-cart.v-fw-medium'))

        if (geekSquadPopup.displayed) {
          geekSquadPopup.click()
        } else {
          driver.findElement(By.cssSelector('.cart-link')).click()
        }

        clickOnElement(wait, driver, 'input#availability-radio-1')
        clickOnElement(wait, driver,'.listing-header__button button')
        clickOnElement(wait, driver, '.button--continue button.btn-secondary')
        clickOnElement(wait, driver, '.button--place-order button.btn-primary')

      } else {
        buyWhenEnabled(addToCartButton, driver, wait)
      }
    } else {
      println("Outta stock!")
      Thread.sleep(15000)
      println('Refreshing...')
      driver.navigate().refresh()
      attemptToPurchase(driver)
    }
  }

  private static void clickOnElement(WebDriverWait wait, ChromeDriver driver, String cssSelector) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(cssSelector)))

    WebElement shippingRadioButton = driver.findElement(By.cssSelector(cssSelector))

    shippingRadioButton.click()
  }
}
