# PhishNet — Cybersecurity Incident Reporting System

A Java + MySQL desktop application for reporting, tracking, and analyzing cybersecurity incidents such as phishing attacks, phone scams, ransomware, and identity theft. Developed as a database application project for **CCINFOM S24** at De La Salle University – Manila (Term 1, A.Y. 2025–2026).

**Group 2:** Hallare, Zach Benedict I. · Campo, Benette Enzo V. · Martin, Kurt Nehemiah Z.

---

## Purpose

Traditional tools like spreadsheets cannot handle the complexity of linking victims, perpetrators, evidence, and attack types across multiple incidents. PhishNet provides a structured platform to:

- Let victims submit and track cybersecurity incident reports.
- Allow administrators to validate reports, review evidence, and manage perpetrators.
- Automatically escalate threat levels when a perpetrator targets multiple victims.
- Flag high-risk victim accounts that have been repeatedly targeted.
- Generate analytics reports to identify attack patterns and trends.
- Comply with the Philippine Data Privacy Act (RA 10173) through data anonymization and role-based access controls.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21.0.1 (FXML + SceneBuilder) |
| Database | MySQL (`CybersecurityDB`, port 3306) |
| Password Hashing | Argon2id via Password4j 1.7.3 |
| Build Tool | Maven |

---

## Features

### Victim Portal
- **Register / Login** — Secure account creation with Argon2id-hashed passwords.
- **Report an Incident** — Submit a new cybersecurity incident, selecting the attack type and entering perpetrator details. The system deduplicates perpetrators by identifier (phone number, email, IP address, URL, or social media account) and auto-escalates their threat level to *Malicious* if they accumulate 3+ unique victims within 7 days.
- **Upload Evidence** — Attach screenshots, email exports, chat logs, or files to an existing report. Files are copied to local storage and tracked in the database with a verification status.
- **View My Reports** — See all submitted reports, their current status, and the verification status of attached evidence.

### Admin Dashboard
- **Review Pending Reports** — Validate or reject incoming incident reports. Rejected items are moved to the recycle bin for recovery.
- **Review Pending Evidence** — Verify or reject uploaded evidence with inline image previews. Supports restoring rejected items from the recycle bin.
- **Evaluate Reports** — Inspect and update perpetrator details, manually adjust threat levels, and add investigation notes to individual reports. All threat level changes are logged in an audit trail.
- **Create Reports** — Manually submit incident reports on behalf of victims.
- **Generate Analytics Reports** — Access four report types (described below).

### Analytics Reports
All reports support month/year filtering and CSV export.

| Report | Description |
|---|---|
| **Monthly Attack Trends** | Incidents grouped by attack type and time-of-day for a selected month, including evidence submission counts per incident. |
| **Top Perpetrators** | Ranks the top 10 perpetrators by incident count for a selected month, with associated attack types. |
| **Victim Activity** | Lists victims with more than 3 incidents in a given month to surface frequently targeted individuals. |
| **Incident Evidence Summary** | All evidence submissions for a period with type, submission date, verification status, and reviewing admin. |

---

## Core Data Model

```
Victims ──< IncidentReports >── Perpetrators
                │                      │
                └──< Evidence          └── ThreatLevelLog
                │
VictimStatusLog ──< Victims
AttackTypes ──< IncidentReports
Administrators ──< IncidentReports, Evidence, ThreatLevelLog
RecycleBinReports / RecycleBinEvidence  (soft-delete archive)
```

**Key status enumerations:**

- Incident status: `Pending` · `Validated` · `Rejected` · `Under Investigation` · `Resolved`
- Evidence status: `Pending` · `Verified` · `Rejected`
- Perpetrator threat level: `UnderReview` · `Suspected` · `Malicious` · `Cleared`
- Victim account status: `Active` · `Flagged` · `Under Investigation` · `Resolved`

---

## Project Structure

```
src/
├── main/java/
│   ├── Main.java                      # Application entry point
│   ├── controller/                    # JavaFX UI controllers
│   │   └── report/                    # Analytics report controllers
│   ├── model/                         # POJO entities (Victim, IncidentReport, etc.)
│   ├── dao/                           # JDBC Data Access Objects
│   ├── service/                       # Authentication and business logic
│   └── util/                          # DB connection, security, validation, date utils
└── resources/SceneBuilder/
    ├── fxml/                          # FXML layout files
    │   ├── auth/                      # Login, SignUp, AdminLogin
    │   ├── victim/                    # Victim dashboard and report screens
    │   ├── admin/                     # Admin dashboard and management screens
    │   └── shared/                    # Analytics report views
    └── assets/                        # Application icons and images
```

---

## Setup

### Prerequisites
- Java 17+
- Maven
- MySQL Server with a database named `CybersecurityDB`

### Configuration

Database credentials are configured directly in `src/main/java/util/DatabaseConnection.java`. The default connection string is:

```
jdbc:mysql://localhost:3306/CybersecurityDB
```

Update the `URL`, `USER`, and `PASSWORD` constants in that file to match your local MySQL setup.

### Build and Run

```bash
mvn clean javafx:run
```

Or build a fat JAR:

```bash
mvn clean package
java -jar target/dbapp-3.0.0.jar
```

---

## Automatic Business Rules

| Rule | Trigger | Action |
|---|---|---|
| Perpetrator escalation | 3+ unique victims reported in the last 7 days | Threat level → `Malicious`; entry added to `ThreatLevelLog` |
| Victim account flagging | Victim submits more than 5 reports in the current month | Account status → `Flagged`; entry added to `VictimStatusLog` |
| Perpetrator deduplication | New incident report submitted | Existing perpetrator record reused if identifier already exists |

---

## Data Privacy

The system is designed to comply with the **Philippine Data Privacy Act of 2012 (RA 10173)**:

- Passwords are hashed with Argon2id (3 iterations, 64 MiB memory, 4 threads).
- `SecurityUtils` includes anonymization helpers for victim names and emails used in reports.
- Role-based access controls separate victim and administrator capabilities.
- Constant-time password comparison prevents timing-based attacks.
