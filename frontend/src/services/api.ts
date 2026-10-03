import { FoodItem } from '../types/food';

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Fetches food items from the backend API.
 * If category is provided and not 'All', queries /api/food-items?category={category}.
 */
export async function fetchFoodItems(category?: string): Promise<FoodItem[]> {
  const url =
    category && category !== 'All'
      ? `${API_BASE_URL}/food-items?category=${encodeURIComponent(category)}`
      : `${API_BASE_URL}/food-items`;

  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to load food items (${response.status}: ${response.statusText})`);
  }

  return response.json();
}
