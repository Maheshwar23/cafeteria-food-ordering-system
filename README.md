# Cafeteria Food Ordering System

A full-stack, enterprise-grade college demonstration project implementing six classic Gang of Four (GoF) design patterns in a realistic food ordering workflow. Built with **Spring Boot 3**, **MySQL**, **Spring Data JPA**, and **React + TypeScript (Vite)**.

---

## 1. Project Overview

The **Cafeteria Food Ordering System** streamlines food ordering for a campus cafeteria. Students and staff can browse menu items across multiple food categories, manage an active cart, and proceed to checkout by specifying delivery options, payment options, and discount codes.

Behind the intuitive web interface, the application orchestrates six core object-oriented design patterns across the ordering lifecycle—from polymorphic domain object creation and step-by-step order construction to dynamic discount calculation, third-party payment integration, facade-based workflow coordination, and real-time status change notifications.

---

## 2. Key Features

* **Food Browsing & Category Filtering:** Interactive menu display with category tabs (`All`, `Burger`, `Pizza`, `Beverage`, `Dessert`) fetching persisted items from the backend database.
* **Interactive Shopping Cart:** Real-time quantity adjustments (`+` / `−`), item removal, live subtotal updates, and clear-cart actions.
* **Flexible Checkout:**
  * **Delivery Options:** `DINE_IN`, `TAKEAWAY`, and `DELIVERY`.
  * **Dynamic Delivery Address:** Conditionally required and visible only when `DELIVERY` is selected; hidden for `DINE_IN` and `TAKEAWAY`.
  * **Special Instructions:** Optional kitchen preparation notes.
* **Payment Methods & Simulation:**
  * `CARD` and `CASH` (routed through MockPay adapter).
  * `UPI` (routed through QuickPay adapter).
  * `FAIL` (demo payment gateway failure for testing resilience and error banners).
* **Discount Strategies:** Selectable discount options (`No Discount`, `10% Discount`, `₹50 Discount`) evaluated server-side.
* **Authoritative Server Pricing:** Client sends item identities and quantities only; the backend looks up authoritative prices from MySQL to prevent price tampering.
* **Order Confirmation Receipt:** Immediate post-order screen displaying generated Order ID, line-item breakdown, discount deductions, final total, delivery/payment details, and payment transaction tokens.
* **Visual Order Status Tracking:** Progress track visualizing stages (`PLACED` → `PREPARING` → `READY` → `OUT_FOR_DELIVERY` → `DELIVERED`, and `CANCELLED`).
* **Live Status Updater (Observer Demo):** In-app control allowing evaluators to transition order status, triggering backend observer notifications.
* **Comprehensive Error Handling:** Validations for empty carts, missing delivery addresses, payment gateway rejections, and backend connection drops.

---

## 3. Design Patterns

All six design patterns are genuinely implemented in the backend domain layer and executed during order processing:

### 1. Factory Method (Creational)
* **Implementation:**
  * Abstract Creator: `com.cafeteria.factory.FoodFactory`
  * Concrete Creators: `BurgerFactory`, `PizzaFactory`, `BeverageFactory`, `DessertFactory`
  * Product Hierarchy: `FoodItem` (abstract base), `Burger`, `Pizza`, `Beverage`, `Dessert`
* **Problem Solved:** Decouples order ingestion from specific food subclass instantiation. The system does not hardcode `new Burger(...)` in business services.
* **How It Is Used:** When client requests items by category, `OrderService.resolveFactory(category)` selects the corresponding `FoodFactory` subclass, which instantiates the concrete `FoodItem` domain instance.

### 2. Builder (Creational)
* **Implementation:**
  * Builder Class: `com.cafeteria.model.Order.Builder`
  * Target Product: `com.cafeteria.model.Order`
* **Problem Solved:** Solves telescoping constructors and enforces immutability when assembling complex objects with multiple optional parameters (delivery address, special instructions, payment options, varying line items).
* **How It Is Used:** `OrderService` constructs domain orders incrementally:
  ```java
  Order domainOrder = new Order.Builder()
          .addItem(domainFoodItem, itemReq.getQuantity())
          .setAddress(request.getAddress())
          .setSpecialInstructions(request.getSpecialInstructions())
          .setDeliveryOption(request.getDeliveryOption())
          .setPaymentOption(request.getPaymentOption())
          .build();
  ```

### 3. Strategy (Behavioral)
* **Implementation:**
  * Strategy Interface: `com.cafeteria.strategy.PricingStrategy`
  * Concrete Strategies: `NoDiscountStrategy`, `PercentageDiscountStrategy`, `FixedAmountDiscountStrategy`
  * Context Class: `com.cafeteria.strategy.PricingContext`
