package com.rajmani7584.payloaddumper

import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.Utils.MyButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun SelectScreen(
    navController: NavHostController,
    dataModel: DataViewModel
) {
    val currentPath by dataModel.lastDirectory
    val isDarkTheme by dataModel.isDarkTheme.collectAsState()
    val externalStoragePath = Environment.getExternalStorageDirectory().absolutePath

    val isDir =
        navController.currentBackStackEntry?.arguments?.getBoolean("selectDirectory") == true

    Column {
        var list by remember { mutableStateOf<List<String>>(emptyList()) }
        var canGoBack by remember { mutableStateOf(false) }
        var canWrite by remember { mutableStateOf(false) }
        var invalidPath by remember { mutableStateOf(false) }
        val scroll = rememberScrollState(0)


        val fileModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)

        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "",
                Modifier.clickable(onClick = {
                    navController.popBackStack()
                }),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "SELECT ${if (isDir) "DIRECTORY" else "PAYLOAD"}",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily(Font(R.font.doto)),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        val headerHeightPx = with(LocalDensity.current) { 45.dp.toPx() }
        val minHeightPx = with(LocalDensity.current) { 0.dp.toPx() }
        val headerHeight = remember { mutableFloatStateOf(headerHeightPx) }

        val scrollBehavior = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newHeight = headerHeight.floatValue + delta
                    headerHeight.floatValue = newHeight.coerceIn(minHeightPx, headerHeightPx)
                    return Offset(0f, if (newHeight in minHeightPx..headerHeightPx) delta else 0f)
                }
            }
        }

        Column(Modifier.fillMaxWidth().nestedScroll(scrollBehavior)) {
            Row(
                Modifier.horizontalScroll(scroll)
                    .height(with(LocalDensity.current) { headerHeight.floatValue.toDp() })
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val directories =
                    currentPath.replace(externalStoragePath, "Internal Storage").split("/")
                for (index in 0 until directories.size) {

                    if (index != 0)
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "",
                            modifier = Modifier.fillMaxHeight()
                        )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxHeight()
                            .clickable(enabled = index != directories.size - 1, onClick = {
                                dataModel.setLastDirectory(
                                    directories.subList(0, index + 1).joinToString("/")
                                        .replace("Internal Storage", externalStoragePath)
                                )
                            })
                    ) {
                        Text(
                            directories[index],
                            fontStyle = FontStyle.Italic,
                            maxLines = 1
                        )
                    }
                }
            }

            Box(Modifier.fillMaxSize()) {
                if (isDir && canWrite) {
                    MyButton(
                        onClick = {
                            dataModel.setOutputDirectory(currentPath)
                            navController.popBackStack()
                        },
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.padding(bottom = 15.dp).zIndex(1f)
                            .align(Alignment.BottomCenter)
                            .width(navController.context.resources.displayMetrics.widthPixels.dp / 4)
                            .height(55.dp)
                    ) {
                        Text(
                            "Extract Here",
                            modifier = Modifier.padding(6.dp),
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
                Box(Modifier.padding(horizontal = 6.dp).background(MaterialTheme.colorScheme.surface)) {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 6.dp, bottom = 25.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (list.isEmpty()) {
                            item {
                                if (invalidPath) Text("invalid path", color = Color.Red)
                                else Text(
                                    if (File(currentPath).canRead()) "No files" else "No permission"
                                )
                            }
                        } else {
                            items(list.size) {
                                val file = list[it]
                                if (isDir) {
                                    if (!file.startsWith("fl:")) {
                                        Box(modifier = fileModifier.clickable(true, onClick = {
                                            dataModel.setLastDirectory("$currentPath/$file")
                                        })) {
                                            FolderButton(
                                                file,
                                                Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    if (file.startsWith("fl:")) {
                                        Box (fileModifier.clickable(true, onClick = {
                                            dataModel.initPayload(
                                                "$currentPath/${
                                                    file.removePrefix(
                                                        "fl:"
                                                    )
                                                }"
                                            )
                                            navController.popBackStack()
                                        })) {
                                            FileButton(
                                                file,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    } else {
                                        Box (fileModifier.clickable {
                                            dataModel.setLastDirectory("${currentPath}/${file}")
                                        }) {
                                            FolderButton(file, Modifier.padding(vertical = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(currentPath) {
            scroll.animateScrollTo(scroll.maxValue)
            val file = File(currentPath)
            invalidPath = !file.exists()
            canWrite = file.canWrite()
            list = emptyList()
            if (invalidPath) return@LaunchedEffect
            canGoBack = file.parentFile?.canWrite() == true

            withContext(Dispatchers.IO) {
                val allFiles = file.listFiles()
                if (allFiles != null) {
                    var folders = listOf<String>()
                    var files = listOf<String>()

                    allFiles.forEach { file ->
                        if (file.isDirectory) {
                            folders += file.name
                        } else if (!isDir && file.extension == "bin" || file.extension == "zip") {
                            files += "fl:${file.name}"
                        }
                    }
                    list += folders + files
                    delay(20)
                }
            }
        }

        BackHandler(canGoBack, onBack = {
            dataModel.setLastDirectory(File(currentPath).parentFile?.absolutePath.toString())
        })
    }
}

@Composable
fun FileButton(
    file: String,
    modifier: Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_file_present_24),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(file.removePrefix("fl:"), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
fun FolderButton(
    file: String,
    modifier: Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_folder_24),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(file, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
    }
}