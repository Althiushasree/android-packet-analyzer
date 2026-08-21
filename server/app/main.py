import time
import os
from typing import List
from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.database import engine, Base, get_db
from app.auth import verify_api_key
import app.models as models
import app.schemas as schemas

# Create database tables automatically on launch
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="NT04 Network Intelligence Central Server",
    description="High-performance backend for multi-device network monitoring and PostgreSQL synchronization",
    version="1.0.0"
)

# Enable CORS for cross-origin browser dashboards or API clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def root_info():
    return {
        "service": "NT04 Network Intelligence Central Server",
        "status": "ONLINE",
        "docs": "/docs",
        "health": "/api/health"
    }


@app.get("/api/health", response_model=schemas.HealthResponse)
def get_health(
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    try:
        clients_count = db.query(models.Client).count()
        sessions_count = db.query(models.NetworkSession).count()
        db_type = "PostgreSQL" if "postgresql" in str(engine.url) else "SQLite"

        return schemas.HealthResponse(
            status="healthy",
            database=f"{db_type} (connected)",
            timestamp=int(time.time() * 1000),
            version="1.0.0",
            clients_count=clients_count,
            sessions_count=sessions_count
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Database health check failed: {str(e)}"
        )


@app.post("/api/clients/register", response_model=schemas.ClientRegisterResponse)
def register_client(
    req: schemas.ClientRegisterRequest,
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    now_ms = int(time.time() * 1000)
    client = db.query(models.Client).filter(models.Client.client_id == req.client_id).first()

    if client:
        client.client_name = req.client_name
        client.ip_address = req.ip_address
        client.os_version = req.os_version
        client.app_version = req.app_version
        client.device_model = req.device_model
        client.last_seen = now_ms
        client.is_active = True
    else:
        client = models.Client(
            client_id=req.client_id,
            client_name=req.client_name,
            ip_address=req.ip_address,
            os_version=req.os_version,
            app_version=req.app_version,
            device_model=req.device_model,
            first_seen=now_ms,
            last_seen=now_ms,
            is_active=True
        )
        db.add(client)

    db.commit()
    return schemas.ClientRegisterResponse(
        status="registered",
        client_id=client.client_id,
        registered_at=now_ms
    )


@app.post("/api/sync", response_model=schemas.BatchSyncResponse)
def sync_batch(
    req: schemas.BatchSyncRequest,
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    now_ms = int(time.time() * 1000)
    synced_records = 0

    try:
        # Ensure client exists
        client = db.query(models.Client).filter(models.Client.client_id == req.client_id).first()
        if not client:
            client = models.Client(
                client_id=req.client_id,
                client_name=f"Client-{req.client_id[:6]}",
                first_seen=now_ms,
                last_seen=now_ms,
                is_active=True
            )
            db.add(client)
            db.flush()
        else:
            client.last_seen = now_ms

        # 1. Sync Sessions (Merge / Upsert)
        for s in req.sessions:
            existing = db.query(models.NetworkSession).filter(models.NetworkSession.session_id == s.session_id).first()
            if existing:
                existing.end_time = s.end_time
                existing.total_packets = s.total_packets or 0
                existing.total_bytes = s.total_bytes or 0
                existing.upload_bytes = s.upload_bytes or 0
                existing.download_bytes = s.download_bytes or 0
                existing.capture_status = s.capture_status or "ACTIVE"
            else:
                db.add(models.NetworkSession(
                    session_id=s.session_id,
                    client_id=s.client_id,
                    start_time=s.start_time,
                    end_time=s.end_time,
                    network_name=s.network_name,
                    interface_name=s.interface_name,
                    interface_type=s.interface_type,
                    local_ip=s.local_ip,
                    ipv6=s.ipv6 or "",
                    mac_address=s.mac_address or "",
                    gateway=s.gateway or "",
                    dns_servers=s.dns_servers or "",
                    subnet=s.subnet or "",
                    capture_status=s.capture_status or "ACTIVE",
                    total_packets=s.total_packets or 0,
                    total_bytes=s.total_bytes or 0,
                    upload_bytes=s.upload_bytes or 0,
                    download_bytes=s.download_bytes or 0
                ))
            synced_records += 1

        # 2. Sync Devices
        for d in req.devices:
            dev = db.query(models.NetworkDevice).filter(models.NetworkDevice.device_id == d.device_id).first()
            if dev:
                dev.last_seen = d.last_seen
                dev.ip_address = d.ip_address
                dev.is_active = d.is_active if d.is_active is not None else True
            else:
                db.add(models.NetworkDevice(
                    device_id=d.device_id,
                    client_id=d.client_id or req.client_id,
                    ip_address=d.ip_address,
                    ipv6=d.ipv6 or "",
                    mac_address=d.mac_address or "",
                    hostname=d.hostname or "Unknown",
                    vendor=d.vendor or "Generic",
                    device_type=d.device_type or "Host",
                    first_seen=d.first_seen,
                    last_seen=d.last_seen,
                    is_active=d.is_active if d.is_active is not None else True
                ))
            synced_records += 1

        # 3. Sync Device Session History
        for dh in req.device_history:
            db.add(models.DeviceSessionHistory(
                session_id=dh.session_id,
                device_id=dh.device_id,
                ip_address=dh.ip_address,
                first_seen=dh.first_seen,
                last_seen=dh.last_seen,
                packets=dh.packets or 0,
                bytes=dh.bytes or 0,
                upload=dh.upload or 0,
                download=dh.download or 0,
                active_connections=dh.active_connections or 0,
                protocols=dh.protocols or "",
                ports=dh.ports or ""
            ))
            synced_records += 1

        # 4. Sync Traffic Stats
        for ts in req.traffic_stats:
            db.add(models.TrafficStatistic(
                session_id=ts.session_id,
                timestamp=ts.timestamp,
                device=ts.device or "All Devices",
                protocol=ts.protocol or "TCP",
                bytes=ts.bytes or 0,
                packets=ts.packets or 0,
                upload=ts.upload or 0,
                download=ts.download or 0,
                connections=ts.connections or 0
            ))
            synced_records += 1

        # 5. Sync Services
        for sv in req.services:
            db.add(models.ServiceObservation(
                session_id=sv.session_id,
                device_id=sv.device_id,
                timestamp=sv.timestamp,
                service_name=sv.service_name,
                domain=sv.domain or "",
                destination_ip=sv.destination_ip or "",
                protocol=sv.protocol or "TCP",
                port=sv.port or 0,
                traffic_bytes=sv.traffic_bytes or 0,
                classification=sv.classification or "General",
                confidence=sv.confidence or "HIGH",
                evidence=sv.evidence or ""
            ))
            synced_records += 1

        # 6. Sync DNS Logs
        for dn in req.dns_logs:
            db.add(models.DnsHistory(
                session_id=dn.session_id,
                device_id=dn.device_id,
                timestamp=dn.timestamp,
                dns_server=dn.dns_server or "",
                domain=dn.domain,
                query_type=dn.query_type or "A",
                response=dn.response or "",
                response_status=dn.response_status or "NOERROR",
                response_time_ms=dn.response_time_ms or 0
            ))
            synced_records += 1

        # 7. Sync Connections
        for cn in req.connections:
            db.add(models.ConnectionHistory(
                session_id=cn.session_id,
                device_id=cn.device_id,
                timestamp=cn.timestamp,
                source_ip=cn.source_ip,
                destination_ip=cn.destination_ip,
                source_port=cn.source_port or 0,
                destination_port=cn.destination_port or 0,
                protocol=cn.protocol or "TCP",
                bytes=cn.bytes or 0,
                packets=cn.packets or 0,
                duration=cn.duration or 0.0,
                status=cn.status or "ESTABLISHED"
            ))
            synced_records += 1

        # 8. Sync Security Events
        for se in req.security_events:
            existing_event = db.query(models.SecurityEvent).filter(models.SecurityEvent.event_id == se.event_id).first()
            if not existing_event:
                db.add(models.SecurityEvent(
                    event_id=se.event_id,
                    session_id=se.session_id,
                    device_id=se.device_id,
                    timestamp=se.timestamp,
                    severity=se.severity,
                    event_type=se.event_type,
                    source=se.source or "",
                    destination=se.destination or "",
                    protocol=se.protocol or "",
                    port=se.port or 0,
                    evidence=se.evidence or "",
                    confidence=se.confidence or "HIGH",
                    description=se.description or "",
                    status=se.status or "NEW"
                ))
                synced_records += 1

        # 9. Sync Health Records
        for hr in req.health_records:
            db.add(models.NetworkHealthHistory(
                session_id=hr.session_id,
                timestamp=hr.timestamp,
                latency=hr.latency or 0.0,
                packet_loss=hr.packet_loss or 0.0,
                dns_latency=hr.dns_latency or 0.0,
                throughput=hr.throughput or 0.0,
                retransmissions=hr.retransmissions or 0,
                connection_failures=hr.connection_failures or 0,
                interface_errors=hr.interface_errors or 0,
                health_score=hr.health_score or 100
            ))
            synced_records += 1

        db.commit()

        return schemas.BatchSyncResponse(
            status="success",
            received_at=now_ms,
            synced_records=synced_records,
            message=f"Successfully synced {synced_records} records"
        )
    except Exception as e:
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Batch sync failed: {str(e)}"
        )


@app.get("/api/database/stats", response_model=schemas.DatabaseStatsResponse)
def get_database_stats(
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    return schemas.DatabaseStatsResponse(
        total_clients=db.query(models.Client).count(),
        total_sessions=db.query(models.NetworkSession).count(),
        total_devices=db.query(models.NetworkDevice).count(),
        total_traffic_records=db.query(models.TrafficStatistic).count(),
        total_dns_records=db.query(models.DnsHistory).count(),
        total_connections=db.query(models.ConnectionHistory).count(),
        total_security_events=db.query(models.SecurityEvent).count(),
        total_health_records=db.query(models.NetworkHealthHistory).count(),
        db_status="CONNECTED"
    )


@app.get("/api/sessions", response_model=List[schemas.NetworkSessionDto])
def list_sessions(
    limit: int = 50,
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    sessions = db.query(models.NetworkSession).order_by(models.NetworkSession.start_time.desc()).limit(limit).all()
    return [
        schemas.NetworkSessionDto(
            session_id=s.session_id,
            client_id=s.client_id,
            start_time=s.start_time,
            end_time=s.end_time,
            network_name=s.network_name,
            interface_name=s.interface_name,
            interface_type=s.interface_type,
            local_ip=s.local_ip,
            ipv6=s.ipv6,
            mac_address=s.mac_address,
            gateway=s.gateway,
            dns_servers=s.dns_servers,
            subnet=s.subnet,
            capture_status=s.capture_status,
            total_packets=s.total_packets,
            total_bytes=s.total_bytes,
            upload_bytes=s.upload_bytes,
            download_bytes=s.download_bytes
        )
        for s in sessions
    ]


@app.get("/api/sessions/{session_id}", response_model=schemas.NetworkSessionDto)
def get_session(
    session_id: str,
    api_key: str = Depends(verify_api_key),
    db: Session = Depends(get_db)
):
    s = db.query(models.NetworkSession).filter(models.NetworkSession.session_id == session_id).first()
    if not s:
        raise HTTPException(status_code=404, detail="Session not found")
    return schemas.NetworkSessionDto(
        session_id=s.session_id,
        client_id=s.client_id,
        start_time=s.start_time,
        end_time=s.end_time,
        network_name=s.network_name,
        interface_name=s.interface_name,
        interface_type=s.interface_type,
        local_ip=s.local_ip,
        ipv6=s.ipv6,
        mac_address=s.mac_address,
        gateway=s.gateway,
        dns_servers=s.dns_servers,
        subnet=s.subnet,
        capture_status=s.capture_status,
        total_packets=s.total_packets,
        total_bytes=s.total_bytes,
        upload_bytes=s.upload_bytes,
        download_bytes=s.download_bytes
    )
