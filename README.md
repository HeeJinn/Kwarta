# Kwarta - Personal Finance & Budgeting App

**Kwarta** is a modern, premium, offline-first personal finance and budgeting Android application. Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **Koin**, Kwarta offers a sleek, intuitive, and responsive experience for managing your income, tracking expenses, and planning monthly budgets.

---

## 🚀 Features

### 1. Interactive Dashboard
- **Dynamic Financial Overview**: See your current financial health at a glance.
- **Expressive Budget Ring**: Visual circular representation of your overall budget usage.
- **Recent Transactions**: Quick access to recent transactions.

### 2. Smart Budgeting & Analytics
- **Monthly Budget Limits**: Set custom budget limits for individual expense categories.
- **Monthly Spend Summary**: A comprehensive card showing total spent vs. total limit, and the remaining (or overspent) amount.
- **Segmented Spent Breakdown Bar**: A visual proportion bar displaying how much of your total spending is allocated to each category, color-coded by the database-configured category colors.
- **Interactive Legend**: Flowing list showing category names and their percentage contribution to your monthly spending.
- **Alert System**: Automatically highlights categories reaching critical spend thresholds (amber at 75%, red when exceeding 90% or overspent).
- **Edit/Delete Budgets**: Dynamically modify or remove budget goals for the current month.

### 3. Comprehensive Transaction Tracking
- **Unified Transaction List**: View, edit, or delete transactions easily.
- **Date & Category Filters**: Filter transactions by exact date ranges (Today, This Month, Custom Dates) or specific categories.
- **Rich Details**: Add custom titles, merchant names, notes, and attach receipt images (securely copied to app-specific internal storage).
- **Advanced Navigation & Scrolling**:
  - **Enter Always Top App Bar**: App bars dynamically hide/slide out of view during scroll for maximum content visibility.
  - **Auto-Hide Bottom Navigation**: The bottom navigation bar hides when scrolling down.
  - **FAB Menu**: Floating Action Button menu with micro-animations that expands to "Add Income" and "Add Expense", and automatically fades out when scrolled down.

### 4. Seeded & Custom Categories
- Pre-seeded default categories (Food, Rent, Salary, Transport, Entertainment) with unique color-hex identifiers.
- Supports adding custom categories with custom colors.

---

## 🛠️ Technology Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Design System**: Material 3
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Database / Local Storage**: [Room ORM](https://developer.android.com/training/data-storage/room) (SQLite)
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern

---

## 📂 Project Structure

```text
Kwarta/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/kwarta/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/         # Room Database Entities, DAOs & AppDatabase
│   │   │   │   │   └── repository/    # Finance Repository & implementation
│   │   │   │   ├── di/                # Koin Dependency Injection Modules
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/    # Reusable UI widgets (e.g., ExpressiveBudgetRing)
│   │   │   │   │   ├── navigation/    # Navigation destinations, host and scroll-offset state
│   │   │   │   │   ├── screens/       # Dashboard, Budgets, and Transactions views
│   │   │   │   │   └── theme/         # Material Theme definitions (Color, Typography, Shape)
│   │   │   │   ├── KwartaApplication.kt
│   │   │   │   └── MainActivity.kt
```

---

## 🛠️ Build & Run Instructions

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 34+)

### Compilation
To compile the Kotlin files and verify the codebase is error-free, run:
```powershell
.\gradlew compileDebugKotlin
```

### Running the App
To install and run the application on an active Android device or emulator:
```powershell
.\gradlew installDebug
```
