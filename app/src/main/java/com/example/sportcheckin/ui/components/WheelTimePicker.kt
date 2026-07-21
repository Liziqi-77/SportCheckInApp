package com.example.sportcheckin.ui.components

import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * 仅允许滚轮选择，禁止手动输入：
 * descendantFocusability = FOCUS_BLOCK_DESCENDANTS 会拦截软键盘聚焦。
 */
@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    modifier: Modifier = Modifier,
    onValueChange: (hour: Int, minute: Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember { mutableIntStateOf(initialMinute.coerceIn(0, 59)) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = 0
                    maxValue = 23
                    value = hour
                    wrapSelectorWheel = true
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, newVal ->
                        hour = newVal
                        onValueChange(hour, minute)
                    }
                }
            },
            update = { picker ->
                if (picker.value != hour) picker.value = hour
            }
        )
        Text(text = "小时", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(20.dp))
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = 0
                    maxValue = 59
                    value = minute
                    wrapSelectorWheel = true
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, newVal ->
                        minute = newVal
                        onValueChange(hour, minute)
                    }
                }
            },
            update = { picker ->
                if (picker.value != minute) picker.value = minute
            }
        )
        Text(text = "分钟", style = MaterialTheme.typography.titleMedium)
    }
}
