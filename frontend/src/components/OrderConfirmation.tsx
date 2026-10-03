import React from 'react';
import { PlaceOrderResponse } from '../types/food';

interface OrderConfirmationProps {
  order: PlaceOrderResponse;
  onTrackOrder: () => void;
  onBackToMenu: () => void;
}

function formatLabel(raw: string | null | undefined): string {
  if (!raw) return '—';
  if (raw.toLowerCase().includes('cash')) return 'Cash';
  return raw
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

export const OrderConfirmation: React.FC<OrderConfirmationProps> = ({
  order,
  onTrackOrder,
  onBackToMenu,
}) => {
  // Calculate estimated subtotal from item data
  const itemsSubtotal = (order.items ?? []).reduce(
    (sum, item) => sum + item.subtotal,
    0
  );
  const discount = itemsSubtotal - order.finalPrice;

  return (
    <div className="confirmation-overlay" role="dialog" aria-modal="true" aria-label="Order Confirmation">
      <div className="confirmation-panel">
        {/* Success Banner */}
        <div className="confirmation-success-banner">
          <div className="confirmation-check">✓</div>
          <div>
            <h2 className="confirmation-heading">Order Placed!</h2>
            <p className="confirmation-subheading">
              Your order has been received and is being processed.
            </p>
          </div>
        </div>

        {/* Order ID + Status */}
        <div className="confirmation-id-row">
          <div className="confirmation-id-block">
            <span className="conf-label">Order</span>
            <span className="conf-order-id" id="confirmation-order-id">
              #{order.orderId}
            </span>
          </div>
          <span
            className={`order-status-badge status-${order.status.toLowerCase()}`}
            id="confirmation-status"
          >
            {formatLabel(order.status)}
          </span>
        </div>

        {/* Items */}
        <section className="confirmation-section">
          <h3 className="confirmation-section-title">Items Ordered</h3>
          <div className="confirmation-items-list" id="confirmation-items-list">
            {(order.items ?? []).map((item, idx) => (
              <div key={idx} className="confirmation-item-row">
                <div className="conf-item-info">
                  <span className="conf-item-name">{item.foodName}</span>
                  <span className="conf-item-category">{formatLabel(item.foodCategory)}</span>
                </div>
                <div className="conf-item-right">
                  <span className="conf-item-qty">× {item.quantity}</span>
                  <span className="conf-item-subtotal">₹{item.subtotal.toFixed(2)}</span>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Pricing Breakdown */}
        <section className="confirmation-section">
          <h3 className="confirmation-section-title">Pricing</h3>
          <div className="confirmation-pricing">
            <div className="conf-price-row">
              <span>Subtotal</span>
              <span id="confirmation-subtotal">₹{itemsSubtotal.toFixed(2)}</span>
            </div>
            {discount > 0.005 && (
              <div className="conf-price-row conf-discount-row">
                <span>Discount (Strategy)</span>
                <span id="confirmation-discount" className="conf-discount-val">
                  −₹{discount.toFixed(2)}
                </span>
              </div>
            )}
            <div className="conf-price-row conf-total-row">
              <span>Total</span>
              <span id="confirmation-total" className="conf-total-val">
                ₹{order.finalPrice.toFixed(2)}
              </span>
            </div>
          </div>
        </section>

        {/* Order Details */}
        <section className="confirmation-section">
          <h3 className="confirmation-section-title">Order Details</h3>
          <div className="confirmation-details-grid">
            <div className="conf-detail-item">
              <span className="conf-detail-label">Payment</span>
              <span className="conf-detail-val" id="confirmation-payment">
                {formatLabel(order.paymentOption)}
              </span>
            </div>
            <div className="conf-detail-item">
              <span className="conf-detail-label">Delivery</span>
              <span className="conf-detail-val" id="confirmation-delivery">
                {formatLabel(order.deliveryOption)}
              </span>
            </div>
            {order.transactionId && (
              <div className="conf-detail-item conf-detail-full">
                <span className="conf-detail-label">Transaction ID</span>
                <span className="conf-detail-val conf-txn-id" id="confirmation-transaction-id">
                  {order.transactionId}
                </span>
              </div>
            )}
            {order.address && (
              <div className="conf-detail-item conf-detail-full">
                <span className="conf-detail-label">Address</span>
                <span className="conf-detail-val" id="confirmation-address">
                  {order.address}
                </span>
              </div>
            )}
          </div>
        </section>

        {/* Backend message */}
        {order.message && (
          <p className="confirmation-backend-msg" id="confirmation-backend-message">
            {order.message}
          </p>
        )}

        {/* Actions */}
        <div className="confirmation-actions">
          <button
            type="button"
            className="btn-track-order"
            onClick={onTrackOrder}
            id="track-order-btn"
          >
            📍 Track Order
          </button>
          <button
            type="button"
            className="btn-back-to-menu"
            onClick={onBackToMenu}
            id="back-to-menu-btn"
          >
            ← Back to Menu
          </button>
        </div>
      </div>
    </div>
  );
};
