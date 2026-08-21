package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ConnectionHistoryEntity
import com.example.data.model.DataRetentionSettingsEntity
import com.example.data.model.DeviceSessionHistoryEntity
import com.example.data.model.DnsHistoryEntity
import com.example.data.model.NetworkDeviceEntity
import com.example.data.model.NetworkHealthHistoryEntity
import com.example.data.model.NetworkSessionEntity
import com.example.data.model.SecurityEventEntity
import com.example.data.model.ServiceObservationEntity
import com.example.data.model.TrafficStatisticEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkSessionDao {
  @Query("SELECT * FROM network_sessions ORDER BY startTime DESC")
  suspend fun getAllSessions(): List<NetworkSessionEntity>

  @Query("SELECT * FROM network_sessions ORDER BY startTime DESC")
  fun getAllSessionsFlow(): Flow<List<NetworkSessionEntity>>

  @Query("SELECT * FROM network_sessions WHERE sessionId = :sessionId LIMIT 1")
  suspend fun getSessionById(sessionId: String): NetworkSessionEntity?

  @Query("SELECT * FROM network_sessions WHERE captureStatus = 'ACTIVE' ORDER BY startTime DESC LIMIT 1")
  suspend fun getActiveSession(): NetworkSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: NetworkSessionEntity): Long

  @Update
  suspend fun updateSession(session: NetworkSessionEntity)

  @Query("UPDATE network_sessions SET endTime = :endTime, captureStatus = 'COMPLETED', totalPackets = :totalPackets, totalBytes = :totalBytes, uploadBytes = :uploadBytes, downloadBytes = :downloadBytes WHERE sessionId = :sessionId")
  suspend fun closeSession(
    sessionId: String,
    endTime: Long,
    totalPackets: Long,
    totalBytes: Long,
    uploadBytes: Long,
    downloadBytes: Long
  )

  @Query("DELETE FROM network_sessions WHERE sessionId = :sessionId")
  suspend fun deleteSession(sessionId: String)

  @Query("SELECT COUNT(*) FROM network_sessions")
  suspend fun getSessionCount(): Int
}

@Dao
interface NetworkDeviceDao {
  @Query("SELECT * FROM network_devices ORDER BY lastSeen DESC")
  suspend fun getAllDevices(): List<NetworkDeviceEntity>

  @Query("SELECT * FROM network_devices ORDER BY lastSeen DESC")
  fun getAllDevicesFlow(): Flow<List<NetworkDeviceEntity>>

  @Query("SELECT * FROM network_devices WHERE deviceId = :deviceId LIMIT 1")
  suspend fun getDeviceById(deviceId: String): NetworkDeviceEntity?

  @Query("SELECT * FROM network_devices WHERE ipAddress = :ipAddress LIMIT 1")
  suspend fun getDeviceByIp(ipAddress: String): NetworkDeviceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateDevice(device: NetworkDeviceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDevices(devices: List<NetworkDeviceEntity>)

  @Query("SELECT COUNT(*) FROM network_devices")
  suspend fun getDeviceCount(): Int
}

@Dao
interface DeviceSessionHistoryDao {
  @Query("SELECT * FROM device_session_history ORDER BY id DESC")
  suspend fun getAllDeviceHistory(): List<DeviceSessionHistoryEntity>

  @Query("SELECT * FROM device_session_history WHERE sessionId = :sessionId ORDER BY bytes DESC")
  fun getDevicesForSessionFlow(sessionId: String): Flow<List<DeviceSessionHistoryEntity>>

  @Query("SELECT * FROM device_session_history WHERE sessionId = :sessionId ORDER BY bytes DESC")
  suspend fun getDevicesForSession(sessionId: String): List<DeviceSessionHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateDeviceSession(record: DeviceSessionHistoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(records: List<DeviceSessionHistoryEntity>)
}

@Dao
interface TrafficStatisticDao {
  @Query("SELECT * FROM traffic_statistics ORDER BY timestamp DESC")
  suspend fun getAllTrafficStats(): List<TrafficStatisticEntity>

