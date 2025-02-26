package com.rajmani7584.payloaddumper.ui.customviews

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.rajmani7584.payloaddumper.R

@Composable
fun NButton(isActive: Boolean = false, onClick: () -> Unit, page: Int = 0) {
    Button (onClick = onClick, modifier = Modifier.padding(ButtonDefaults.ButtonWithIconContentPadding),
        colors = ButtonDefaults.filledTonalButtonColors().copy(containerColor = ButtonDefaults.filledTonalButtonColors().containerColor.copy(alpha = 0f))
    ) {
        Icon(
            when (page) {
                0 -> if (isActive) Icons.Filled.Home else Icons.Outlined.Home
                1 -> if (isActive) ImageVector.vectorResource(R.drawable.twotone_terminal_24) else ImageVector.vectorResource(R.drawable.round_terminal_24)
                2 -> if (isActive) Icons.Filled.Settings else Icons.Outlined.Settings
                else -> Icons.TwoTone.Close
            },
            when (page) {
                0 -> "Home"
                1 -> "Logs"
                2 -> "Settings"
                else -> "Unknown"
            },
            tint = if (isActive) Color.Red else ButtonDefaults.filledTonalButtonColors().contentColor
        )
    }
}

@Composable
fun MyButton(modifier: Modifier = Modifier, enabled: Boolean = true, isDarkTheme: Boolean = false, onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(enabled = enabled, modifier = modifier, onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isDarkTheme) Color(0xFFD7D8D8) else Color(0xFF1E2225),
            contentColor = if (isDarkTheme) Color.Black else Color.White,
            disabledContainerColor = Color(0x88888888)
        )) {
        content()
    }
}