package org.seleniumpoc

enum CssSelectors {

  ADD_TO_CART('.add-to-cart-button button'),
  CART_COUNT('.count-container .count'),
  GO_TO_CART('.cart-link'),
  GO_TO_CART_GS_POPUP('.go-to-cart.v-fw-medium'),
  SHIPPING_RADIO_BUTTON('input#availability-radio-1'),
  CHECKOUT_BUTTON('.listing-header__button button'),
  PAYMENT_BUTTON('.button--continue button.btn-secondary'),
  PLACE_ORDER_BUTTON('.button--place-order button.btn-primary')

  private String cssSelector

  CssSelectors(String cssSelector) {
    this.cssSelector = cssSelector
  }

  def get() {
    cssSelector
  }
}
