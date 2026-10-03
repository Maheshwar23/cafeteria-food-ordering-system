import React, { useState, useEffect, useCallback } from 'react';
import { OrderResponse, ORDER_STATUSES, OrderStatus } from '../types/food';
import { getOrder, updateOrderStatus } from '../services/api';

interface OrderTrackingProps {
  orderId: number;
  onBackToMenu: () => void;
}

const STATUS_LABELS: Record<string, string> = {
  PLACED: 'Placed',
  PREPARING: 'Preparing',
  READY: 'Ready',
  OUT_FOR_DELIVERY: 'Out for Delivery',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

const STATUS_ICONS: Record<string, string> = {
  PLACED: '📋',
  PREPARING: '👨‍🍳',
  READY: '✅',
  OUT_FOR_DELIVERY: '🚗',
  DELIVERED: '🎉',
  CANCELLED: '❌',
};

function formatLabel(raw: string | null | undefined): string {
  if (!raw) return '—';
  if (raw.toLowerCase().includes('cash')) return 'Cash';
  return raw
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

export const OrderTracking: React.FC<OrderTrackingProps> = ({
  orderId,
  onBackToMenu,
}) => {
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [updateError, setUpdateError] = useState<string | null>(null);
  const [updateSuccess, setUpdateSuccess] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  const fetchOrder = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getOrder(orderId);
      setOrder(data);
      setSelectedStatus(data.status);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to load order.');
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => {
    fetchOrder();
  }, [fetchOrder]);

  const handleStatusUpdate = async () => {
    if (!selectedStatus || !order || selectedStatus === order.status) return;
    setUpdatingStatus(true);
    setUpdateError(null);
    setUpdateSuccess(null);
    try {
      await updateOrderStatus(orderId, selectedStatus);
      await fetchOrder(); // re-fetch fresh data
      setUpdateSuccess(`Status updated to "${STATUS_LABELS[selectedStatus] ?? selectedStatus}"`);
      setTimeout(() => setUpdateSuccess(null), 3000);
    } catch (err: unknown) {
      setUpdateError(err instanceof Error ? err.message : 'Failed to update status.');
    } finally {
      setUpdatingStatus(false);
    }
  };

  // Compute current step index for the progress track
  const currentStatusIndex = order
    ? ORDER_STATUSES.indexOf(order.status as OrderStatus)
    : -1;

  return (
    <div className="tracking-page">
      {/* Header */}
      <div className="tracking-top-bar">
        <button
          type="button"
          className="btn-tracking-back"
          onClick={onBackToMenu}
          id="tracking-back-btn"
        >
          ← Back to Menu
        </button>
        <h2 className="tracking-heading">
          Order Tracking — <span className="tracking-order-id">#{orderId}</span>
        </h2>
        <button
          type="button"
          className="btn-refresh"
          onClick={fetchOrder}
          disabled={loading}
          id="tracking-refresh-btn"
          title="Refresh order status"
        >
          {loading ? '↻' : '🔄'} Refresh
        </button>
      </div>

      {/* Loading */}
      {loading && (
        <div className="loading-state" id="tracking-loading">
          <div className="spinner" />
          <p>Loading order #{orderId}…</p>
        </div>
      )}

      {/* Error */}
      {!loading && error && (
        <div className="error-card" id="tracking-error">
          <div className="error-icon">⚠️</div>
          <div className="error-body">
            <h3 className="error-title">Could not load order</h3>
            <p className="error-text">{error}</p>
            <button className="btn-retry" onClick={fetchOrder} type="button" id="tracking-retry-btn">
              🔄 Retry
            </button>
          </div>
        </div>
      )}

      {!loading && !error && order && (
        <div className="tracking-content">
          {/* Status Progress Track */}
          <section className="tracking-section tracking-progress-section">
            <h3 className="tracking-section-title">Order Status</h3>

            {order.status === 'CANCELLED' ? (
              <div className="cancelled-banner" id="tracking-cancelled-banner">
                <span>❌</span>
                <span>This order has been cancelled.</span>
              </div>
            ) : (
              <div className="status-track" id="tracking-status-track">
                {ORDER_STATUSES.map((step, idx) => {
                  const isPast = idx < currentStatusIndex;
                  const isCurrent = idx === currentStatusIndex;
                  const isFuture = idx > currentStatusIndex;

                  return (
                    <React.Fragment key={step}>
                      <div
                        className={`status-step ${isPast ? 'past' : ''} ${isCurrent ? 'current' : ''} ${isFuture ? 'future' : ''}`}
                        id={`status-step-${step.toLowerCase()}`}
                      >
                        <div className="status-step-dot">
                          {isPast || isCurrent ? STATUS_ICONS[step] : '○'}
                        </div>
                        <div className="status-step-label">{STATUS_LABELS[step]}</div>
                      </div>
                      {idx < ORDER_STATUSES.length - 1 && (
                        <div className={`status-connector ${isPast ? 'filled' : ''}`} />
                      )}
                    </React.Fragment>
                  );
                })}
              </div>
            )}
          </section>

          {/* Order Items */}
          <section className="tracking-section">
            <h3 className="tracking-section-title">Items</h3>
            <div className="tracking-items-list" id="tracking-items-list">
              {order.items.map((item, idx) => (
                <div key={idx} className="tracking-item-row">
                  <div className="tracking-item-info">
                    <span className="tracking-item-name">{item.foodName}</span>
                    <span className="tracking-item-cat">{formatLabel(item.foodCategory)}</span>
                  </div>
                  <div className="tracking-item-right">
                    <span className="tracking-item-qty">× {item.quantity}</span>
                    <span className="tracking-item-subtotal">₹{item.subtotal.toFixed(2)}</span>
                  </div>
                </div>
              ))}
              <div className="tracking-item-row tracking-total-row">
                <span className="tracking-item-name">Total</span>
                <span className="tracking-total-val" id="tracking-total">
                  ₹{order.totalAmount.toFixed(2)}
                </span>
              </div>
            </div>
          </section>

          {/* Order Details */}
          <section className="tracking-section">
            <h3 className="tracking-section-title">Details</h3>
            <div className="tracking-details-grid">
              <div className="tracking-detail-item">
                <span className="tracking-detail-label">Delivery</span>
                <span className="tracking-detail-val" id="tracking-delivery">
                  {formatLabel(order.deliveryOption)}
                </span>
              </div>
              <div className="tracking-detail-item">
                <span className="tracking-detail-label">Payment</span>
                <span className="tracking-detail-val" id="tracking-payment">
                  {formatLabel(order.paymentOption)}
                </span>
              </div>
              {order.address && (
                <div className="tracking-detail-item tracking-detail-full">
                  <span className="tracking-detail-label">Address</span>
                  <span className="tracking-detail-val" id="tracking-address">
                    {order.address}
                  </span>
                </div>
              )}
              {order.specialInstructions && (
                <div className="tracking-detail-item tracking-detail-full">
                  <span className="tracking-detail-label">Special Instructions</span>
                  <span className="tracking-detail-val" id="tracking-special-instructions">
                    {order.specialInstructions}
                  </span>
                </div>
              )}
            </div>
          </section>

          {/* Demo Status Updater */}
          <section className="tracking-section admin-section" id="demo-status-updater">
            <h3 className="tracking-section-title">
              🎓 Demo — Update Status
              <span className="pattern-tag">Observer Pattern</span>
            </h3>
            <p className="admin-section-note">
              Updating the status triggers the backend's <strong>Observer</strong> pattern —
              KitchenNotificationObserver and CustomerNotificationObserver are both notified.
            </p>
            <div className="admin-controls">
              <select
                className="status-select"
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                disabled={updatingStatus}
                id="status-select"
                aria-label="Select new order status"
              >
                {[...ORDER_STATUSES, 'CANCELLED' as OrderStatus].map((s) => (
                  <option key={s} value={s}>
                    {STATUS_ICONS[s]} {STATUS_LABELS[s]}
                  </option>
                ))}
              </select>
              <button
                type="button"
                className="btn-update-status"
                onClick={handleStatusUpdate}
                disabled={updatingStatus || selectedStatus === order.status}
                id="update-status-btn"
              >
                {updatingStatus ? (
                  <span className="btn-loading">
                    <span className="spinner-sm" />
                    Updating…
                  </span>
                ) : (
                  'Update Status'
                )}
              </button>
            </div>

            {updateSuccess && (
              <div className="update-success-msg" id="update-success-msg" role="status">
                ✅ {updateSuccess}
              </div>
            )}
            {updateError && (
              <div className="update-error-msg" id="update-error-msg" role="alert">
                ⚠️ {updateError}
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  );
};
