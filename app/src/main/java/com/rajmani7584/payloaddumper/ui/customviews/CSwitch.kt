package com.rajmani7584.payloaddumper.ui.customviews

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CSwitch(enabled: Boolean = true, isDarkTheme: Boolean, isDynamicColor: Boolean = false, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = if (isDarkTheme) SwitchDefaults.colors().copy(checkedTrackColor = Color.White, checkedThumbColor = Color(0xFF1B1B1B), uncheckedThumbColor = Color(0xFF919191), uncheckedTrackColor = Color(0xFF353535)) else SwitchDefaults.colors().copy(checkedTrackColor = Color.Black, checkedThumbColor = Color(0xFFE2E2E2), uncheckedThumbColor = Color(0xFF777777), uncheckedTrackColor = Color(0xFFE2E2E2))
    Switch(
        enabled = enabled,
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = if (isDynamicColor) SwitchDefaults.colors() else colors)
}