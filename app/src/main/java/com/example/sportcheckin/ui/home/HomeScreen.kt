package com.example.sportcheckin.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportcheckin.R
import com.example.sportcheckin.data.ExerciseProjectEntity
import com.example.sportcheckin.ui.components.WheelTimePicker
import com.example.sportcheckin.ui.theme.BrandOrange
import com.example.sportcheckin.ui.theme.MintGreen
import com.example.sportcheckin.ui.theme.SoftGreen
import com.example.sportcheckin.ui.theme.TextSecondary
import com.example.sportcheckin.util.toDurationText
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ExerciseProjectEntity?>(null) }
    var pendingDeleteProject by remember { mutableStateOf<ExerciseProjectEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "今日运动打卡",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)) +
                        " · 长按卡片编辑，左滑删除；已完成自动沉底",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state.projects.isEmpty() && !state.isLoading) {
                EmptyHomeHint(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
            ) {
                items(state.projects, key = { it.id }) { project ->
                    ExerciseProjectCard(
                        project = project,
                        checked = state.checkedProjectIds.contains(project.id),
                        onCheckedChange = { checked -> viewModel.toggle(project, checked) },
                        onLongEdit = { editingProject = project },
                        onDeleteRequest = { pendingDeleteProject = project }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = BrandOrange,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_add_24), contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = "添加运动")
        }
    }

    if (showAddDialog) {
        ExerciseEditorDialog(
            title = "添加运动项目",
            confirmText = "创建",
            initialName = "",
            initialMinutes = 30,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, minutes ->
                viewModel.addProject(name, minutes)
                showAddDialog = false
            }
        )
    }

    editingProject?.let { project ->
        ExerciseEditorDialog(
            title = "编辑运动项目",
            confirmText = "保存",
            initialName = project.name,
            initialMinutes = project.targetMinutes,
            onDismiss = { editingProject = null },
            onConfirm = { name, minutes ->
                viewModel.updateProject(project.copy(name = name, targetMinutes = minutes))
                editingProject = null
            }
        )
    }

    pendingDeleteProject?.let { project ->
        AlertDialog(
            onDismissRequest = { pendingDeleteProject = null },
            title = { Text(text = "删除运动项目？") },
            text = {
                Text(text = "将删除“${project.name}”以及它的全部历史打卡记录，此操作不可恢复。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(project)
                        pendingDeleteProject = null
                    }
                ) {
                    Text(text = "确认删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteProject = null }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyHomeHint(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "还没有运动项目", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角“添加运动”，创建跑步、跳绳、力量训练等任意数量的项目。",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseProjectCard(
    project: ExerciseProjectEntity,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLongEdit: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "删除",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onCheckedChange(!checked) },
                    onLongClick = onLongEdit
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (checked) SoftGreen else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MintGreen,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = MaterialTheme.colorScheme.surface
                    )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "目标 ${project.targetMinutes.toDurationText()} / 天",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = if (checked) "已完成" else "待完成",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (checked) MintGreen else TextSecondary,
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ExerciseEditorDialog(
    title: String,
    confirmText: String,
    initialName: String,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (name: String, minutes: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var hour by remember { mutableIntStateOf((initialMinutes.coerceIn(1, 1440)) / 60) }
    var minute by remember { mutableIntStateOf((initialMinutes.coerceIn(1, 1440)) % 60) }
    val totalMinutes = hour * 60 + minute

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "运动名称") },
                    placeholder = { Text(text = "例如：跑步、跳绳") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "每日运动时间（滚轮选择）", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                WheelTimePicker(
                    initialHour = hour,
                    initialMinute = minute,
                    modifier = Modifier.fillMaxWidth()
                ) { newHour, newMinute ->
                    hour = newHour
                    minute = newMinute
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "合计：${totalMinutes.toDurationText()}（范围 1 ~ 1440 分钟）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && totalMinutes in 1..1440,
                onClick = { onConfirm(name, totalMinutes) }
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        }
    )
}
