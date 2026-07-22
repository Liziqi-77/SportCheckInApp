package com.example.sportcheckin.data

import android.content.Context
import com.example.sportcheckin.util.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class SportRepository private constructor(context: Context) {
    private val projectDao = AppDatabase.getInstance(context).exerciseProjectDao()
    private val recordDao = AppDatabase.getInstance(context).checkInRecordDao()

    val projects: Flow<List<ExerciseProjectEntity>> = projectDao.observeProjects()

    fun recordsForDate(date: LocalDate): Flow<List<CheckInRecordEntity>> =
        recordDao.observeRecordsForDate(DateUtils.toKey(date))

    fun recordsBetween(start: LocalDate, end: LocalDate): Flow<List<CheckInRecordEntity>> =
        recordDao.observeRecordsBetween(DateUtils.toKey(start), DateUtils.toKey(end))

    suspend fun addProject(name: String, targetMinutes: Int) {
        projectDao.upsert(
            ExerciseProjectEntity(
                name = name.trim(),
                targetMinutes = targetMinutes.coerceIn(1, 24 * 60),
                sortOrder = projectDao.nextSortOrder()
            )
        )
    }

    suspend fun updateProject(project: ExerciseProjectEntity) {
        val safeProject = project.copy(
            name = project.name.trim(),
            targetMinutes = project.targetMinutes.coerceIn(1, 24 * 60)
        )
        projectDao.update(safeProject)
        val todayKey = DateUtils.toKey(LocalDate.now())
        recordDao.get(safeProject.id, todayKey)?.let { todayRecord ->
            if (todayRecord.completed) {
                recordDao.upsert(todayRecord.copy(minutes = safeProject.targetMinutes))
            }
        }
    }

    suspend fun deleteProject(project: ExerciseProjectEntity) {
        recordDao.deleteForProject(project.id)
        projectDao.delete(project)
    }

    suspend fun toggleCheckIn(project: ExerciseProjectEntity, date: LocalDate, checked: Boolean) {
        val key = DateUtils.toKey(date)
        if (checked) {
            recordDao.upsert(
                CheckInRecordEntity(
                    projectId = project.id,
                    date = key,
                    completed = true,
                    minutes = project.targetMinutes
                )
            )
        } else {
            recordDao.delete(project.id, key)
        }
    }

    suspend fun setCheckIn(
        projectId: Long,
        date: LocalDate,
        completed: Boolean,
        minutes: Int
    ) {
        val key = DateUtils.toKey(date)
        if (!completed) {
            recordDao.delete(projectId, key)
            return
        }
        val project = projectDao.getById(projectId) ?: return
        recordDao.upsert(
            CheckInRecordEntity(
                projectId = project.id,
                date = key,
                completed = true,
                minutes = minutes.coerceIn(1, 24 * 60)
            )
        )
    }

    suspend fun pruneHistoryBefore(date: LocalDate) {
        recordDao.deleteBefore(DateUtils.toKey(date))
    }

    companion object {
        @Volatile
        private var instance: SportRepository? = null

        fun getInstance(context: Context): SportRepository {
            return instance ?: synchronized(this) {
                instance ?: SportRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
