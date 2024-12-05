package com.rajmani7584.payloaddumper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.ui.customviews.LoadingIndicator

@Composable
fun HomeScreen(navController: NavHostController, dataViewModel: DataViewModel) {
    // Collecting state and data from the ViewModel
    val payloadPath by remember { dataViewModel.payloadPath }
    val isLoading by dataViewModel.isLoading.collectAsState()
    val payload = dataViewModel.payloadInfo.value
    val isDarkTheme by dataViewModel.isDarkTheme.collectAsState()
    val payloadError by remember { dataViewModel.payloadError }
    val operations = dataViewModel.partitionStatus.value.filter { it.value.isSelected }.size
    val completed = dataViewModel.partitionStatus.value.filter { it.value.statusCode == 3 }.size

    Row (Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.Outlined.Settings,
            contentDescription = "Settings",
            modifier = Modifier.clickable {
                navController.navigate(
                    Screens.SETTING
                )
            })
    }
    // Root container with max size
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxHeight(.5f)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("${Screens.SELECTOR}/false") {
                            popUpTo(Screens.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_add_circle_24),
                    contentDescription = "",
                    modifier = Modifier.size(
                        minOf(
                            LocalConfiguration.current.screenHeightDp,
                            LocalConfiguration.current.screenWidthDp
                        ).dp / 4
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = payloadPath?.split("/")?.last()
                        ?: "Select a Payload\nYou can Select payload.bin/OTA.zip",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
                // Loading indicator
                if (isLoading) {
                    Spacer(Modifier.height(24.dp))
                    LoadingIndicator()
                }
                // Error message if any
                payloadError?.let {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        it,
                        color = Color.Red,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp
                    )
                }
            }
            payload?.let {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(.5f)
                        .padding(bottom = 4.dp)
                        .background(if (isDarkTheme) Color(0xFF48474C) else Color(0xFFD1E3EE), RoundedCornerShape(8.dp))
                        .clickable {
                            navController.navigate(Screens.EXTRACT)
                        }
                ) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        it.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(6.dp))
                    if (operations > 0)
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                "$completed/$operations",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            @Suppress("DEPRECATION")
                            LinearProgressIndicator(
                                progress = completed.toFloat() / operations,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = Color.Blue,
                                trackColor = Color.Gray,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                }
            }
        }
    }
}