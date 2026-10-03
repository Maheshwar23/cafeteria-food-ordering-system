import React from 'react';

interface HeaderProps {
  cartItemCount: number;
  onCartToggle?: () => void;
  isCartOpen?: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  cartItemCount,
  onCartToggle,
  isCartOpen = false,
}) => {
  return (
    <header className="site-header" id="site-header">
      <div className="header-container">
        <div className="brand-group">
          <div className="brand-icon">🍽️</div>
          <div>
            <h1 className="brand-title">Cafeteria Express</h1>
            <p className="brand-subtitle">Fresh & Delicious Campus Meals</p>
          </div>
        </div>

        <div className="header-actions">
          <span className="api-badge" title="Connected to Spring Boot backend">
            <span className="api-dot"></span>
            Backend API Active
          </span>

          <button
            className="mobile-cart-toggle"
            onClick={onCartToggle}
            aria-label="Toggle cart"
            aria-expanded={isCartOpen}
            id="mobile-cart-toggle-btn"
          >
            🛒 Cart ({cartItemCount})
          </button>
        </div>
      </div>
    </header>
  );
};
