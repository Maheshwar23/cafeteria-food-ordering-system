import {
  FoodItem,
  PlaceOrderRequest,
  PlaceOrderResponse,
  OrderResponse,
} from '../types/food';

const API_BASE_URL = 'http://localhost:8080/api';

// ─── Shared fetch helper ──────────────────────────────────────────────────────

async function apiFetch<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    // Attempt to read a JSON error body (Spring's standard error format)
    let message = `Request failed (${response.status}: ${response.statusText})`;
    try {
      const errBody = await response.json();
      if (errBody?.message) message = errBody.message;
      else if (errBody?.error) message = errBody.error;
      else if (typeof errBody === 'string') message = errBody;
    } catch {
      // ignore JSON parse failures — keep default message
    }
    throw new Error(message);
  }

  // 204 No Content / empty body
  const text = await response.text();
  if (!text) return undefined as unknown as T;
  return JSON.parse(text) as T;
}

// ─── Food items ───────────────────────────────────────────────────────────────

/**
 * Fetches food items from the backend API.
 * If category is provided and not 'All', queries /api/food-items?category={category}.
 */
export async function fetchFoodItems(category?: string): Promise<FoodItem[]> {
  const path =
    category && category !== 'All'
      ? `/food-items?category=${encodeURIComponent(category)}`
      : '/food-items';
  return apiFetch<FoodItem[]>(path);
}

// ─── Orders ───────────────────────────────────────────────────────────────────

/**
 * POST /api/orders
 * Sends the checkout form to the backend.
 * The backend is authoritative for pricing — client never sends prices.
 * Returns PlaceOrderResponse on success; throws an Error on failure.
 */
export async function placeOrder(
  request: PlaceOrderRequest
): Promise<PlaceOrderResponse> {
  return apiFetch<PlaceOrderResponse>('/orders', {
    method: 'POST',
    body: JSON.stringify(request),
  });
}

/**
 * GET /api/orders/{id}
 * Retrieves a persisted order for the tracking view.
 */
export async function getOrder(id: number): Promise<OrderResponse> {
  return apiFetch<OrderResponse>(`/orders/${id}`);
}

/**
 * PATCH /api/orders/{id}/status
 * Updates the order status and triggers backend Observer notifications.
 * Returns the updated status string.
 */
export async function updateOrderStatus(
  id: number,
  status: string
): Promise<{ id: number; status: string; message: string }> {
  return apiFetch(`/orders/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}
