# FinanceApp — Personal Finance Manager

An Android personal finance app inspired by AndroMoney, built with modern Kotlin and Jetpack libraries.

## Features

### Core (AndroMoney-inspired)
- **Transaction Management** — Add, edit, delete Income / Expense / Transfer transactions
- **Accounts** — Cash, Bank, Credit Card, Savings, Investment accounts with auto-balance tracking
- **Categories** — 19 built-in income/expense categories; fully customisable with icons and colours
- **Dashboard** — Monthly income / expense / net-balance summary cards + recent transactions
- **Reports** — Pie chart (spending by category) and bar chart (monthly trends) drawn with Compose Canvas
- **Budget** — Set monthly budgets per category with live progress bars and over-budget warnings
- **Search & Filter** — Filter transactions by keyword, date range, category, account, and type
- **Recurring Transactions** — Daily / Weekly / Monthly / Yearly repeat schedules
- **Multi-currency** — Each account has its own currency (20 currencies supported)
- **Data Export** — Export all transactions to CSV in `Downloads/FinanceApp/`

### Additional
- **Fee Field** — Every transaction has an optional *Bank Fee / Service Charge* field
- **Points Field** — Every transaction has an optional *Loyalty Points Earned / Redeemed* field

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| UI | Jetpack Compose + Material 3 |
| Navigation | Jetpack Navigation Compose |
| DI | Hilt |
| State | StateFlow / ViewModel |
| Charts | Compose Canvas (no third-party library) |
| Preferences | DataStore |
| Build | Gradle Kotlin DSL |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |

## Project Structure

```
FinanceApp/
├── app/src/main/java/com/financeapp/
│   ├── data/
│   │   ├── local/          # Room DB, 4 DAOs, 4 Entities, FinanceDatabase
│   │   └── repository/     # Repository implementations
│   ├── domain/
│   │   ├── model/          # Domain models + Enums
│   │   ├── repository/     # Repository interfaces
│   │   └── usecase/        # 15 use cases
│   ├── presentation/
│   │   ├── home/           # HomeScreen + HomeViewModel
│   │   ├── transaction/    # List + AddEdit screens & ViewModels
│   │   ├── account/        # AccountsScreen + AccountViewModel
│   │   ├── category/       # CategoriesScreen + CategoryViewModel
│   │   ├── budget/         # BudgetScreen + BudgetViewModel
│   │   ├── report/         # ReportsScreen + ReportViewModel
│   │   ├── settings/       # SettingsScreen + SettingsViewModel
│   │   ├── components/     # Shared composables, Charts, Theme
│   │   └── MainScreen.kt   # BottomNav host
│   ├── di/                 # Hilt DatabaseModule + RepositoryModule
│   ├── FinanceApplication.kt
│   └── MainActivity.kt
└── gradle/libs.versions.toml
```

## How to Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps

```bash
# Clone / navigate to project
cd FinanceApp

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test
```

The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

## First Launch

On the very first launch the app seeds:
- **19 default categories** (Food, Transport, Shopping, Salary, etc.)
- **3 sample accounts** (Wallet, Main Bank, Credit Card)
- **5 sample transactions** so the home screen is populated immediately

## Screens

| Screen | Description |
|---|---|
| Home | Monthly summary + recent transactions |
| Transactions | Filterable/searchable transaction list |
| Add/Edit Transaction | Form with Amount, **Fee**, **Points**, Account, Category, Date, Note, Recurring |
| Reports | Pie chart by category + bar chart by month |
| Budget | Category budgets with progress bars |
| Accounts | Account cards with balances; add/edit/delete |
| Categories | Category grid; add/edit/delete |
| Settings | Default currency, theme, CSV export |

## Data Export

Go to **Settings → Export to CSV**. The file is saved to:
```
/sdcard/Android/data/com.financeapp/files/transactions_<timestamp>.csv
```

The CSV includes all transaction fields including Fee and Points columns.
