package com.example.sportcheckin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ExerciseProjectEntity::class, CheckInRecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseProjectDao(): ExerciseProjectDao
    abstract fun checkInRecordDao(): CheckInRecordDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercise_projects ADD COLUMN startDate TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "UPDATE exercise_projects SET startDate = date(createdAt / 1000, 'unixepoch', 'localtime') WHERE startDate = ''"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sport_check_in.db"
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
