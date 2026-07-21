package com.example.sportcheckin.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sportcheckin.data.SportRepository
import java.time.LocalDate

/**
 * 首页勾选状态按 yyyy-MM-dd 记录，新自然日天然为空；
 * 该任务负责跨天后清理过早历史，避免长期运行后数据库膨胀。
 */
class DailyResetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        SportRepository.getInstance(applicationContext)
            .pruneHistoryBefore(LocalDate.now().minusDays(370))
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "sport_check_in_daily_reset"
    }
}
