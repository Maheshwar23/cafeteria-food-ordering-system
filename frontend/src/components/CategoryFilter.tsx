import React from 'react';
import { Category } from '../types/food';

interface CategoryFilterProps {
  categories: Category[];
  selectedCategory: Category;
  onSelectCategory: (category: Category) => void;
  disabled?: boolean;
}

const CATEGORY_ICONS: Record<Category, string> = {
  All: '✨',
  Burger: '🍔',
  Pizza: '🍕',
  Beverage: '🥤',
  Dessert: '🍰',
};

export const CategoryFilter: React.FC<CategoryFilterProps> = ({
  categories,
  selectedCategory,
  onSelectCategory,
  disabled = false,
}) => {
  return (
    <nav className="category-filter" aria-label="Food categories" id="category-filter-nav">
      <div className="category-scroll">
        {categories.map((cat) => {
          const isSelected = selectedCategory === cat;
          return (
            <button
              key={cat}
              id={`category-btn-${cat.toLowerCase()}`}
              className={`category-pill ${isSelected ? 'active' : ''}`}
              onClick={() => onSelectCategory(cat)}
              disabled={disabled}
              type="button"
            >
              <span className="category-icon">{CATEGORY_ICONS[cat] || '🍽️'}</span>
              <span className="category-label">{cat}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
