package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PacketEntity
import com.example.data.model.PcapFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PacketDao {
  @Query("SELECT * FROM packets ORDER BY id DESC")
  fun getAllPacketsFlow(): Flow<List<PacketEntity>>

  @Query("SELECT * FROM packets ORDER BY id DESC LIMIT :limit OFFSET :offset")
  suspend fun getPacketsPaged(limit: Int, offset: Int): List<PacketEntity>

  @Query("SELECT * FROM packets WHERE sessionId = :sessionId ORDER BY id DESC")
  fun getPacketsForSession(sessionId: String): Flow<List<PacketEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPacket(packet: PacketEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPackets(packets: List<PacketEntity>)

  @Query("DELETE FROM packets")
  suspend fun clearAllPackets()

  @Query("SELECT COUNT(*) FROM packets")
  suspend fun getPacketCount(): Int

  @Query("SELECT SUM(length) FROM packets")
  suspend fun getTotalBytes(): Long?
}

@Dao
interface PcapFileDao {
  @Query("SELECT * FROM pcap_files ORDER BY timestamp DESC")
  fun getAllPcapFiles(): Flow<List<PcapFileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPcapFile(pcapFile: PcapFileEntity): Long

  @Query("DELETE FROM pcap_files WHERE id = :id")
  suspend fun deletePcapFile(id: Long)
}

@Dao
interface NotificationSettingDao {
  @Query("SELECT * FROM notification_settings WHERE id = 1")
  fun getNotificationSettings(): Flow<NotificationSettingEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveNotificationSettings(settings: NotificationSettingEntity)
}