* **Problem Solved:** Eliminates brittle conditional branches (`if/else` ladders) for discount calculations and allows new promotion rules to be added without modifying existing pricing code (Open/Closed Principle).
* **How It Is Used:** The client submits a discount token (`NONE`, `PERCENTAGE:10`, `FIXED:50`). `OrderService.resolveStrategy()` instantiates the corresponding `PricingStrategy`, passes it to `PricingContext`, and calculates the authoritative final total.

### 4. Observer (Behavioral)
* **Implementation:**
  * Observer Interface: `com.cafeteria.observer.OrderStatusObserver`
  * Subject: `com.cafeteria.model.Order` (maintains `List<OrderStatusObserver>`)
  * Concrete Observers: `CustomerNotificationObserver`, `KitchenNotificationObserver`
* **Problem Solved:** Decouples order state management from external notification systems. When an order transitions through its lifecycle, interested parties must be informed without tightly coupling their logic to the `Order` class.
* **How It Is Used:** During order placement and on every `PATCH /api/orders/{id}/status` transition, `CustomerNotificationObserver` and `KitchenNotificationObserver` are attached to the domain order. Calling `domainOrder.setStatus(newStatus)` automatically notifies all observers.

### 5. Adapter (Structural)
* **Implementation:**
  * Target Interface: `com.cafeteria.adapter.PaymentProcessor`
  * Adapters: `MockPayAdapter`, `QuickPayAdapter`
  * Adaptees / External APIs: `com.cafeteria.adapter.external.MockPayService`, `QuickPayService`
  * Result Wrapper: `com.cafeteria.adapter.PaymentResult`
* **Problem Solved:** Integrates incompatible third-party payment vendor interfaces into a unified cafeteria checkout contract without altering external APIs.
* **How It Is Used:**
  * `MockPayService.makePayment(amount, currency)` is adapted via `MockPayAdapter.processPayment(amount)`.
  * `QuickPayService.sendPayment(paymentData)` is adapted via `QuickPayAdapter.processPayment(amount)`.
  * The checkout facade interacts solely with the unified `PaymentProcessor` interface.

### 6. Facade (Structural)
* **Implementation:**
  * Facade Class: `com.cafeteria.facade.CafeteriaOrderFacade`
  * Return DTO: `com.cafeteria.facade.OrderPlacementResult`
* **Problem Solved:** Shields clients and service layers from the complexity of coordinating pricing calculation, payment execution, observer registration, and domain validation.
* **How It Is Used:** `OrderService` delegates the multi-step ordering workflow to `facade.placeOrder(domainOrder, customerObserver, kitchenObserver)`, which executes all steps in unified sequence and returns an `OrderPlacementResult`.

---

## 4. Technology Stack

* **Backend:**
  * Java 21 / 23 Target
  * Spring Boot 3.3.5
  * Spring Data JPA / Hibernate
  * Maven (`mvnw`)
  * Jakarta Bean Validation
* **Database:**
  * MySQL Community Server 8.x / 26.x
  * Dialect: `org.hibernate.dialect.MySQLDialect`
  * Embedded H2 Database (for isolated unit/repository test execution)
* **Frontend:**
  * React 19
  * TypeScript 5.7
  * Vite 6
  * Modern Vanilla CSS (CSS variables, glassmorphism, responsive grid/flexbox)

---

## 5. Architecture

```
[ React + TypeScript Frontend (Port 5173) ]
                   │
                   ▼  HTTP REST API (JSON)
[ Spring Boot Controller Layer (Port 8080) ]
   ├── FoodItemController
   ├── OrderController
   └── HealthController
                   │
                   ▼
[ Service & Design Patterns Domain Layer ]
   ├── OrderService / FoodItemService
   ├── CafeteriaOrderFacade          <── [Facade Pattern]
   ├── FoodFactory & Concrete Types  <── [Factory Method Pattern]
   ├── Order.Builder                 <── [Builder Pattern]
   ├── PricingContext & Strategies   <── [Strategy Pattern]
   ├── PaymentProcessor Adapters     <── [Adapter Pattern]
   └── OrderStatusObserver Observers <── [Observer Pattern]
                   │
                   ▼
[ Spring Data JPA Repositories ]
   ├── FoodItemRepository
   └── OrderRepository
                   │
                   ▼  JDBC (Port 3306)
[ MySQL Database: `cafeteria` ]
   ├── food_items
   ├── orders
   └── order_items
```

---

## 6. Project Structure

