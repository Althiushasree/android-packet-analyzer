#!/usr/bin/env python3
"""
Test script for NT04 Network Intelligence Central Server.
Verifies connection, health endpoint, client registration, and batch synchronization.
"""

import requests
import json
import time

BASE_URL = "http://localhost:8000"
API_KEY = "nt04-network-admin-secret-token"

headers = {
    "X-API-Key": API_KEY,
    "Content-Type": "application/json"
}


def test_health():
    print("[1/4] Testing /api/health...")
    resp = requests.get(f"{BASE_URL}/api/health", headers=headers)
    print(f"Status Code: {resp.status_code}")
    print(f"Response: {resp.json()}")
    assert resp.status_code == 200, "Health check failed"


def test_register():
    print("\n[2/4] Testing /api/clients/register...")
    payload = {
        "client_id": "TEST-CLIENT-001",
        "client_name": "CLI Test Device",
        "ip_address": "192.168.1.50",
        "os_version": "Android 14 (API 34)",
        "app_version": "1.0-NT04",
        "device_model": "Pixel 8 Pro"
    }
    resp = requests.post(f"{BASE_URL}/api/clients/register", headers=headers, json=payload)
    print(f"Status Code: {resp.status_code}")
    print(f"Response: {resp.json()}")
    assert resp.status_code == 200, "Client registration failed"


def test_sync():
    print("\n[3/4] Testing /api/sync...")
    now_ms = int(time.time() * 1000)
    payload = {
        "client_id": "TEST-CLIENT-001",
        "sync_timestamp": now_ms,
        "sessions": [{
            "session_id": "SESSION-TEST-001",
            "client_id": "TEST-CLIENT-001",
            "start_time": now_ms - 60000,
            "end_time": now_ms,
            "network_name": "Test_WiFi_5G",
            "interface_name": "wlan0",
            "interface_type": "Wi-Fi",
            "local_ip": "192.168.1.50",
            "ipv6": "fe80::1",
            "mac_address": "AA:BB:CC:DD:EE:FF",
            "gateway": "192.168.1.1",
            "dns_servers": "1.1.1.1,8.8.8.8",
            "subnet": "255.255.255.0",
            "capture_status": "COMPLETED",
            "total_packets": 1250,
            "total_bytes": 1048576,
            "upload_bytes": 450000,
            "download_bytes": 598576
        }],
        "devices": [{
            "device_id": "DEV-TEST-001",
            "client_id": "TEST-CLIENT-001",
            "ip_address": "192.168.1.1",
            "mac_address": "00:11:22:33:44:55",
            "hostname": "gateway.home",
            "vendor": "Cisco",
            "device_type": "Router",
            "first_seen": now_ms - 60000,
            "last_seen": now_ms,
            "is_active": True
        }],
        "security_events": [{
            "event_id": "SEC-TEST-001",
            "session_id": "SESSION-TEST-001",
            "device_id": "DEV-TEST-001",
            "timestamp": now_ms,
            "severity": "MEDIUM",
            "event_type": "PORT_SCAN_SUSPICIOUS",
            "source": "192.168.1.50",
            "destination": "192.168.1.1",
            "protocol": "TCP",
            "port": 443,
            "evidence": "Rapid TCP SYN sequence detected",
            "confidence": "HIGH",
            "description": "Port probe detected on gateway",
            "status": "NEW"
        }]
    }
    resp = requests.post(f"{BASE_URL}/api/sync", headers=headers, json=payload)
    print(f"Status Code: {resp.status_code}")
    print(f"Response: {resp.json()}")
    assert resp.status_code == 200, "Sync batch failed"


def test_stats():
    print("\n[4/4] Testing /api/database/stats...")
    resp = requests.get(f"{BASE_URL}/api/database/stats", headers=headers)
    print(f"Status Code: {resp.status_code}")
    print(f"Response: {resp.json()}")
    assert resp.status_code == 200, "Database stats failed"


if __name__ == "__main__":
    try:
        test_health()
        test_register()
        test_sync()
        test_stats()
        print("\n ALL CENTRAL SERVER API TESTS PASSED!")
    except Exception as e:
        print(f"\n❌ Test failed: {e}")
