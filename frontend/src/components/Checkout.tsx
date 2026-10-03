import React, { useState } from 'react';
import { CartItem, CheckoutForm, PlaceOrderRequest } from '../types/food';

interface CheckoutProps {
  cartItems: CartItem[];
  onSubmit: (request: PlaceOrderRequest) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
  submitError: string | null;
}

const DEFAULT_FORM: CheckoutForm = {
  deliveryOption: 'DINE_IN',
  paymentOption: 'CARD',
  discountStrategy: 'NONE',
  address: '',
  specialInstructions: '',
};

function formatDiscount(strategy: string): string {
  if (strategy === 'NONE') return 'No Discount';
  if (strategy === 'PERCENTAGE:10') return '10% Discount';
  if (strategy === 'FIXED:50') return '₹50 Off';
  return strategy;
}

export const Checkout: React.FC<CheckoutProps> = ({
  cartItems,
  onSubmit,
  onCancel,
  isSubmitting,
  submitError,
}) => {
  const [form, setForm] = useState<CheckoutForm>(DEFAULT_FORM);
  const [validationError, setValidationError] = useState<string | null>(null);

  const subtotal = cartItems.reduce(
    (sum, ci) => sum + ci.foodItem.price * ci.quantity,
    0
  );

  const handleChange = <K extends keyof CheckoutForm>(
    key: K,
    value: CheckoutForm[K]
  ) => {
    setValidationError(null);
    setForm((prev) => ({
      ...prev,
      [key]: value,
      // clear address when switching away from delivery
      ...(key === 'deliveryOption' && value !== 'DELIVERY' ? { address: '' } : {}),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;

    if (cartItems.length === 0) {
      setValidationError('Your cart is empty. Please add items before placing an order.');
      return;
    }

    if (form.deliveryOption === 'DELIVERY' && !form.address.trim()) {
      setValidationError('Delivery address is required for Delivery orders.');
      return;
    }

    setValidationError(null);

    const request: PlaceOrderRequest = {
      items: cartItems.map((ci) => ({
        category: ci.foodItem.category,
        name: ci.foodItem.name,
        quantity: ci.quantity,
      })),
      deliveryOption: form.deliveryOption,
      paymentOption: form.paymentOption === 'CASH' ? 'Cash (MockPay)' : form.paymentOption,
      discountStrategy: form.discountStrategy,
      ...(form.deliveryOption === 'DELIVERY' && form.address.trim()
        ? { address: form.address.trim() }
        : {}),
      ...(form.specialInstructions.trim()
        ? { specialInstructions: form.specialInstructions.trim() }
        : {}),
    };

    await onSubmit(request);
  };

  const displayedError = validationError || submitError;

  return (
    <div className="checkout-overlay" role="dialog" aria-modal="true" aria-label="Checkout">
      <div className="checkout-panel">
        {/* Header */}
        <div className="checkout-panel-header">
          <div className="checkout-title-group">
            <span className="checkout-icon">📋</span>
            <h2 className="checkout-heading">Place Your Order</h2>
          </div>
          <button
            className="checkout-close-btn"
            onClick={onCancel}
            type="button"
            aria-label="Close checkout"
            disabled={isSubmitting}
          >
            ✕
          </button>
        </div>

        <div className="checkout-body">
          {/* Order Summary */}
          <section className="checkout-section">
            <h3 className="checkout-section-title">Order Summary</h3>
            <div className="checkout-items-list">
              {cartItems.map(({ foodItem, quantity }) => (
                <div key={foodItem.id} className="checkout-item-row">
                  <span className="checkout-item-name">
                    {quantity} × {foodItem.name}
                  </span>
                  <span className="checkout-item-price">
                    ₹{(foodItem.price * quantity).toFixed(2)}
                  </span>
                </div>
              ))}
              <div className="checkout-item-row checkout-subtotal-row">
                <span className="checkout-item-name">Est. Subtotal</span>
                <span className="checkout-item-price checkout-subtotal-val">
                  ₹{subtotal.toFixed(2)}
                </span>
              </div>
            </div>
            <p className="checkout-pricing-note">
              ℹ️ Final price is calculated by the backend (Strategy pattern).
            </p>
          </section>

          <form id="checkout-form" onSubmit={handleSubmit} noValidate>
            {/* Delivery Option */}
            <section className="checkout-section">
              <h3 className="checkout-section-title">Delivery Option</h3>
              <div className="checkout-option-group" role="group" aria-label="Delivery Option">
                {(['DINE_IN', 'TAKEAWAY', 'DELIVERY'] as const).map((opt) => (
                  <label
                    key={opt}
                    className={`checkout-option-card ${form.deliveryOption === opt ? 'selected' : ''}`}
                    id={`delivery-option-${opt.toLowerCase()}`}
                  >
                    <input
                      type="radio"
                      name="deliveryOption"
                      value={opt}
                      checked={form.deliveryOption === opt}
                      onChange={() => handleChange('deliveryOption', opt)}
                      className="sr-only"
                    />
                    <span className="option-icon">
                      {opt === 'DINE_IN' ? '🍽️' : opt === 'TAKEAWAY' ? '🥡' : '🚗'}
                    </span>
                    <span className="option-label">
                      {opt === 'DINE_IN' ? 'Dine In' : opt === 'TAKEAWAY' ? 'Takeaway' : 'Delivery'}
                    </span>
                  </label>
                ))}
              </div>

              {form.deliveryOption === 'DELIVERY' && (
                <div className="checkout-field" id="address-field">
                  <label htmlFor="delivery-address" className="checkout-label">
                    Delivery Address <span className="required-star">*</span>
                  </label>
                  <textarea
                    id="delivery-address"
                    className="checkout-textarea"
                    placeholder="Enter your delivery address..."
                    value={form.address}
                    onChange={(e) => handleChange('address', e.target.value)}
                    rows={2}
                    required
                  />
                </div>
              )}
            </section>

            {/* Payment Option */}
            <section className="checkout-section">
              <h3 className="checkout-section-title">
                Payment Option
                <span className="pattern-tag">Adapter Pattern</span>
              </h3>
              <div className="checkout-option-group" role="group" aria-label="Payment Option">
                {(['CARD', 'CASH', 'UPI', 'FAIL'] as const).map((opt) => (
                  <label
                    key={opt}
                    className={`checkout-option-card ${form.paymentOption === opt ? 'selected' : ''} ${opt === 'FAIL' ? 'danger-option' : ''}`}
                    id={`payment-option-${opt.toLowerCase()}`}
                  >
                    <input
                      type="radio"
                      name="paymentOption"
                      value={opt}
                      checked={form.paymentOption === opt}
                      onChange={() => handleChange('paymentOption', opt)}
                      className="sr-only"
                    />
                    <span className="option-icon">
                      {opt === 'CARD' ? '💳' : opt === 'CASH' ? '💵' : opt === 'UPI' ? '📱' : '❌'}
                    </span>
                    <span className="option-label">
                      {opt === 'CARD' ? 'Card' : opt === 'CASH' ? 'Cash' : opt === 'UPI' ? 'UPI' : 'Fail (Demo)'}
                    </span>
                  </label>
                ))}
              </div>
              {form.paymentOption === 'FAIL' && (
                <p className="checkout-danger-note">
                  ⚠️ This will simulate a payment failure to demonstrate Adapter error handling.
                </p>
              )}
            </section>

            {/* Discount Strategy */}
            <section className="checkout-section">
              <h3 className="checkout-section-title">
                Discount
                <span className="pattern-tag">Strategy Pattern</span>
              </h3>
              <div className="checkout-option-group" role="group" aria-label="Discount Strategy">
                {(['NONE', 'PERCENTAGE:10', 'FIXED:50'] as const).map((opt) => (
                  <label
                    key={opt}
                    className={`checkout-option-card ${form.discountStrategy === opt ? 'selected' : ''}`}
                    id={`discount-option-${opt.replace(':', '-').toLowerCase()}`}
                  >
                    <input
                      type="radio"
                      name="discountStrategy"
                      value={opt}
                      checked={form.discountStrategy === opt}
                      onChange={() => handleChange('discountStrategy', opt)}
                      className="sr-only"
                    />
                    <span className="option-icon">
                      {opt === 'NONE' ? '🏷️' : opt === 'PERCENTAGE:10' ? '📉' : '🎟️'}
                    </span>
                    <span className="option-label">{formatDiscount(opt)}</span>
                  </label>
                ))}
              </div>
            </section>

            {/* Special Instructions */}
            <section className="checkout-section">
              <h3 className="checkout-section-title">Special Instructions</h3>
              <div className="checkout-field">
                <textarea
                  id="special-instructions"
                  className="checkout-textarea"
                  placeholder="Any special requests? (optional)"
                  value={form.specialInstructions}
                  onChange={(e) => handleChange('specialInstructions', e.target.value)}
                  rows={2}
                />
              </div>
            </section>

            {/* Error Banner */}
            {displayedError && (
              <div className="checkout-error-banner" id="checkout-error-banner" role="alert">
                <span className="checkout-error-icon">⚠️</span>
                <div>
                  <p className="checkout-error-title">
                    {validationError ? 'Please check your order' : 'Order Failed'}
                  </p>
                  <p className="checkout-error-msg">{displayedError}</p>
                </div>
              </div>
            )}

            {/* Design Pattern Info */}
            <div className="pattern-info-block">
              <p className="pattern-info-title">🎓 Design Patterns in action on "Place Order":</p>
              <ul className="pattern-info-list">
                <li><strong>Factory Method</strong> — creates food domain objects (Burger, Pizza, …)</li>
                <li><strong>Builder</strong> — assembles the Order from your selections</li>
                <li><strong>Strategy</strong> — applies the chosen discount</li>
                <li><strong>Adapter</strong> — processes payment through MockPay / QuickPay</li>
                <li><strong>Facade</strong> — CafeteriaOrderFacade coordinates everything</li>
                <li><strong>Observer</strong> — Kitchen &amp; Customer get notified</li>
              </ul>
            </div>

            {/* Actions */}
            <div className="checkout-footer">
              <button
                type="button"
                className="btn-checkout-cancel"
                onClick={onCancel}
                disabled={isSubmitting}
                id="checkout-cancel-btn"
              >
                Back to Cart
              </button>
              <button
                type="submit"
                className="btn-place-order"
                disabled={isSubmitting}
                id="place-order-btn"
              >
                {isSubmitting ? (
                  <span className="btn-loading">
                    <span className="spinner-sm" />
                    Placing Order…
                  </span>
                ) : (
                  '✓ Place Order'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
