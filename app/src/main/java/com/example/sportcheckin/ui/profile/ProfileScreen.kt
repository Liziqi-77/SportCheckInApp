package com.example.sportcheckin.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportcheckin.reminder.ReminderPreferences
import com.example.sportcheckin.reminder.SystemSettingsNavigator
import com.example.sportcheckin.ui.theme.BrandOrange
import com.example.sportcheckin.ui.theme.MintGreen
import com.example.sportcheckin.ui.theme.TextSecondary

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()
    val notificationsEnabled = rememberNotificationEnabled()
    var darkModeEnabled by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = BrandOrange.copy(alpha = 0.16f),
                    modifier = Modifier.size(58.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏃", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(modifier = Modifier.size(14.dp))
                Column {
                    Text(text = "运动爱好者", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "个人资料编辑接口已预留",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        SettingSwitchCard(
            title = "每日打卡提醒",
            subtitle = "每天 ${ReminderPreferences.DEFAULT_HOUR}:00 发送本地通知；Android 13+ 需授权通知权限",
            checked = reminderEnabled,
            onCheckedChange = { checked ->
                val hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                viewModel.onReminderSwitchChanged(
                    enabled = checked,
                    hasNotificationPermission = hasNotificationPermission
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        ReminderTroubleshootCard(notificationsEnabled = notificationsEnabled)

        SettingSwitchCard(
            title = "深色模式",
            subtitle = "预留：当前版本跟随系统",
            checked = darkModeEnabled,
            onCheckedChange = { darkModeEnabled = it }
        )
        SettingActionCard(
            title = "数据备份与恢复",
            subtitle = "预留：可扩展为本地导出/WebDAV/云同步"
        )
        SettingActionCard(
            title = "成就勋章墙",
            subtitle = "预留：连续打卡、累计时长、月度目标等成就"
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun rememberNotificationEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(SystemSettingsNavigator.areNotificationsEnabled(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = SystemSettingsNavigator.areNotificationsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return enabled
}

@Composable
private fun ReminderTroubleshootCard(notificationsEnabled: Boolean) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(text = "提醒可用性检测", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (notificationsEnabled) "通知权限已开启，提醒可正常显示。" else "通知未开启：即使打开提醒，也可能无法弹出通知栏。",
                style = MaterialTheme.typography.bodyMedium,
                color = if (notificationsEnabled) MintGreen else MaterialTheme.colorScheme.error
            )
            Text(
                text = "国产 ROM 还可能限制自启动/后台运行；如提醒不稳定，请完成下面两项设置。",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!notificationsEnabled) {
                    TextButton(onClick = { SystemSettingsNavigator.openNotificationSettings(context) }) {
                        Text(text = "去开启通知")
                    }
                }
                TextButton(onClick = { SystemSettingsNavigator.openAutoStartSettings(context) }) {
                    Text(text = "自启动设置")
                }
                TextButton(onClick = { SystemSettingsNavigator.openBatteryOptimizationSettings(context) }) {
                    Text(text = "关闭电池优化")
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun SettingActionCard(
    title: String,
    subtitle: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
