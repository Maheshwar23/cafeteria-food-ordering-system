package com.cafeteria.model;

/**
 * Enumeration representing the possible lifecycle states of an Order.
 */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
