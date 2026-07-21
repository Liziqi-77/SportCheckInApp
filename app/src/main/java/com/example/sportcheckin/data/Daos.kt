package com.example.sportcheckin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseProjectDao {
    @Query("SELECT * FROM exercise_projects ORDER BY sortOrder ASC, createdAt ASC")
    fun observeProjects(): Flow<List<ExerciseProjectEntity>>

    @Query("SELECT * FROM exercise_projects WHERE id = :id")
    suspend fun getById(id: Long): ExerciseProjectEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM exercise_projects")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ExerciseProjectEntity): Long

    @Update
    suspend fun update(project: ExerciseProjectEntity)

    @Delete
    suspend fun delete(project: ExerciseProjectEntity)
}

@Dao
interface CheckInRecordDao {
    @Query("SELECT * FROM check_in_records WHERE date = :date")
    fun observeRecordsForDate(date: String): Flow<List<CheckInRecordEntity>>

    @Query("SELECT * FROM check_in_records WHERE date BETWEEN :startDate AND :endDate")
    fun observeRecordsBetween(startDate: String, endDate: String): Flow<List<CheckInRecordEntity>>

    @Query("SELECT * FROM check_in_records WHERE projectId = :projectId AND date = :date")
    suspend fun get(projectId: Long, date: String): CheckInRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: CheckInRecordEntity)

    @Query("DELETE FROM check_in_records WHERE projectId = :projectId AND date = :date")
    suspend fun delete(projectId: Long, date: String)

    @Query("DELETE FROM check_in_records WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: Long)

    @Query("DELETE FROM check_in_records WHERE date < :beforeDate")
    suspend fun deleteBefore(beforeDate: String)
}