```
cafeteria-food-ordering-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cafeteria/
│   │   │   │   ├── adapter/        # Adapter pattern (MockPay, QuickPay)
│   │   │   │   ├── config/         # CORS and DataInitializer seed configuration
│   │   │   │   ├── controller/     # REST Controllers & DTOs
│   │   │   │   ├── entity/         # JPA Entities (FoodItem, Order, OrderItem)
│   │   │   │   ├── facade/         # Facade pattern (CafeteriaOrderFacade)
│   │   │   │   ├── factory/        # Factory Method pattern (FoodFactory hierarchy)
│   │   │   │   ├── model/          # Domain models & Builder (Order.Builder)
│   │   │   │   ├── observer/       # Observer pattern (OrderStatusObserver)
│   │   │   │   ├── repository/     # Spring Data JPA Repositories
│   │   │   │   ├── service/        # Service orchestration (OrderService)
│   │   │   │   └── strategy/       # Strategy pattern (PricingStrategy hierarchy)
│   │   │   └── resources/
│   │   │       └── application.properties # Environment-driven configuration
│   │   └── test/                   # 60 automated unit, pattern, and JPA tests
│   ├── mvnw.cmd                    # Maven wrapper script (Windows)
│   └── pom.xml                     # Maven project definition
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Cart.tsx            # Slide-out cart with item controls
│   │   │   ├── CategoryFilter.tsx  # Food category filter pills
│   │   │   ├── Checkout.tsx        # Checkout modal with pattern options
│   │   │   ├── FoodCard.tsx        # Menu item card with Add to Cart
│   │   │   ├── Header.tsx          # App header with backend status badge
│   │   │   ├── OrderConfirmation.tsx # Post-order receipt view
│   │   │   └── OrderTracking.tsx   # Visual status progression & demo updater
│   │   ├── services/
│   │   │   └── api.ts              # Centralized backend HTTP client
│   │   ├── types/
│   │   │   └── food.ts             # TypeScript interfaces mirroring backend DTOs
│   │   ├── App.tsx                 # Main application view state machine
│   │   ├── index.css               # Design system and component styling
│   │   └── main.tsx                # React application entry point
│   ├── package.json                # Frontend dependencies and scripts
│   └── vite.config.ts              # Vite configuration
│
└── README.md                       # Complete project documentation
```

---

## 7. Database Setup

The backend utilizes MySQL with automatic table creation/updating (`spring.jpa.hibernate.ddl-auto=update`) and seeds default menu items on startup through `DataInitializer`.

### 1. Create the Database
Log into MySQL using the command line or MySQL Workbench:
```sql
CREATE DATABASE IF NOT EXISTS cafeteria;
```

### 2. Configure Credentials Securely
The application avoids hardcoding database passwords. In `application.properties`, credentials are read from environment variables:
```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:cafeteria}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```
Set your password in PowerShell before starting the backend (see section below). **Never commit plain-text passwords into version control.**

---

## 8. Running the Project

Open two separate Windows PowerShell terminals:

### Terminal 1: Start the Backend (Port 8080)
```powershell
# Navigate to the backend directory
cd c:\Users\nikil\Documents\cafeteria-food-ordering-system\backend

# Set your MySQL password in the environment for this session
$env:DB_PASSWORD = "your_actual_mysql_password"

# Optional: set DB_USERNAME if different from 'root'
# $env:DB_USERNAME = "root"

# Run the Spring Boot application
.\mvnw.cmd spring-boot:run
```
*Backend will be accessible at: `http://localhost:8080`*
*Health check endpoint: `http://localhost:8080/api/health`*

### Terminal 2: Start the Frontend (Port 5173)
```powershell
# Navigate to the frontend directory
cd c:\Users\nikil\Documents\cafeteria-food-ordering-system\frontend

# Install dependencies (if not already installed)
npm.cmd install

# Start Vite development server
npm.cmd run dev
```
*Frontend will be accessible at: `http://localhost:5173`*

---

## 9. REST API Endpoints

| Method | Endpoint | Description | Request Body | Response Body |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Service health verification | *None* | Plaintext: `"Cafeteria backend is running"` |
| `GET` | `/api/food-items` | Retrieve all cafeteria food items | *None* | Array of `FoodItemResponse` (`id`, `name`, `price`, `category`) |
| `GET` | `/api/food-items?category={category}` | Filter food items by category | *None* | Filtered array of `FoodItemResponse` |
| `POST` | `/api/orders` | Place a new order using design patterns | `PlaceOrderRequest` | `PlaceOrderResponse` (HTTP 201 Created) |
| `GET` | `/api/orders/{id}` | Retrieve order details & line items | *None* | `OrderResponse` (HTTP 200 OK) |
| `PATCH` | `/api/orders/{id}/status` | Update status & trigger Observers | `StatusUpdateRequest` | Map (`id`, `status`, `message`) |

### Example Payloads

#### `POST /api/orders` Request:
```json
{
  "items": [
    { "category": "Burger", "name": "Classic Burger", "quantity": 2 },
    { "category": "Beverage", "name": "Cold Drink", "quantity": 1 }
  ],
  "deliveryOption": "DELIVERY",
  "paymentOption": "CARD",
  "discountStrategy": "PERCENTAGE:10",
  "address": "Room 402, Block A",
  "specialInstructions": "No onions"
}
```

