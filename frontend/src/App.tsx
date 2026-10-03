import React, { useState, useEffect, useCallback } from 'react';
import { FoodItem, CartItem, Category } from './types/food';
import { fetchFoodItems } from './services/api';
import { Header } from './components/Header';
import { CategoryFilter } from './components/CategoryFilter';
import { FoodCard } from './components/FoodCard';
import { Cart } from './components/Cart';
import './index.css';

const CATEGORIES: Category[] = ['All', 'Burger', 'Pizza', 'Beverage', 'Dessert'];

export const App: React.FC = () => {
  const [foodItems, setFoodItems] = useState<FoodItem[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<Category>('All');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [isMobileCartOpen, setIsMobileCartOpen] = useState<boolean>(false);

  // Fetch food items whenever selectedCategory changes
  const loadFoodItems = useCallback(async (category: Category) => {
    setLoading(true);
    setError(null);
    try {
      const items = await fetchFoodItems(category);
      setFoodItems(items);
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Unable to reach the cafeteria backend server.';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadFoodItems(selectedCategory);
  }, [selectedCategory, loadFoodItems]);

  // Cart operations
  const handleAddToCart = (item: FoodItem) => {
    setCartItems((prev) => {
      const existing = prev.find((ci) => ci.foodItem.id === item.id);
      if (existing) {
        return prev.map((ci) =>
          ci.foodItem.id === item.id
            ? { ...ci, quantity: ci.quantity + 1 }
            : ci
        );
      }
      return [...prev, { foodItem: item, quantity: 1 }];
    });
  };

  const handleIncreaseQuantity = (foodItemId: number) => {
    setCartItems((prev) =>
      prev.map((ci) =>
        ci.foodItem.id === foodItemId
          ? { ...ci, quantity: ci.quantity + 1 }
          : ci
      )
    );
  };

  const handleDecreaseQuantity = (foodItemId: number) => {
    setCartItems((prev) =>
      prev
        .map((ci) =>
          ci.foodItem.id === foodItemId
            ? { ...ci, quantity: ci.quantity - 1 }
            : ci
        )
        .filter((ci) => ci.quantity > 0)
    );
  };

  const handleRemoveItem = (foodItemId: number) => {
    setCartItems((prev) => prev.filter((ci) => ci.foodItem.id !== foodItemId));
  };

  const handleClearCart = () => {
    setCartItems([]);
  };

  const totalCartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div className="app-shell">
      <Header
        cartItemCount={totalCartCount}
        onCartToggle={() => setIsMobileCartOpen((prev) => !prev)}
        isCartOpen={isMobileCartOpen}
      />

      <main className="main-content">
        <div className="layout-grid">
          {/* Menu Browsing Section */}
          <section className="menu-section" aria-label="Cafeteria Menu">
            <div className="menu-header">
              <div className="menu-title-group">
                <h2 className="menu-heading">Menu Catalog</h2>
                <p className="menu-subheading">
                  {selectedCategory === 'All'
                    ? 'Showing all items across categories'
                    : `Filtered by ${selectedCategory}`}
                </p>
              </div>

              <CategoryFilter
                categories={CATEGORIES}
                selectedCategory={selectedCategory}
                onSelectCategory={(cat) => setSelectedCategory(cat)}
                disabled={loading}
              />
            </div>

            {/* Loading State */}
            {loading && (
              <div className="loading-state" id="loading-spinner">
                <div className="spinner"></div>
                <p>Loading fresh food items from backend...</p>
              </div>
            )}

            {/* Error State */}
            {!loading && error && (
              <div className="error-card" id="error-message">
                <div className="error-icon">⚠️</div>
                <div className="error-body">
                  <h3 className="error-title">Backend Connection Issue</h3>
                  <p className="error-text">{error}</p>
                  <button
                    className="btn-retry"
                    onClick={() => loadFoodItems(selectedCategory)}
                    type="button"
                    id="retry-btn"
                  >
                    🔄 Retry Loading
                  </button>
                </div>
              </div>
            )}

            {/* Empty State */}
            {!loading && !error && foodItems.length === 0 && (
              <div className="empty-menu" id="empty-menu-state">
                <span className="empty-menu-icon">🍽️</span>
                <h3>No items found</h3>
                <p>No food items currently available in category "{selectedCategory}".</p>
              </div>
            )}

            {/* Food Grid */}
            {!loading && !error && foodItems.length > 0 && (
              <div className="food-grid" id="food-items-grid">
                {foodItems.map((item) => {
                  const inCart = cartItems.find((ci) => ci.foodItem.id === item.id);
                  return (
                    <FoodCard
                      key={item.id}
                      item={item}
                      cartQuantity={inCart ? inCart.quantity : 0}
                      onAddToCart={handleAddToCart}
                    />
                  );
                })}
              </div>
            )}
          </section>

          {/* Cart Sidebar */}
          <section className="cart-section">
            <Cart
              items={cartItems}
              onIncreaseQuantity={handleIncreaseQuantity}
              onDecreaseQuantity={handleDecreaseQuantity}
              onRemoveItem={handleRemoveItem}
              onClearCart={handleClearCart}
              isOpen={isMobileCartOpen}
              onClose={() => setIsMobileCartOpen(false)}
            />
          </section>
        </div>
      </main>

      <footer className="site-footer">
        <p>Cafeteria Food Ordering System &bull; Design Patterns Demonstration</p>
      </footer>
    </div>
  );
};

export default App;
