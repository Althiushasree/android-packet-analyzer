from typing import List, Optional
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str
    database: str
    timestamp: int
    version: str = "1.0.0"
    clients_count: int = 0
    sessions_count: int = 0


class ClientRegisterRequest(BaseModel):
    client_id: str
    client_name: str
    ip_address: Optional[str] = "127.0.0.1"
    os_version: Optional[str] = ""
    app_version: Optional[str] = ""
    device_model: Optional[str] = ""


class ClientRegisterResponse(BaseModel):
    status: str
    client_id: str
    registered_at: int


class NetworkSessionDto(BaseModel):
    session_id: str
    client_id: str
    start_time: int
    end_time: Optional[int] = None
    network_name: str
    interface_name: str
    interface_type: str
    local_ip: str
    ipv6: Optional[str] = ""
    mac_address: Optional[str] = ""
    gateway: Optional[str] = ""
    dns_servers: Optional[str] = ""
    subnet: Optional[str] = ""
    capture_status: Optional[str] = "ACTIVE"
    total_packets: Optional[int] = 0
    total_bytes: Optional[int] = 0
    upload_bytes: Optional[int] = 0
    download_bytes: Optional[int] = 0


class NetworkDeviceDto(BaseModel):
    device_id: str
    client_id: Optional[str] = None
    ip_address: str
    ipv6: Optional[str] = ""
    mac_address: Optional[str] = ""
    hostname: Optional[str] = "Unknown"
    vendor: Optional[str] = "Generic"
    device_type: Optional[str] = "Host"
    first_seen: int
    last_seen: int
    is_active: Optional[bool] = True


class DeviceSessionHistoryDto(BaseModel):
    session_id: str
    device_id: str
    ip_address: str
    first_seen: int
    last_seen: int
    packets: Optional[int] = 0
    bytes: Optional[int] = 0
    upload: Optional[int] = 0
    download: Optional[int] = 0
    active_connections: Optional[int] = 0
    protocols: Optional[str] = ""
    ports: Optional[str] = ""


class TrafficStatisticDto(BaseModel):
    session_id: str
    timestamp: int
    device: Optional[str] = "All Devices"
    protocol: Optional[str] = "TCP"
    bytes: Optional[int] = 0
    packets: Optional[int] = 0
    upload: Optional[int] = 0
    download: Optional[int] = 0
    connections: Optional[int] = 0


class ServiceObservationDto(BaseModel):
    session_id: str
    device_id: str
    timestamp: int
    service_name: str
    domain: Optional[str] = ""
    destination_ip: Optional[str] = ""
    protocol: Optional[str] = "TCP"
    port: Optional[int] = 0
    traffic_bytes: Optional[int] = 0
    classification: Optional[str] = "General"
    confidence: Optional[str] = "HIGH"
    evidence: Optional[str] = ""


class DnsHistoryDto(BaseModel):
    session_id: str
    device_id: str
    timestamp: int
    dns_server: Optional[str] = ""
    domain: str
    query_type: Optional[str] = "A"
    response: Optional[str] = ""
    response_status: Optional[str] = "NOERROR"
    response_time_ms: Optional[int] = 0


class ConnectionHistoryDto(BaseModel):
    session_id: str
    device_id: str
    timestamp: int
    source_ip: str
    destination_ip: str
    source_port: Optional[int] = 0
    destination_port: Optional[int] = 0
    protocol: Optional[str] = "TCP"
    bytes: Optional[int] = 0
    packets: Optional[int] = 0
    duration: Optional[float] = 0.0
    status: Optional[str] = "ESTABLISHED"


class SecurityEventDto(BaseModel):
    event_id: str
    session_id: str
    device_id: str
    timestamp: int
    severity: str
    event_type: str
    source: Optional[str] = ""
    destination: Optional[str] = ""
    protocol: Optional[str] = ""
    port: Optional[int] = 0
    evidence: Optional[str] = ""
    confidence: Optional[str] = "HIGH"
    description: Optional[str] = ""
    status: Optional[str] = "NEW"


class NetworkHealthHistoryDto(BaseModel):
    session_id: str
    timestamp: int
    latency: Optional[float] = 0.0
    packet_loss: Optional[float] = 0.0
    dns_latency: Optional[float] = 0.0
    throughput: Optional[float] = 0.0
    retransmissions: Optional[int] = 0
    connection_failures: Optional[int] = 0
    interface_errors: Optional[int] = 0
    health_score: Optional[int] = 100


class BatchSyncRequest(BaseModel):
    client_id: str
    sync_timestamp: int
    sessions: List[NetworkSessionDto] = []
    devices: List[NetworkDeviceDto] = []
    device_history: List[DeviceSessionHistoryDto] = []
    traffic_stats: List[TrafficStatisticDto] = []
    services: List[ServiceObservationDto] = []
    dns_logs: List[DnsHistoryDto] = []
    connections: List[ConnectionHistoryDto] = []
    security_events: List[SecurityEventDto] = []
    health_records: List[NetworkHealthHistoryDto] = []


class BatchSyncResponse(BaseModel):
    status: str
    received_at: int
    synced_records: int
    message: str = "Sync successful"


class DatabaseStatsResponse(BaseModel):
    total_clients: int
    total_sessions: int
    total_devices: int
    total_traffic_records: int
    total_dns_records: int
    total_connections: int
    total_security_events: int
    total_health_records: int
    db_status: str
