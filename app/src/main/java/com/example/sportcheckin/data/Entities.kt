package com.example.sportcheckin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_projects")
data class ExerciseProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetMinutes: Int,
    val createdAt: Long = System.currentTimeMillis(),
    /** 项目生效日期 yyyy-MM-dd；历史日期早于该值时不计入统计与日历完成度。 */
    @ColumnInfo(defaultValue = "")
    val startDate: String = "",
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
