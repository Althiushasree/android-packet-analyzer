# 📡 Packet Capture Pro — Android Packet Analyzer

> **Professional Android network traffic monitoring, packet analysis, application/IP bandwidth analysis, traffic visualization, and deep network diagnostics.**

Packet Capture Pro is an Android-based network analysis application built with **Kotlin and Jetpack Compose**.

The application is designed to provide a mobile-friendly experience for analyzing network traffic, identifying high-bandwidth applications and IP addresses, monitoring packet flows, recording network usage, detecting traffic anomalies, and visualizing network activity through interactive dashboards.

---

## 🚀 Overview

Packet Capture Pro provides a comprehensive network monitoring dashboard that helps answer:

- Which application is consuming the most network data?
- Which IP address is generating the most traffic?
- Which applications communicate with a particular IP?
- Which IPs are accessed by a particular application?
- How much data is uploaded and downloaded?
- Which protocol is generating the most traffic?
- Are there unusual traffic spikes?
- Which applications exceed their bandwidth limits?
- How has network usage changed over time?
- What specific packets or connections generated the traffic?

---

# ✨ Key Features

## 📊 Interactive Network Dashboard

The main dashboard provides a real-time overview of network activity.

### KPI Metrics

- Total Data Transfer
- Upload Data
- Download Data
- Total Packets
- Average Throughput
- Live Throughput
- Active Connections
- Traffic Events

The dashboard uses a permanent **light/white theme** for clear visibility.

---

# 📱 Application Network Analysis

Packet Capture Pro identifies applications responsible for network traffic.

### Application statistics include:

- Application name
- Application icon
- Total data usage
- Upload usage
- Download usage
- Percentage of total traffic
- Packet count
- Destination IP addresses
- Protocol usage
- Port usage
- Historical usage

### Top Applications

The dashboard displays the highest network-consuming applications.

Example:

| Rank | Application | Data | Percentage |
|---|---|---:|---:|
| 1 | YouTube | 2.45 GB | 18.5% |
| 2 | Chrome | 1.81 GB | 14.5% |
| 3 | Instagram | 1.12 GB | 9.0% |
| 4 | Spotify | 890 MB | 7.1% |
| 5 | WhatsApp | 540 MB | 4.3% |
| - | Other | Remaining | - |

Applications are represented using:

- Application icons
- Interactive bar charts
- Donut charts
- Percentage indicators
- Traffic totals

---

# 🌐 IP Address Analysis

The application provides detailed network usage statistics for remote IP addresses.

### IP statistics include:

- Remote IP address
- Total traffic
- Upload
- Download
- Packets
- Percentage of network traffic
- Applications communicating with the IP
- Protocols
- Ports
- Historical usage

### Top IP Addresses

The dashboard identifies the IP addresses consuming the most network bandwidth.

Example:

| Rank | IP Address | Data | Percentage |
|---|---|---:|---:|
| 1 | 142.250.190.46 | 2.45 GB | 18.5% |
| 2 | 157.240.241.35 | 1.81 GB | 14.5% |
| 3 | 104.244.42.1 | 1.12 GB | 9.0% |
| 4 | 172.217.14.206 | 890 MB | 7.1% |
| 5 | 8.8.8.8 | 540 MB | 4.3% |
| - | Other IPs | Remaining | - |

---

# 🔗 Application ↔ IP Correlation

Packet Capture Pro connects application traffic with destination IP addresses.

For example:

```text
YouTube
   ↓
142.250.190.46
   ↓
2.45 GB
