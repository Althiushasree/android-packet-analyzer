from sqlalchemy import Column, String, Integer, BigInteger, Float, Boolean, ForeignKey, Index
from sqlalchemy.orm import relationship
from app.database import Base


class Client(Base):
    __tablename__ = "clients"

    client_id = Column(String(64), primary_key=True, index=True)
    client_name = Column(String(128), nullable=False)
    ip_address = Column(String(64), nullable=True)
    os_version = Column(String(64), nullable=True)
    app_version = Column(String(64), nullable=True)
    device_model = Column(String(128), nullable=True)
    first_seen = Column(BigInteger, nullable=False)
    last_seen = Column(BigInteger, nullable=False)
    is_active = Column(Boolean, default=True)

    sessions = relationship("NetworkSession", back_populates="client")


class NetworkSession(Base):
    __tablename__ = "network_sessions"

    session_id = Column(String(64), primary_key=True, index=True)
    client_id = Column(String(64), ForeignKey("clients.client_id"), nullable=False, index=True)
    start_time = Column(BigInteger, nullable=False, index=True)
    end_time = Column(BigInteger, nullable=True)
    network_name = Column(String(128), nullable=False)
    interface_name = Column(String(64), nullable=False)
    interface_type = Column(String(32), nullable=False)
    local_ip = Column(String(64), nullable=False)
    ipv6 = Column(String(128), default="")
    mac_address = Column(String(64), default="")
    gateway = Column(String(64), default="")
    dns_servers = Column(String(256), default="")
    subnet = Column(String(64), default="")
    capture_status = Column(String(32), default="ACTIVE")
    total_packets = Column(BigInteger, default=0)
    total_bytes = Column(BigInteger, default=0)
    upload_bytes = Column(BigInteger, default=0)
    download_bytes = Column(BigInteger, default=0)

    client = relationship("Client", back_populates="sessions")
    device_history = relationship("DeviceSessionHistory", back_populates="session")
    traffic_stats = relationship("TrafficStatistic", back_populates="session")
    services = relationship("ServiceObservation", back_populates="session")
    dns_logs = relationship("DnsHistory", back_populates="session")
    connections = relationship("ConnectionHistory", back_populates="session")
    security_events = relationship("SecurityEvent", back_populates="session")
    health_records = relationship("NetworkHealthHistory", back_populates="session")


class NetworkDevice(Base):
    __tablename__ = "network_devices"

    device_id = Column(String(64), primary_key=True, index=True)
    client_id = Column(String(64), index=True, nullable=True)
    ip_address = Column(String(64), nullable=False, index=True)
    ipv6 = Column(String(128), default="")
    mac_address = Column(String(64), default="")
    hostname = Column(String(128), default="Unknown")
    vendor = Column(String(128), default="Generic")
    device_type = Column(String(64), default="Host")
    first_seen = Column(BigInteger, nullable=False)
    last_seen = Column(BigInteger, nullable=False, index=True)
    is_active = Column(Boolean, default=True)


class DeviceSessionHistory(Base):
    __tablename__ = "device_session_history"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    device_id = Column(String(64), nullable=False, index=True)
    ip_address = Column(String(64), nullable=False)
    first_seen = Column(BigInteger, nullable=False)
    last_seen = Column(BigInteger, nullable=False)
    packets = Column(BigInteger, default=0)
    bytes = Column(BigInteger, default=0)
    upload = Column(BigInteger, default=0)
    download = Column(BigInteger, default=0)
    active_connections = Column(Integer, default=0)
    protocols = Column(String(256), default="")
    ports = Column(String(256), default="")

    session = relationship("NetworkSession", back_populates="device_history")


class TrafficStatistic(Base):
    __tablename__ = "traffic_statistics"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    device = Column(String(128), default="All Devices")
    protocol = Column(String(32), default="TCP")
    bytes = Column(BigInteger, default=0)
    packets = Column(BigInteger, default=0)
    upload = Column(BigInteger, default=0)
    download = Column(BigInteger, default=0)
    connections = Column(Integer, default=0)

    session = relationship("NetworkSession", back_populates="traffic_stats")


class ServiceObservation(Base):
    __tablename__ = "service_observations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    device_id = Column(String(64), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    service_name = Column(String(128), nullable=False)
    domain = Column(String(256), default="")
    destination_ip = Column(String(64), default="")
    protocol = Column(String(32), default="TCP")
    port = Column(Integer, default=0)
    traffic_bytes = Column(BigInteger, default=0)
    classification = Column(String(64), default="General")
    confidence = Column(String(32), default="HIGH")
    evidence = Column(String(512), default="")

    session = relationship("NetworkSession", back_populates="services")


class DnsHistory(Base):
    __tablename__ = "dns_history"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    device_id = Column(String(64), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    dns_server = Column(String(64), default="")
    domain = Column(String(256), nullable=False, index=True)
    query_type = Column(String(16), default="A")
    response = Column(String(512), default="")
    response_status = Column(String(32), default="NOERROR")
    response_time_ms = Column(BigInteger, default=0)

    session = relationship("NetworkSession", back_populates="dns_logs")


class ConnectionHistory(Base):
    __tablename__ = "connection_history"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    device_id = Column(String(64), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    source_ip = Column(String(64), nullable=False)
    destination_ip = Column(String(64), nullable=False)
    source_port = Column(Integer, default=0)
    destination_port = Column(Integer, default=0)
    protocol = Column(String(32), default="TCP")
    bytes = Column(BigInteger, default=0)
    packets = Column(BigInteger, default=0)
    duration = Column(Float, default=0.0)
    status = Column(String(32), default="ESTABLISHED")

    session = relationship("NetworkSession", back_populates="connections")


class SecurityEvent(Base):
    __tablename__ = "security_events"

    event_id = Column(String(64), primary_key=True, index=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    device_id = Column(String(64), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    severity = Column(String(32), nullable=False, index=True)
    event_type = Column(String(64), nullable=False)
    source = Column(String(128), default="")
    destination = Column(String(128), default="")
    protocol = Column(String(32), default="")
    port = Column(Integer, default=0)
    evidence = Column(String(512), default="")
    confidence = Column(String(32), default="HIGH")
    description = Column(String(512), default="")
    status = Column(String(32), default="NEW")

    session = relationship("NetworkSession", back_populates="security_events")


class NetworkHealthHistory(Base):
    __tablename__ = "network_health_history"

    id = Column(Integer, primary_key=True, autoincrement=True)
    session_id = Column(String(64), ForeignKey("network_sessions.session_id"), nullable=False, index=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    latency = Column(Float, default=0.0)
    packet_loss = Column(Float, default=0.0)
    dns_latency = Column(Float, default=0.0)
    throughput = Column(Float, default=0.0)
    retransmissions = Column(BigInteger, default=0)
    connection_failures = Column(Integer, default=0)
    interface_errors = Column(BigInteger, default=0)
    health_score = Column(Integer, default=100)

    session = relationship("NetworkSession", back_populates="health_records")