#### `POST /api/orders` Response:
```json
{
  "orderId": 1,
  "success": true,
  "message": "Order placed successfully",
  "finalPrice": 252.0,
  "status": "PLACED",
  "transactionId": "MPAY-INR-9D915798",
  "address": "Room 402, Block A",
  "deliveryOption": "DELIVERY",
  "paymentOption": "CARD",
  "items": [
    {
      "foodName": "Classic Burger",
      "foodCategory": "Burger",
      "unitPrice": 120.0,
      "quantity": 2,
      "subtotal": 240.0
    },
    {
      "foodName": "Cold Drink",
      "foodCategory": "Beverage",
      "unitPrice": 40.0,
      "quantity": 1,
      "subtotal": 40.0
    }
  ]
}
```

#### `PATCH /api/orders/1/status` Request:
```json
{
  "status": "PREPARING"
}
```

---

## 10. Testing

### Backend Automated Test Suite
The backend contains 60 unit, integration, pattern, and repository tests covering all pattern implementations, DTO mappings, and database queries.
```powershell
cd backend
.\mvnw.cmd test
```
**Current Result:**
```text
[INFO] Results:
[INFO] Tests run: 60, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Frontend Production Build
The frontend TypeScript types and Vite bundle compile cleanly without warnings:
```powershell
cd frontend
npm.cmd run build
```
**Current Result:**
```text
✓ 36 modules transformed.
dist/index.html                   0.48 kB
dist/assets/index-BhcjzCCG.css   27.75 kB
dist/assets/index-C_gNqTdd.js   172.98 kB
✓ built in ~760ms
```

---

## 11. Demo Flow

Follow this sequence for an end-to-end project presentation:

1. **Verify Backend Status:** Open `http://localhost:5173`. Point out the green **"Backend Connected"** indicator in the header.
2. **Browse & Filter Food Items:** Click category filters (`Burger`, `Pizza`, `Beverage`, `Dessert`, `All`) to showcase dynamic catalog loading from MySQL.
3. **Cart Operations:** Click **"Add to Cart"** on multiple items. Increment/decrement quantities and demonstrate automatic subtotal calculations.
4. **Initiate Checkout:** Click **"Proceed to Order →"** to launch the checkout modal.
5. **Demonstrate Client Validation:** Select `Delivery`, leave the address blank, and click `Place Order`. Show the validation warning requiring an address.
6. **Demonstrate Payment Failure (Adapter Error Handling):** Select payment option `Fail (Demo)`, enter an address, and click `Place Order`. Show the HTTP 422 gateway rejection error banner.
7. **Place Successful Order:**
   * Select `Takeaway` or `Dine In`.
   * Select payment `Card` or `UPI`.
   * Select `10% Discount` (Strategy pattern).
   * Click **"Place Order"**.
8. **Inspect Confirmation Receipt:** Verify Order ID, calculated discount deduction, authoritative final price, and external transaction ID (`MPAY-...` / `QP-...`).
9. **Order Tracking View:** Click **"Track Order"**. Observe the visual progression bar highlighting `Placed`.
10. **Demonstrate Observer Pattern:** In the **Demo Status Updater**, select `Preparing` and click **"Update Status"**. Observe the live progress tracker advance to `Preparing` and verify state refresh against the database.
11. **Return to Menu:** Click **"← Back to Menu"** to reset the view and start a new order with an empty cart.

---

## 12. Design Pattern Demo Notes

Quick reference table for presentation and code review:

| Design Pattern | Key Classes in Codebase | Role in System |
| :--- | :--- | :--- |
| **Factory Method** | `FoodFactory`, `BurgerFactory`, `PizzaFactory`, `BeverageFactory`, `DessertFactory` | Instantiates category-specific `FoodItem` domain entities dynamically based on request data. |
| **Builder** | `Order.Builder`, `Order` | Step-by-step construction of complex immutable `Order` objects containing variable items, delivery metadata, and payment options. |
| **Strategy** | `PricingStrategy`, `NoDiscountStrategy`, `PercentageDiscountStrategy`, `FixedAmountDiscountStrategy`, `PricingContext` | Evaluates and applies dynamic promotional discounts to determine authoritative order totals. |
| **Observer** | `OrderStatusObserver`, `CustomerNotificationObserver`, `KitchenNotificationObserver` | Automatically notifies external stakeholders (kitchen displays, customer notifications) whenever an order status changes. |
| **Adapter** | `PaymentProcessor`, `MockPayAdapter`, `QuickPayAdapter`, `MockPayService`, `QuickPayService` | Translates disparate payment gateway APIs into a uniform `processPayment(double amount)` interface. |
| **Facade** | `CafeteriaOrderFacade`, `OrderPlacementResult` | Coordinates pricing calculation, payment processing, domain order assembly, and observer notifications behind a single unified API. |
