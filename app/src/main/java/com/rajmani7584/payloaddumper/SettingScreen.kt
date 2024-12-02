package com.rajmani7584.payloaddumper

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun SettingScreen(navController: NavHostController, dataViewModel: DataViewModel) {
    val concurrency by dataViewModel.concurrency.collectAsState()
    val isListView by dataViewModel.listView.collectAsState()
    val isDynamicColor by dataViewModel.isDynamicColor.collectAsState()
    val isDarkTheme by dataViewModel.isDarkTheme.collectAsState()
    val trueBlack by dataViewModel.trueBlack.collectAsState()

    val paddingHeight = 8.dp

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Go Back",
            modifier = Modifier.clickable(onClick = { navController.popBackStack() })
        )
        Column {
            Text(
                "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily(Font(R.font.doto)),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
            )
            Column (Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
                var showConcurrentOpts by remember { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { showConcurrentOpts = !showConcurrentOpts },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Concurrency")
                    Box(Modifier.wrapContentWidth().padding(vertical = paddingHeight)) {
                        Text(
                            "$concurrency",
                            Modifier.width(36.dp).border(1.dp, Color.LightGray, CircleShape)
                                .padding(start = 12.dp, end = 6.dp, bottom = 6.dp, top = 6.dp),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Justify
                        )
                        DropdownMenu(
                            showConcurrentOpts,
                            onDismissRequest = { showConcurrentOpts = false }
                        ) {
                            for (i in 1..8) {
                                DropdownMenuItem(text = {
                                    Text(
                                        "$i",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }, onClick = {
                                    dataViewModel.setConcurrency(i)
                                    showConcurrentOpts = false
                                })
                                if (i != 8) HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.2f
                                    ), modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }

                var showListOpts by remember { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { showListOpts = !showListOpts },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Default View")
                    Box(
                        modifier = Modifier.wrapContentWidth().padding(vertical = paddingHeight)
                    ) {
                        Text(
                            if (isListView) "List" else "Grid",
                            maxLines = 1,
                            modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )
                        DropdownMenu(
                            showListOpts,
                            onDismissRequest = { showListOpts = false },
                        ) {
                            DropdownMenuItem(text = {
                                Text(
                                    "List",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }, onClick = {
                                dataViewModel.setListView(true)
                                showListOpts = false
                            })
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            DropdownMenuItem(text = {
                                Text(
                                    "Grid",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }, onClick = {
                                dataViewModel.setListView(false)
                                showListOpts = false
                            })
                        }
                    }
                }

                var showThemeOpts by remember { mutableStateOf(false) }
                Row(
                    Modifier.fillMaxWidth()
                        .clickable { showThemeOpts = !showThemeOpts },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Column {
                        Text("Theme Style")
                        Text(
                            "System require android-12+",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Box(
                        modifier = Modifier.wrapContentWidth().padding(vertical = paddingHeight)
                    ) {
                        Text(
                            if (isDynamicColor) "System" else "App",
                            maxLines = 1,
                            modifier = Modifier.border(1.dp, Color.LightGray, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 16.sp
                        )
                        DropdownMenu(
                            showThemeOpts,
                            onDismissRequest = { showThemeOpts = false }
                        ) {
                            DropdownMenuItem(text = {
                                Text(
                                    "System",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }, onClick = {
                                dataViewModel.setDynamicColor(true)
                                showThemeOpts = false
                            })
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            DropdownMenuItem(text = {
                                Text(
                                    "App",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }, onClick = {
                                dataViewModel.setDynamicColor(false)
                                showThemeOpts = false
                            })
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth()
                        .clickable(!isDynamicColor) { dataViewModel.setDarkTheme(!isDarkTheme) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Theme", Modifier.padding(vertical = paddingHeight))
                    Switch(
                        enabled = !isDynamicColor,
                        checked = isDarkTheme,
                        onCheckedChange = { dataViewModel.setDarkTheme(!isDarkTheme) })
                }
                Row(
                    Modifier.fillMaxWidth()
                        .clickable(isDarkTheme) { dataViewModel.setTrueBlack(!trueBlack) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("True Black", Modifier.padding(vertical = paddingHeight))
                    Switch(
                        enabled = isDarkTheme,
                        checked = trueBlack,
                        onCheckedChange = { dataViewModel.setTrueBlack(!trueBlack) })
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f))
                Spacer(Modifier.height(12.dp))
                val ctx = LocalContext.current
                Column (modifier = Modifier.fillMaxWidth().clickable{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rajmani7584/Payload-Dumper-Android/releases/latest"))
                    ctx.startActivity(intent)
                }) {
                    var version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "Unable to query"
                    Text("Version: $version", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = paddingHeight))
                    Text("Check for Update", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp))
                }
                Spacer(Modifier.height(16.dp))
                Column (modifier = Modifier.fillMaxWidth().clickable{
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rajmani7584/Payload-Dumper-Android"))
                    ctx.startActivity(intent)
                }) {
                    Text("GitHub", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = paddingHeight))
                    Text("@Rajmani7584", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}