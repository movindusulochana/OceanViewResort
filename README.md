# 🌊 Ocean View Resort - Distributed Management System

Ocean View Resort is a high-end, distributed management system designed for modern luxury hospitality operations. This project features a robust **JavaFX Admin Terminal** for desktop management and a **Professional Web Service (API)** for real-time monitoring and third-party integration.



## 🚀 Key Features

* **Centralized Admin Terminal:** A feature-rich JavaFX desktop application for staff to manage guests, rooms, and reservations.
* **Professional Web Portal:** A beautifully designed, animated dashboard (`/portal`) for real-time operations overview.
* **RESTful JSON APIs:** Endpoints for mobile app synchronization and external data access.
* **Automated Billing:** Built-in SQL procedures and Java services to calculate bills based on room rates and stay duration.
* **Real-time Notifications:** Integrated notification clients for system alerts.

## 🛠 Technology Stack

* **Language:** Java 17 (JDK 17)
* **Desktop UI:** JavaFX 17+
* **Backend Server:** Sun HTTP Server (Embedded)
* **Database:** MySQL 8.0 / MariaDB
* **Dependency Management:** Maven
* **JSON Processing:** Jackson Databind

## 📂 Project Structure

```text
OceanViewResort
├── database/               # SQL Schema & Initial Data
├── src/main/java/
│   ├── network/            # Web Service & API Logic (OceanViewServer)
│   └── resort/
│       ├── api/            # External API Clients (Exchange, Notifications)
│       ├── controller/     # JavaFX UI Controllers
│       ├── dao/            # Data Access Objects (JDBC logic)
│       ├── model/          # Pojo Classes (Guest, Room, Reservation)
│       ├── service/        # Business Logic (Billing, Reservations)
│       └── util/           # Database Connection & Helpers
└── src/main/resources/     # FXML views, CSS styles, and Assets
