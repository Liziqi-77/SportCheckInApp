package com.example.sportcheckin.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_projects")
data class ExerciseProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetMinutes: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

@Entity(
    tableName = "check_in_records",
    primaryKeys = ["projectId", "date"],
    indices = [Index("date"), Index("projectId")]
)
data class CheckInRecordEntity(
    val projectId: Long,
    /** yyyy-MM-dd，与自然日绑定，跨天后自动进入新记录，无需手动清空首页勾选状态。 */
    val date: String,
    val completed: Boolean,
    val minutes: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
