package com.rajmani7584.payloaddumper

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
    val listView by dataViewModel.listView.collectAsState()
    val isExtracting by dataViewModel.isExtracting
    val payloadError by remember { dataViewModel.payloadError }

    // Root container with max size
    Column(Modifier.fillMaxSize()) {

        // Action Bar (top section)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 8.dp)
        ) {
            Spacer(Modifier.weight(1f)) // Spacer to push elements to the right

            // Dropdown menu and list view toggle if payload is available
            if (payload != null) {
                var showOpts by remember { mutableStateOf(false) }

                // Options Menu (Select All, Deselect All, Invert Selections)
                Box {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.rounded_checklist_rtl_24),
                        contentDescription = "",
                        Modifier.clickable (!isExtracting) { showOpts = !showOpts }
                    )
                    DropdownMenu(
                        expanded = showOpts,
                        onDismissRequest = { showOpts = false },
                        modifier = Modifier
                            .padding(4.dp)
                            .wrapContentWidth()
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select All", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                dataViewModel.selectAll(true)
                                showOpts = false
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        DropdownMenuItem(
                            text = { Text("Deselect All", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                dataViewModel.selectAll(false)
                                showOpts = false
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        DropdownMenuItem(
                            text = { Text("Invert Selections", style = MaterialTheme.typography.bodyLarge) },
                            onClick = {
                                dataViewModel.invertSelection()
                                showOpts = false
                            }
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Toggle between grid and list view
                Icon(
                    imageVector = if (listView)
                        ImageVector.vectorResource(R.drawable.rounded_grid_view_24)
                    else
                        Icons.AutoMirrored.Default.List,
                    contentDescription = "List View",
                    Modifier.clickable { dataViewModel.setListView(!listView) }
                )

                Spacer(Modifier.width(12.dp))
            }

            // Settings Icon
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                Modifier.clickable { navController.navigate(Screens.SETTING) }
            )
        }

        // Main Content Area
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .animateContentSize()
                .let { if (payload == null) it.weight(1f) else it },
            horizontalAlignment = if (payload == null) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            // Clickable row for navigating to the selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    navController.navigate("${Screens.SELECTOR}/false") {
                        popUpTo(Screens.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                if (payload == null) {
                    // When no payload is selected, show a "Select Payload" message and icon
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.rounded_add_circle_24),
                            contentDescription = "",
                            modifier = Modifier.size(
                                minOf(
                                    navController.context.resources.displayMetrics.heightPixels,
                                    navController.context.resources.displayMetrics.widthPixels
                                ).dp / 7
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
                    }
                } else {
                    // Display the selected payload name
                    Text("Payload: ", fontWeight = FontWeight.W600, fontSize = 20.sp)
                    Text(
                        payload.name,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Spacer(Modifier.height(24.dp))
                LoadingIndicator()
            }

            // Error message if any
            payloadError?.let {
                Spacer(Modifier.height(24.dp))
                Text(it, color = Color.Red, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
            }
        }

        // If a payload is available, display the extraction screen
        if (payload != null) {
            ExtractScreen(navController, dataViewModel)
        }
    }
}
