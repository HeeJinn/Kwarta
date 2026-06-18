# Kwarta - Personal Finance & Budgeting App

**Kwarta** is a modern, premium, offline-first personal finance and budgeting Android application. Built with **Kotlin**, **Jetpack Compose (Material 3 Expressive Design)**, **Room Database**, and **Koin**, Kwarta offers a sleek, intuitive, responsive, and gorgeous experience for managing your income, tracking expenses, and planning monthly budgets.

---

## 🚀 Features

### 1. Material 3 Expressive UI & Aesthetics
- **Vibrant Hero Balance Card**: A beautiful primary-to-secondary gradient hero card displaying your finances.
- **Expressive Balance Animation**: On opening the app, the total balance counts up smoothly from zero to the actual balance using a fast-out-slow-in animation.
- **Pill-Shaped Navigation Toolbar**: Utilizes a floating, pill-shaped `HorizontalFloatingToolbar` nested scrolling companion that automatically slides out of view when scrolling down and reappears on scroll up.
- **Spring-Animated FAB Menu**: A consolidated 3-action floating button menu (Add Income, Add Expense, Set Budget) that expands and collapses with smooth physical spring transitions and a dimming background.

### 2. Accessibility Shortcuts & Urgent Recording
- **Obsidian Home Screen Widget**: A premium, glassmorphic `3x2` widget featuring:
  - An inner translucent card isolating the total balance.
  - Large circular buttons (`56dp`) with clear flow arrow indicators.
  - Clear descriptive labels (**Income** / **Expense**) to make functions immediately recognizable.
  - Programmatic updates triggered directly upon database insertions to keep the widget live.
- **Launcher App Shortcuts**: Long-press the Kwarta launcher icon to instantly access "Add Expense" or "Add Income" shortcuts.
- **System Quick Settings Tile**: Add the custom "Quick Expense" tile to your device status drawer to launch straight into the expense record view with a single swipe.
- **Home Button Assist Gesture**: Register Kwarta as the system's default Digital Assistant App so that holding down the home button (or corner-swiping) launches the expense recorder instantly.
- **Intent Routing Engine**: A robust routing wrapper in `MainActivity` and Compose `Navigation` that parses intents from shortcuts, widget buttons, assist gestures, or settings tiles, navigating directly to the entry screen during both cold starts and warm task resumes.

### 3. Smart Budgeting & Category Customization
- **Monthly Budget Limits**: Set custom budget limits for individual expense categories.
- **Segmented Spent Breakdown Bar**: A visual proportion bar displaying how much of your total spending is allocated to each category, color-coded by the database-configured category colors.
- **Alert System**: Automatically highlights categories reaching critical spend thresholds (amber at 75%, red when exceeding 90% or overspent).
- **Category Customization Panel**: Accessible from the budget screen to create, edit, or archive categories. Includes:
  - Selection of transaction type (Expense or Income) and priority tags (Need, Want, Saving).
  - A beautiful color picker supporting 8 pre-selected premium color accents.
- **Category Archiving**: Safely hide/archive categories without destroying historical transaction relationships.

### 4. Comprehensive Transaction Tracking
- **Rich Details**: Add custom titles, merchant names, notes, and attach receipt images.
- **Direct Receipt Camera Capture**: When attaching a receipt, choose to take a photo directly using the device camera (using a secure local `FileProvider` and `TakePicture` contract) or pick an existing image from the gallery.
- **Image Persistence**: Receipt attachments are securely copied to internal app storage.

### 5. Settings, Personalization & Data Controls
- **Flexible Theme Engine**: Toggle between System Default, Light, and Dark modes.
- **Premium Color Palettes**: Customize the app appearance with 5 custom Material 3 color schemes (Purple, Blue, Green, Orange, Monochrome Black) or Android 12+ wallpaper-synced Dynamic Material You.
- **Dashboard Layout Preferences**: Toggle the Daily Safe-to-Spend Dial widget on or off. When disabled, the Primary Budget Limit card automatically spans the full screen width on both phones and tablets.
- **Notifications Switches**: Choose to schedule or cancel Daily Reminders and enable/disable critical budget alerts.
- **Data Reconciliation**: Reconcile actual balance discrepancies or reset the manual offset adjustment to revert strictly to computed values.
- **Secure Data Wipe**: Double-confirmation factory reset in the Danger Zone to erase all custom data safely and restore defaults.

---


## 🛠️ Technology Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Design System**: Material 3 Expressive API (`androidx.compose.material3:material3:1.5.0-alpha21`)
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
│   │   │   │   ├── tile/              # Quick Settings Tile Service (QuickExpenseTileService)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/    # Reusable UI widgets (e.g., ExpressiveBudgetRing)
│   │   │   │   │   ├── navigation/    # Navigation destinations, host and deep-link host routing
│   │   │   │   │   ├── screens/       # Dashboard, Budgets, and Transactions views
│   │   │   │   │   └── theme/         # Material Theme definitions (Color, Typography, Shape)
│   │   │   │   ├── widget/            # App Widget Provider and broadcast updater utility
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
.\gradlew compileDebugKotlin "-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr"
```

### Building the Debug APK
To compile, package, and generate a debug APK:
```powershell
.\gradlew assembleDebug "-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr"
```

### Running the App
To install and run the application on an active Android device or emulator:
```powershell
.\gradlew installDebug "-Dorg.gradle.java.home=C:\Program Files\Android\Android Studio\jbr"
```
