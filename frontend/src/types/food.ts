export interface FoodItem {
  id: number;
  name: string;
  price: number;
  category: string;
}

export interface CartItem {
  foodItem: FoodItem;
  quantity: number;
}

export type Category = 'All' | 'Burger' | 'Pizza' | 'Beverage' | 'Dessert';
