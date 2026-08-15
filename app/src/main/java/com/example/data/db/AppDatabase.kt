package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.NotificationSettingEntity
import com.example.data.model.PacketEntity
import com.example.data.model.PcapFileEntity

@Database(
  entities = [PacketEntity::class, PcapFileEntity::class, NotificationSettingEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun packetDao(): PacketDao
  abstract fun pcapFileDao(): PcapFileDao
  abstract fun notificationSettingDao(): NotificationSettingDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "packet_capture_pro.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
