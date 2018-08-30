package org.buyshit

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver

class bby {

  private static ChromeDriverService service

  private static WebDriver driver

  static void main(String[] args) {
    ClassLoader classloader = Thread.currentThread().getContextClassLoader()
    def chromeDriverPath = classloader.getResource('chromedriver.exe').path

    ChromeOptions options = new ChromeOptions()

    //options.addArguments('--headless')

    service = new ChromeDriverService.Builder()
      .usingDriverExecutable(new File(chromeDriverPath))
      .usingAnyFreePort()
      .build()

    service.start()

    /*driver = new RemoteWebDriver(service.getUrl(),
      DesiredCapabilities.chrome())*/

    driver = new ChromeDriver(service, options)

    driver.get('https://www.bestbuy.com/site/super-smash-bros-ultimate-special-edition-nintendo-switch/6255361.p?skuId=6255361')

    WebElement addToCartButton = driver.findElement(By.cssSelector('.add-to-cart-button button'))

    if (addToCartButton.isEnabled()) {
      println('buy the shit naow!')

      addToCartButton.click()

      driver.get('https://www.bestbuy.com/cart')

      WebElement checkOutButton = driver.findElement(By.cssSelector('.listing-header__button button'))

      checkOutButton.click()

      WebElement payment = driver.findElement(By.cssSelector('button--continue button'))

      payment.click()
    } else {
      println("shit's outta stock!")
    }

    driver.quit()
    service.stop()
  }
}
