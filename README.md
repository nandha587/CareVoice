# CareVoice 📞💊

> **Voice-First Medication Reminder and Adherence System for Elderly Loved Ones**  
> *Engineered so seniors never need a smartphone application or complex technology to stay healthy.*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4-teal.svg)](https://tailwindcss.com/)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/)
[![Database](https://img.shields.io/badge/PostgreSQL%20%7C%20H2-Supported-blue.svg)](https://www.postgresql.org/)

---

## 1. System Architecture Overview

CareVoice is built on a clean layered enterprise architecture:

```
[ Elderly User ]                      [ Caregiver Web Portal ]
 (Regular Phone)                      (React + TypeScript + Tailwind)
        |                                       |
        | Keypad DTMF (1 / 2)                   | HTTPS / REST (JWT Auth)
        v                                       v
[ Telephony Gateway (Twilio/Sim) ]    [ Reverse Proxy / API Gateway ]
        |                                       |
        +---------------+   +-------------------+
                        |   |
                        v   v
            +---------------------------------------+
            |      Spring Boot Application          |
            |                                       |
            |  [ Controller Layer ]                 |
            |   - AuthController                    |
            |   - PatientController                 |
            |   - MedicationController              |
            |   - DoseEventController               |
            |   - TelephonyWebhookController        |
            |   - PrescriptionOcrController         |
            |   - ReportController                  |
            |                                       |
            |  [ Service Layer ]                    |
            |   - AuthService (JWT & BCrypt)        |
            |   - PatientService                    |
            |   - MedicationService                 |
            |   - DoseEventService                  |
            |   - EscalationService                 |
            |   - TelephonyService (Twilio & Sim)   |
            |   - ReminderSchedulerService          |
            |   - NotificationService               |
            |   - PrescriptionOcrService            |
            |   - ReportService                     |
            |   - AuditLogService                   |
            |                                       |
            |  [ Security & Verification ]          |
            |   - JwtAuthFilter                     |
            |   - Human-in-the-Loop OCR Gate        |
            |                                       |
            |  [ Data Access (Spring Data JPA) ]    |
            |   - User, Patient, Medication,        |
            |     Schedule, DoseEvent, Notification |
            +---------------------------------------+
                        |                   |
                        v                   v
              [ PostgreSQL Database ]   [ Prescription Store ]
```

---

## 2. Technologies & Purpose

| Technology | Role | Purpose & Rationale |
| :--- | :--- | :--- |
| **Java 21 / Spring Boot 3.3** | Backend Engine | Strongly-typed, production-grade framework providing declarative transactions (`@Transactional`), robust scheduling (`@Scheduled`), and security. |
| **Spring Security & JJWT 0.12** | Authentication | Stateless JWT tokens, role-based caregiver access control (`ROLE_CAREGIVER`), and BCrypt (work factor 12) password hashing. |
| **Spring Data JPA & Hibernate** | ORM & Persistence | Object-relational mapping, transactional data integrity, and adherence aggregation queries. |
| **PostgreSQL & H2** | Relational Databases | Dual-profile architecture: H2 in-memory mode for zero-setup demo execution; PostgreSQL 16 for production durability. |
| **Spring Scheduler** | Scheduling Engine | Fixed-rate heartbeat (every 60s) checks due dose events; nightly lookahead seeder projects future doses. Modular design facilitates moving to Redis/BullMQ later. |
| **Twilio SDK & Webhooks** | Outbound Telephony | Generates TwiML voice responses, places cellular phone calls, and receives DTMF keypad tones (`1` / `2`). |
| **Interactive In-Browser Simulator**| Demo / Testing | Built-in telephone handset simulator with Web Speech audio synthesis in English, Spanish, and Hindi for immediate portfolio testing without a paid Twilio number. |
| **Prescription OCR Service** | Document Ingestion | Extracts candidate medication fields from images/text. Enforces strict untrusted draft status until caregiver human verification. |
| **React 18, TypeScript, Vite** | Frontend Portal | Single-page application with type safety, responsive design, and low latency. |
| **Tailwind CSS & Lucide Icons** | Styling & UI | Accessible, senior-care focused design with status badges and accessible typography. |

---

## 3. Database Schema

- **`users`**: Caregiver identity (UUID, email, password_hash, full_name, role, timestamps).
- **`patients`**: Elderly recipients (caregiver_id, full_name, phone_number, preferred_language, timezone, emergency_contact, notes).
- **`medications`**: Prescribed items (patient_id, name, dosage, confirmed_instruction, quantity_remaining, low_stock_threshold, is_active).
- **`medication_schedules`**: Reminder times (medication_id, reminder_time, frequency, days_of_week, timezone).
- **`dose_events`**: Individual dose reminders (medication_schedule_id, patient_id, scheduled_at, status, call_attempts, last_called_at, responded_at, response_source, notes).
  - *Statuses*: `SCHEDULED`, `CALLING`, `TAKEN`, `MISSED`, `NO_RESPONSE`, `CANCELLED`.
- **`notifications`**: Escalation logs (patient_id, caregiver_id, dose_event_id, type, channel, status, message, sent_at).
- **`prescription_drafts`**: Untrusted OCR drafts (caregiver_id, patient_id, original_filename, raw_ocr_text, parsed_fields, status).
- **`audit_logs`**: Tamper-evident operational audit trail (user_id, action, entity_type, entity_id, metadata, timestamp).

---

## 4. Key Workflows & Safety Principles

### 1. Voice Call & Adherence Workflow
1. The scheduler identifies a due dose event (`SCHEDULED` and `scheduledAt <= now()`).
2. CareVoice initiates an automated outbound call to the senior's phone.
3. The phone rings, connects, and speaks in the patient's preferred language (`en-US`, `es-ES`, `hi-IN`):
   > *"Hello Eleanor. This is your CareVoice medication reminder. It is time for your scheduled medicine: Lisinopril, 10mg. Take after breakfast with water. Press 1 after taking it. Press 2 if you have not taken it."*
4. **If Senior presses `1` (Taken)**:
   - Status updated to `TAKEN`.
   - Medication quantity remaining is decremented by 1.
   - If stock drops to/below threshold, caregiver receives `LOW_STOCK_WARNING`.
5. **If Senior presses `2` (Not Taken)**:
   - Status updated to `MISSED`.
   - Caregiver receives immediate `MISSED_DOSE` alert.
6. **If Senior does not answer / Call dropped**:
   - Retry scheduled after configured delay (e.g., 10 minutes).
   - If retries exhausted (default: 2 attempts), status marked `NO_RESPONSE` and caregiver is escalated with urgent `NO_RESPONSE` alert.
   - **Safety Rule**: CareVoice NEVER automatically instructs a patient to take an extra dose.

### 2. Prescription OCR Human-in-the-Loop Verification Gate
```
Prescription Image → OCR Extraction → Caregiver Verification → Active Medication
```
- OCR output is strictly classified as **Untrusted Draft** (`PENDING_VERIFICATION`).
- Reminders are **never** scheduled automatically from raw OCR.
- The caregiver must review and click `Confirm Medication & Activate Reminders` to confirm medicine name, dosage, instructions, quantity, and reminder time.

### 3. Non-Diagnostic Reporting Rule
Weekly summaries report objective factual adherence metrics:
> *"12 of 14 scheduled medication reminders were confirmed as taken this week (85.7% adherence)."*
The system never infers medical improvement or alters clinical dosages.

---

## 5. Third-Party Credentials Reference

CareVoice runs out of the box with **zero required external purchases** using the built-in simulator profile (`dev`). When deploying to production with live telephony, configure the following:

| Service | Account Required | Credential Name | Storage Location | Why It Is Needed |
| :--- | :--- | :--- | :--- | :--- |
| **Twilio** | Twilio Cloud Account ([twilio.com](https://www.twilio.com)) | `TWILIO_ACCOUNT_SID`<br>`TWILIO_AUTH_TOKEN`<br>`TWILIO_FROM_NUMBER` | `.env` or Environment Variables | To place actual cellular phone calls to landlines/mobile phones and receive DTMF keypad tones. |
| **Email SMTP** | Gmail App Password, SendGrid, or Mailgun | `SPRING_MAIL_HOST`<br>`SPRING_MAIL_USERNAME`<br>`SPRING_MAIL_PASSWORD` | `.env` or Environment Variables | To deliver email notifications when an elderly patient misses a dose or stock runs low. |
| **PostgreSQL** | Local PostgreSQL or Docker container | `SPRING_DATASOURCE_URL`<br>`DB_USER`<br>`DB_PASSWORD` | `.env` or Docker Compose | ACID persistent storage for patients, medications, and dosage logs. |

---

## 6. Getting Started & Running the Project

### Prerequisites
- **Java 21+** (`java -version`, `javac -version`)
- **Maven 3.9+** (`mvn -version`)
- **Node.js 18+** & **npm** (`node -v`)

### Step 1: Start the Backend (Spring Boot)
Open a terminal in `backend/`:
```bash
cd backend
mvn spring-boot:run
```
*Note: The backend starts on `http://localhost:8080` with an H2 in-memory database and pre-seeded demo data.*

#### Default Demo Credentials:
- **Email**: `caregiver@carevoice.com`
- **Password**: `CareVoice2026!`
- **Pre-enrolled Patients**:
  - `Eleanor Vance` (+15551234567, English, America/New_York)
  - `Ramesh Sharma` (+919876543210, Hindi, Asia/Kolkata)

### Step 2: Start the Frontend (React + Vite)
Open a second terminal in `frontend/`:
```bash
cd frontend
npm run dev
```
Open your browser at `http://localhost:5173`.

---

## 7. Interactive Senior Telephone Simulator

For portfolio demonstration and testing without a paid Twilio number:
1. Navigate to **Live Call Simulator** (`/simulator`) or click **Test Call** on any reminder in the dashboard.
2. Click **Place Automated Call**.
3. Hear the automated voice prompt synthesized live through your browser in the patient's language (`en-US`, `es-ES`, `hi-IN`).
4. Click Keypad **1 (Dose Taken)** or **2 (Not Taken)**:
   - Watch the dose status update immediately on the dashboard.
   - Watch the medication pill count decrement by 1 on adherence.
   - Watch the caregiver alert trigger if marked missed!

---

## 8. Automated Test Suite

CareVoice includes end-to-end integration tests verifying:
- Authentication & JWT issuance
- Patient and medication creation
- Keypad 1 adherence & inventory decrement
- Keypad 2 escalation & alert dispatch
- Prescription OCR draft safety gate

To execute the test suite:
```bash
cd backend
mvn test
```
All tests pass cleanly (`BUILD SUCCESS`).