  @Query("SELECT * FROM traffic_statistics WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getTrafficStatsForSessionFlow(sessionId: String): Flow<List<TrafficStatisticEntity>>

  @Query("SELECT * FROM traffic_statistics WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  suspend fun getTrafficStatsForSession(sessionId: String): List<TrafficStatisticEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStatistic(stat: TrafficStatisticEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(stats: List<TrafficStatisticEntity>)

  @Query("DELETE FROM traffic_statistics WHERE timestamp < :cutoffTimestamp")
  suspend fun deleteOlderThan(cutoffTimestamp: Long): Int
}

@Dao
interface ServiceObservationDao {
  @Query("SELECT * FROM service_observations ORDER BY timestamp DESC")
  suspend fun getAllServices(): List<ServiceObservationEntity>

  @Query("SELECT * FROM service_observations WHERE sessionId = :sessionId ORDER BY trafficBytes DESC")
  fun getServicesForSessionFlow(sessionId: String): Flow<List<ServiceObservationEntity>>

  @Query("SELECT * FROM service_observations WHERE sessionId = :sessionId ORDER BY trafficBytes DESC")
  suspend fun getServicesForSession(sessionId: String): List<ServiceObservationEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertObservation(observation: ServiceObservationEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(observations: List<ServiceObservationEntity>)
}

@Dao
interface DnsHistoryDao {
  @Query("SELECT * FROM dns_history ORDER BY timestamp DESC")
  suspend fun getAllDns(): List<DnsHistoryEntity>

  @Query("SELECT * FROM dns_history WHERE sessionId = :sessionId ORDER BY timestamp DESC")
  fun getDnsHistoryForSessionFlow(sessionId: String): Flow<List<DnsHistoryEntity>>

  @Query("SELECT * FROM dns_history WHERE domain LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 100")
  suspend fun searchDns(query: String): List<DnsHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDns(dns: DnsHistoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(records: List<DnsHistoryEntity>)
}

@Dao
interface ConnectionHistoryDao {
  @Query("SELECT * FROM connection_history ORDER BY timestamp DESC")
  suspend fun getAllConnections(): List<ConnectionHistoryEntity>

  @Query("SELECT * FROM connection_history WHERE sessionId = :sessionId ORDER BY timestamp DESC")
  fun getConnectionsForSessionFlow(sessionId: String): Flow<List<ConnectionHistoryEntity>>

  @Query("SELECT * FROM connection_history WHERE sourceIp LIKE '%' || :query || '%' OR destinationIp LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 200")
  suspend fun searchConnections(query: String): List<ConnectionHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConnection(conn: ConnectionHistoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(records: List<ConnectionHistoryEntity>)
}

@Dao
interface SecurityEventDao {
  @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
  suspend fun getAllSecurityEvents(): List<SecurityEventEntity>

  @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
  fun getAllSecurityEventsFlow(): Flow<List<SecurityEventEntity>>

  @Query("SELECT * FROM security_events WHERE sessionId = :sessionId ORDER BY timestamp DESC")
  fun getSecurityEventsForSessionFlow(sessionId: String): Flow<List<SecurityEventEntity>>

  @Query("SELECT * FROM security_events WHERE sessionId = :sessionId ORDER BY timestamp DESC")
  suspend fun getSecurityEventsForSession(sessionId: String): List<SecurityEventEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEvent(event: SecurityEventEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(events: List<SecurityEventEntity>)

  @Query("DELETE FROM security_events WHERE timestamp < :cutoffTimestamp")
  suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

  @Query("SELECT COUNT(*) FROM security_events")
  suspend fun getSecurityEventCount(): Int
}

@Dao
interface NetworkHealthHistoryDao {
  @Query("SELECT * FROM network_health_history ORDER BY timestamp DESC")
  suspend fun getAllHealth(): List<NetworkHealthHistoryEntity>

  @Query("SELECT * FROM network_health_history WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getHealthHistoryForSessionFlow(sessionId: String): Flow<List<NetworkHealthHistoryEntity>>

  @Query("SELECT * FROM network_health_history WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  suspend fun getHealthHistoryForSession(sessionId: String): List<NetworkHealthHistoryEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHealth(health: NetworkHealthHistoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBatch(records: List<NetworkHealthHistoryEntity>)
}

@Dao
interface DataRetentionDao {
  @Query("SELECT * FROM data_retention_settings WHERE id = 1")
  fun getRetentionSettingsFlow(): Flow<DataRetentionSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveRetentionSettings(settings: DataRetentionSettingsEntity)
}
