import React from 'react';
import { CartItem } from '../types/food';

interface CartProps {
  items: CartItem[];
  onIncreaseQuantity: (foodItemId: number) => void;
  onDecreaseQuantity: (foodItemId: number) => void;
  onRemoveItem: (foodItemId: number) => void;
  onClearCart: () => void;
  isOpen?: boolean;
  onClose?: () => void;
}

export const Cart: React.FC<CartProps> = ({
  items,
  onIncreaseQuantity,
  onDecreaseQuantity,
  onRemoveItem,
  onClearCart,
  isOpen = true,
  onClose,
}) => {
  const totalItemCount = items.reduce((sum, item) => sum + item.quantity, 0);
  const subtotal = items.reduce(
    (sum, item) => sum + item.foodItem.price * item.quantity,
    0
  );

  return (
    <aside
      className={`cart-sidebar ${isOpen ? 'open' : ''}`}
      id="cart-sidebar"
      aria-label="Shopping Cart"
    >
      <div className="cart-header">
        <div className="cart-title-wrap">
          <span className="cart-icon">🛒</span>
          <h2 className="cart-title">Your Order</h2>
          <span className="cart-badge">{totalItemCount}</span>
        </div>

        {onClose && (
          <button
            className="cart-close-btn"
            onClick={onClose}
            aria-label="Close cart"
            type="button"
          >
            ✕
          </button>
        )}
      </div>

      <div className="cart-content">
        {items.length === 0 ? (
          <div className="cart-empty" id="cart-empty-state">
            <div className="empty-icon">🍽️</div>
            <p className="empty-title">Your cart is empty</p>
            <p className="empty-subtitle">
              Add delicious food items from the menu to start your cafeteria order.
            </p>
          </div>
        ) : (
          <div className="cart-items-list" id="cart-items-list">
            {items.map(({ foodItem, quantity }) => {
              const itemTotal = foodItem.price * quantity;
              return (
                <div
                  key={foodItem.id}
                  className="cart-item-row"
                  id={`cart-item-${foodItem.id}`}
                >
                  <div className="cart-item-info">
                    <h4 className="cart-item-name">{foodItem.name}</h4>
                    <span className="cart-item-unit-price">
                      ₹{foodItem.price.toFixed(2)} each
                    </span>
                  </div>

                  <div className="cart-item-actions">
                    <div className="qty-control">
                      <button
                        className="qty-btn"
                        onClick={() => onDecreaseQuantity(foodItem.id)}
                        aria-label={`Decrease ${foodItem.name} quantity`}
                        id={`qty-decrease-${foodItem.id}`}
                        type="button"
                      >
                        −
                      </button>
                      <span
                        className="qty-value"
                        id={`qty-value-${foodItem.id}`}
                      >
                        {quantity}
                      </span>
                      <button
                        className="qty-btn"
                        onClick={() => onIncreaseQuantity(foodItem.id)}
                        aria-label={`Increase ${foodItem.name} quantity`}
                        id={`qty-increase-${foodItem.id}`}
                        type="button"
                      >
                        ＋
                      </button>
                    </div>

                    <div className="cart-item-subtotal">
                      ₹{itemTotal.toFixed(2)}
                    </div>

                    <button
                      className="btn-remove-item"
                      onClick={() => onRemoveItem(foodItem.id)}
                      aria-label={`Remove ${foodItem.name}`}
                      title="Remove item"
                      id={`cart-remove-${foodItem.id}`}
                      type="button"
                    >
                      🗑️
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {items.length > 0 && (
        <div className="cart-footer" id="cart-footer">
          <div className="cart-summary-line">
            <span className="summary-label">Items ({totalItemCount}):</span>
            <span className="summary-val">{items.length} unique</span>
          </div>
          <div className="cart-summary-line subtotal-line">
            <span className="summary-label">Subtotal:</span>
            <span className="summary-amount" id="cart-subtotal-amount">
              ₹{subtotal.toFixed(2)}
            </span>
          </div>

          <div className="cart-actions-row">
            <button
              className="btn-clear-cart"
              onClick={onClearCart}
              type="button"
              id="clear-cart-btn"
            >
              Clear Cart
            </button>
            <button
              className="btn-checkout disabled"
              disabled
              title="Order submission will be connected in Stage 10B"
              type="button"
              id="checkout-preview-btn"
            >
              Proceed to Order (Stage 10B)
            </button>
          </div>

          <p className="checkout-note">
            ℹ️ Checkout orchestration with design patterns unlocks in Stage 10B
          </p>
        </div>
      )}
    </aside>
  );
};
