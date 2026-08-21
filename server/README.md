# NT04 Network Intelligence Central Server & PostgreSQL Synchronization Guide

This directory contains the production-grade **FastAPI + PostgreSQL** central synchronization backend for the NT04 Android Network Monitor & Packet Analyzer app.

---

## 🚀 Quick Start in 60 Seconds

### 1. Prerequisites
- Python 3.9+
- PostgreSQL (or use the built-in SQLite fallback for zero-install testing)

### 2. Install Dependencies
```bash
cd server
python3 -m venv venv
source venv/bin/activate   # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 3. Configure Database (.env)
Copy the template and configure your connection:
```bash
cp .env.example .env
```

Edit `.env`:
```ini
# PostgreSQL (Production / LAN Central Server):
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/network_intelligence

# Or SQLite (Zero-config instant testing):
# DATABASE_URL=sqlite:///./network_intelligence.db

SERVER_HOST=0.0.0.0
SERVER_PORT=8000
API_KEY=nt04-network-admin-secret-token
```

### 4. Create PostgreSQL Database (if using PostgreSQL)
```sql
CREATE DATABASE network_intelligence;
```

### 5. Start the FastAPI Central Server
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
The server will automatically generate all 10 SQL tables on startup!
- Interactive OpenAPI Docs: `http://localhost:8000/docs`
- Health Check: `http://localhost:8000/api/health`

---

## 🌐 Network Setup Scenarios

### 💻 Scenario A: Single Laptop / Same Device

#### A1. Android Emulator (Android Studio Emulator)
- In the Android App's **Server Sync** tab, tap **Emulator Preset (10.0.2.2)**.
- The emulator's `10.0.2.2` IP automatically routes to your laptop's `127.0.0.1:8000`.
- Tap **TEST** or **CONNECT**, then **SYNC**.

#### A2. Physical Android Phone connected via USB
Run the ADB reverse port forwarding command in your terminal:
```bash
adb reverse tcp:8000 tcp:8000
```
- In the Android App's **Server Sync** tab, set Host to `127.0.0.1` and Port `8000`.
- Tap **CONNECT**.

---

### 🖥️ Scenario B: Two Different Laptops over Local Network (LAN)

#### Laptop 1 (Central Server Laptop):
1. Find Laptop 1's local LAN IP address:
   - **Linux / macOS**: `ifconfig` or `ip addr` (look for `192.168.x.x` or `10.x.x.x`)
   - **Windows**: `ipconfig`
2. Start the server binding to all network interfaces:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8000
   ```
3. Make sure firewall allows inbound port 8000:
   - **Linux**: `sudo ufw allow 8000/tcp`
   - **Windows**: Allow Python in Windows Defender Firewall

#### Laptop 2 (Client / Android Device or Emulator on Laptop 2):
1. Open the Android App -> Navigate to **Server Sync Gateway** (tab 4).
2. Enter Laptop 1's LAN IP (e.g. `192.168.1.20`) and Port `8000`.
3. Enter API Key: `nt04-network-admin-secret-token`.
4. Tap **TEST & CONNECT** -> You will see the latency, PostgreSQL status, and connected clients.
5. Tap **SYNC** -> All local Room captures, devices, DNS queries, and security alerts will upload in real time.

---

## 🗄️ PostgreSQL Direct Verification Queries

Open `psql -U postgres -d network_intelligence` or pgAdmin:

```sql
-- 1. View Registered Client Devices
SELECT client_id, client_name, ip_address, os_version, device_model, is_active FROM clients;

-- 2. View Synchronized Network Capture Sessions
SELECT session_id, client_id, network_name, interface_name, local_ip, total_packets, total_bytes 
FROM network_sessions ORDER BY start_time DESC;

-- 3. View Discovered Network Devices & Topology
SELECT device_id, ip_address, hostname, vendor, device_type, is_active 
FROM network_devices ORDER BY last_seen DESC;

-- 4. View Synchronized Security Alerts & Anomalies
SELECT event_id, severity, event_type, source, destination, protocol, confidence, description 
FROM security_events ORDER BY timestamp DESC;

-- 5. View Aggregated Traffic & Service Intelligence
SELECT service_name, domain, destination_ip, protocol, traffic_bytes, classification 
FROM service_observations ORDER BY traffic_bytes DESC;

-- 6. View DNS Query Logs
SELECT domain, query_type, response_status, response_time_ms, timestamp 
FROM dns_history ORDER BY timestamp DESC;
```

---

## 🧪 Automated Testing
Run the included test script to verify server functionality:
```bash
python test_server.py
```
Expected output:
```text
[1/4] Testing /api/health... Status: 200 OK
[2/4] Testing /api/clients/register... Status: 200 OK
[3/4] Testing /api/sync... Status: 200 OK
[4/4] Testing /api/database/stats... Status: 200 OK
 ALL CENTRAL SERVER API TESTS PASSED!
```
