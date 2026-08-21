package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ConnectionHistoryEntity
import com.example.data.model.DataRetentionSettingsEntity
import com.example.data.model.DeviceSessionHistoryEntity
import com.example.data.model.DnsHistoryEntity
import com.example.data.model.NetworkDeviceEntity
import com.example.data.model.NetworkHealthHistoryEntity
import com.example.data.model.NetworkSessionEntity
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PacketEntity
import com.example.data.model.PcapFileEntity
import com.example.data.model.SecurityEventEntity
import com.example.data.model.ServiceObservationEntity
import com.example.data.model.TrafficStatisticEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    PacketEntity::class,
    PcapFileEntity::class,
    NotificationSettingEntity::class,
    NetworkSessionEntity::class,
    NetworkDeviceEntity::class,
    DeviceSessionHistoryEntity::class,
    TrafficStatisticEntity::class,
    ServiceObservationEntity::class,
    DnsHistoryEntity::class,
    ConnectionHistoryEntity::class,
    SecurityEventEntity::class,
    NetworkHealthHistoryEntity::class,
    DataRetentionSettingsEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun packetDao(): PacketDao
  abstract fun pcapFileDao(): PcapFileDao
  abstract fun notificationSettingDao(): NotificationSettingDao
  abstract fun networkSessionDao(): NetworkSessionDao
  abstract fun networkDeviceDao(): NetworkDeviceDao
  abstract fun deviceSessionHistoryDao(): DeviceSessionHistoryDao
  abstract fun trafficStatisticDao(): TrafficStatisticDao
  abstract fun serviceObservationDao(): ServiceObservationDao
  abstract fun dnsHistoryDao(): DnsHistoryDao
  abstract fun connectionHistoryDao(): ConnectionHistoryDao
  abstract fun securityEventDao(): SecurityEventDao
  abstract fun networkHealthHistoryDao(): NetworkHealthHistoryDao
  abstract fun dataRetentionDao(): DataRetentionDao

  companion object {
    const val DATABASE_NAME = "packet_capture_pro.db"

    @Volatile
    private var INSTANCE: AppDatabase? = null

    /**
     * Migration from Version 1 to Version 2:
     * Adds persistent tables for network sessions, device registries,
     * traffic statistics, DNS history, flows, security findings, health, and retention.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `network_sessions` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `startTime` INTEGER NOT NULL,
            `endTime` INTEGER,
            `networkName` TEXT NOT NULL,
            `interfaceName` TEXT NOT NULL,
            `interfaceType` TEXT NOT NULL,
            `localIp` TEXT NOT NULL,
            `ipv6` TEXT NOT NULL,
            `macAddress` TEXT NOT NULL,
            `gateway` TEXT NOT NULL,
            `dnsServers` TEXT NOT NULL,
            `subnet` TEXT NOT NULL,
            `captureStatus` TEXT NOT NULL,
            `totalPackets` INTEGER NOT NULL,
            `totalBytes` INTEGER NOT NULL,
            `uploadBytes` INTEGER NOT NULL,
            `downloadBytes` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_network_sessions_sessionId` ON `network_sessions` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_sessions_startTime` ON `network_sessions` (`startTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_sessions_networkName` ON `network_sessions` (`networkName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_sessions_interfaceName` ON `network_sessions` (`interfaceName`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `network_devices` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `ipAddress` TEXT NOT NULL,
            `ipv6` TEXT NOT NULL,
            `macAddress` TEXT NOT NULL,
            `hostname` TEXT NOT NULL,
            `vendor` TEXT NOT NULL,
            `deviceType` TEXT NOT NULL,
            `firstSeen` INTEGER NOT NULL,
            `lastSeen` INTEGER NOT NULL,
            `isActive` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_network_devices_deviceId` ON `network_devices` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_devices_ipAddress` ON `network_devices` (`ipAddress`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_devices_macAddress` ON `network_devices` (`macAddress`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_devices_hostname` ON `network_devices` (`hostname`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_devices_lastSeen` ON `network_devices` (`lastSeen`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `device_session_history` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `ipAddress` TEXT NOT NULL,
            `firstSeen` INTEGER NOT NULL,
            `lastSeen` INTEGER NOT NULL,
            `packets` INTEGER NOT NULL,
            `bytes` INTEGER NOT NULL,
            `upload` INTEGER NOT NULL,
            `download` INTEGER NOT NULL,
            `activeConnections` INTEGER NOT NULL,
            `protocols` TEXT NOT NULL,
            `ports` TEXT NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_session_history_sessionId` ON `device_session_history` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_session_history_deviceId` ON `device_session_history` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_device_session_history_sessionId_deviceId` ON `device_session_history` (`sessionId`, `deviceId`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `traffic_statistics` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `device` TEXT NOT NULL,
            `protocol` TEXT NOT NULL,
            `bytes` INTEGER NOT NULL,
            `packets` INTEGER NOT NULL,
            `upload` INTEGER NOT NULL,
            `download` INTEGER NOT NULL,
            `connections` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_traffic_statistics_sessionId` ON `traffic_statistics` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_traffic_statistics_timestamp` ON `traffic_statistics` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_traffic_statistics_protocol` ON `traffic_statistics` (`protocol`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `service_observations` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `serviceName` TEXT NOT NULL,
            `domain` TEXT NOT NULL,
            `destinationIp` TEXT NOT NULL,
            `protocol` TEXT NOT NULL,
            `port` INTEGER NOT NULL,
            `trafficBytes` INTEGER NOT NULL,
            `classification` TEXT NOT NULL,
            `confidence` TEXT NOT NULL,
            `evidence` TEXT NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_observations_sessionId` ON `service_observations` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_observations_deviceId` ON `service_observations` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_observations_serviceName` ON `service_observations` (`serviceName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_observations_classification` ON `service_observations` (`classification`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_observations_timestamp` ON `service_observations` (`timestamp`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `dns_history` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `dnsServer` TEXT NOT NULL,
            `domain` TEXT NOT NULL,
            `queryType` TEXT NOT NULL,
            `response` TEXT NOT NULL,
            `responseStatus` TEXT NOT NULL,
            `responseTimeMs` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dns_history_sessionId` ON `dns_history` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dns_history_deviceId` ON `dns_history` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dns_history_domain` ON `dns_history` (`domain`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dns_history_timestamp` ON `dns_history` (`timestamp`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `connection_history` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `sourceIp` TEXT NOT NULL,
            `destinationIp` TEXT NOT NULL,
            `sourcePort` INTEGER NOT NULL,
            `destinationPort` INTEGER NOT NULL,
            `protocol` TEXT NOT NULL,
            `bytes` INTEGER NOT NULL,
            `packets` INTEGER NOT NULL,
            `duration` REAL NOT NULL,
            `status` TEXT NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_sessionId` ON `connection_history` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_deviceId` ON `connection_history` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_timestamp` ON `connection_history` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_sourceIp` ON `connection_history` (`sourceIp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_destinationIp` ON `connection_history` (`destinationIp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_protocol` ON `connection_history` (`protocol`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_connection_history_destinationPort` ON `connection_history` (`destinationPort`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `security_events` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `eventId` TEXT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `deviceId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `severity` TEXT NOT NULL,
            `eventType` TEXT NOT NULL,
            `source` TEXT NOT NULL,
            `destination` TEXT NOT NULL,
            `protocol` TEXT NOT NULL,
            `port` INTEGER NOT NULL,
            `evidence` TEXT NOT NULL,
            `confidence` TEXT NOT NULL,
            `description` TEXT NOT NULL,
            `status` TEXT NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_sessionId` ON `security_events` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_deviceId` ON `security_events` (`deviceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_severity` ON `security_events` (`severity`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_eventType` ON `security_events` (`eventType`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_security_events_timestamp` ON `security_events` (`timestamp`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `network_health_history` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `sessionId` TEXT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `latency` REAL NOT NULL,
            `packetLoss` REAL NOT NULL,
            `dnsLatency` REAL NOT NULL,
            `throughput` REAL NOT NULL,
            `retransmissions` INTEGER NOT NULL,
            `connectionFailures` INTEGER NOT NULL,
            `interfaceErrors` INTEGER NOT NULL,
            `healthScore` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_health_history_sessionId` ON `network_health_history` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_network_health_history_timestamp` ON `network_health_history` (`timestamp`)")

        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `data_retention_settings` (
            `id` INTEGER NOT NULL,
            `rawPacketsRetentionHours` INTEGER NOT NULL,
            `trafficStatsRetentionDays` INTEGER NOT NULL,
            `securityEventsRetentionDays` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          DATABASE_NAME
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

