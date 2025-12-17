package com.example.findu.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.findu.model.dao.FoundItemDao
import com.example.findu.model.dao.LostItemDao
import com.example.findu.model.dao.NotificationDao
import com.example.findu.model.dao.UserDao

@Database(
    entities = [FoundItem::class, LostItem::class, User::class, Notification::class], // Added Notification::class
    version = 3, // Increased version number
    exportSchema = false
)
@TypeConverters(MapTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foundItemDao(): FoundItemDao
    abstract fun lostItemDao(): LostItemDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao // Added NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "findu_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}