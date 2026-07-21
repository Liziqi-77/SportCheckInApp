package com.example.sportcheckin.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportcheckin.data.ExerciseProjectEntity
import com.example.sportcheckin.data.SportRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val projects: List<ExerciseProjectEntity> = emptyList(),
    val checkedProjectIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SportRepository.getInstance(application)
    private val today = MutableStateFlow(LocalDate.now())

    private val todayRecords = today.flatMapLatest { date ->
        repository.recordsForDate(date)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.projects,
        todayRecords
    ) { projects, records ->
        HomeUiState(
            today = today.value,
            projects = projects,
            checkedProjectIds = records.filter { it.completed }.map { it.projectId }.toSet(),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                val now = LocalDate.now()
                if (now != today.value) {
                    today.value = now
                }
            }
        }
    }

    fun addProject(name: String, targetMinutes: Int) {
        viewModelScope.launch {
            repository.addProject(name, targetMinutes)
        }
    }

    fun updateProject(project: ExerciseProjectEntity) {
        viewModelScope.launch {
            repository.updateProject(project)
        }
    }

    fun deleteProject(project: ExerciseProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
        }
    }

    fun toggle(project: ExerciseProjectEntity, checked: Boolean) {
        viewModelScope.launch {
            repository.toggleCheckIn(project, today.value, checked)
        }
    }
}
