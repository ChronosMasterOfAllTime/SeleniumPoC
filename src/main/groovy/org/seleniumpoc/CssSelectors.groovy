package org.seleniumpoc

enum CssSelectors {

  USERNAME_FIELD('#fld-e'),
  PASSWORD_FIELD('#fld-p1'),
  LOGIN_SUBMIT('.js-submit-button'),
  ADD_TO_CART('.add-to-cart-button button'),
  CART_COUNT('.count-container .count'),
  GO_TO_CART('.cart-link'),
  GO_TO_CART_GS_POPUP('a.footer-link[href="/cart"]'),
  SHIPPING_RADIO_BUTTON('.availability--unselected input#availability-radio-1'),
  CHECKOUT_BUTTON('.listing-header__button button'),
  PAYMENT_BUTTON('.button--continue button.btn-secondary'),
  PLACE_ORDER_BUTTON('.button--place-order button.btn-primary'),
  ALERT_BANNER('.c-alert .c-alert-icon')

  private String cssSelector

  CssSelectors(String cssSelector) {
    this.cssSelector = cssSelector
  }

  def get() {
    cssSelector
  }
}
