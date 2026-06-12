# LovelyPOS 🛒

The reliable, offline-first Point of Sale companion for small businesses.

![Version](https://img.shields.io/badge/version-v1.1.3-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2026+-3DDC84?logo=android)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-4285F4?logo=jetpackcompose)
![License](https://img.shields.io/badge/License-MIT-yellow)

## About

**LovelyPOS** was born out of a real-world necessity. When my wife started her home-based food business, we found that most POS solutions were either too complex, required a constant internet connection, or were locked behind monthly subscriptions. 

This project is a unique collaboration between a programmer couple. With my wife’s background as a Frontend Web Developer and former Database Administrator, she architected the table structures and defined the data flow. I handled the Android implementation, focusing on a fluid user experience and robust offline architecture. We spent many late nights brainstorming the MVP to ensure it meets the practical demands of a fast-paced small kitchen.

Today, LovelyPOS is used daily in production by my wife. It is designed to be fully offline, ensuring that business never stops due to a poor connection, and keeping all sensitive sales data strictly on the device.

## Screenshots

<!-- Add screenshots here -->

## Tech Stack

| Category | Technology |
| --- | --- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Local Database** | Room (SQLite) |
| **Architecture** | Clean Architecture (MVVM + Use Cases) |
| **Design System** | Material 3 |
| **Concurrency** | Kotlin Coroutines & Flow |

## Architecture

LovelyPOS follows **Clean Architecture** principles to ensure the codebase is maintainable, testable, and scalable. The data flow follows a unidirectional pattern:

```text
UI (Compose) ↔ ViewModel (State) → UseCase (Domain) → Repository (Data) → DAO → Room (SQLite)
```

1.  **UI Layer**: Jetpack Compose functions that observe UI state from ViewModels.
2.  **ViewModel Layer**: Manages UI state and handles user interactions.
3.  **Domain Layer (UseCases)**: Contains the pure business logic of the application.
4.  **Data Layer (Repository/DAO)**: Orchestrates data from the Room database, serving as the single source of truth.

## Features

### 📋 Menu Management
- Full CRUD for menu items with stock tracking.
- Toggle item availability and "Out of Stock" (Habis) states.
- Smart sorting (A-Z, by stock) and Grid/List view toggles.

### 💰 POS / Cashier
- Intuitive category filter chips for fast navigation.
- Bottom Sheet Cart with real-time item count badges.
- Extended FAB for a quick view of total items and pricing.
- Multiple payment methods: Cash (with auto-change calculation), Transfer, and QRIS.

### 📊 History & Analytics
- Transaction history with detailed drill-down per receipt.
- Sales summaries filtered by Today, Week, Month, or Custom range.
- **Data Visualization**: Pure Canvas-built bar charts for best-selling items and daily revenue trends.
- Payment method breakdown for financial reconciliation.

### 🌓 Modern UI
- Native Dark Mode support.
- Fully responsive layouts for Portrait and Landscape orientations.

## Getting Started

### Requirements
- Android Studio (Iguana or newer)
- JDK 21
- Android Device/Emulator with API 26 (Android 8.0) or higher

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/semnazri/LovelyPOS.git
    ```
2.  Open the project in Android Studio.
3.  Wait for Gradle synchronization to complete.
4.  Run the `app` module on your device.

*Note: No backend setup is required. The app is fully functional offline from the first launch.*

## Roadmap
- [ ] **Atomic Design**: Refactoring the UI components for better reusability.
- [ ] **PDF Export**: Generate monthly sales reports in PDF format.
- [ ] **Bluetooth Printing**: Support for 58mm thermal receipt printers.
- [ ] **Sync Engine**: Optional cloud backup using Spring Boot/Kotlin backend.
- [ ] **Web Dashboard**: A browser-based view for remote business monitoring.

## Contributing
Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**. Please feel free to open an issue or submit a pull request.

## License
Distributed under the MIT License. See `LICENSE` for more information.

## Author

**Syamsul Bahri**
Senior Android Engineer | 11+ YOE
- **LinkedIn**: [linkedin.com](https://www.linkedin.com/in/syamsul-bahri-8a7b808a/)
- **Email**: dragon.coc88@gmail.com (Not Business Email, just for contact)
