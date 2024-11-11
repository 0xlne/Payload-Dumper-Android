package com.rajmani7584.payloaddumper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SelectPayload(
    currentPath: MutableState<String>,
    typeDir: MutableState<Boolean>,
    modifier: Modifier,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val fileModifier = Modifier
        .padding(7.dp)
        .height(45.dp)
        .fillMaxWidth()
        .background(Color(0x33aaaaaa), shape = RoundedCornerShape(12.dp))

    // Use SnapshotStateList for incremental loading
    val fileList = remember { mutableStateListOf<String>() }
    var canGoBack by remember { mutableStateOf(true) }

    LaunchedEffect(currentPath.value) {
        canGoBack = File(currentPath.value).parentFile?.canWrite() == true
        fileList.clear()

        withContext(Dispatchers.IO) {
            val allFiles = File(currentPath.value).listFiles()
            if (allFiles != null) {
                val folders = mutableListOf<String>()
                val files = mutableListOf<String>()

                allFiles.forEach { file ->
                    if (file.isDirectory) {
                        folders.add(file.name)
                    } else if (!typeDir.value && file.extension == "bin") {
                        files.add("fl:${file.name}")
                    }
                }
                fileList += folders + files
            }
        }
    }

    Dialog(onDismissRequest = { onDismiss() }) {
        Column(modifier) {
            Text(
                text = "Select ${if (typeDir.value) "Directory to Extract" else "Payload.bin"}:",
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
            )
            Text("> ${currentPath.value}", modifier = Modifier.padding(start = 15.dp))

            if (canGoBack) {
                Row(
                    modifier = fileModifier.clickable {
                        currentPath.value = File(currentPath.value).parent ?: currentPath.value
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Parent directory", fontSize = 18.sp, modifier = Modifier.padding(start = 15.dp))
                }
            }

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (fileList.isEmpty()) {
                    item { Text(if (File(currentPath.value).canRead()) "No files" else "No permission") }
                } else {
                    items(fileList.size) {
                        val file = fileList[it]
                        if (typeDir.value) {
                            FolderButton(file, currentPath, fileModifier)
                        } else {
                            if (file.startsWith("fl:")) {
                                FileButton(file, fileModifier.clickable(true, onClick = {
                                    onSelect("${currentPath.value}/${file.removePrefix("fl:")}")
                                }))
                            } else {
                                FolderButton(file, currentPath, fileModifier)
                            }
                        }
                    }
                }
            }

            if (typeDir.value) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .padding(10.dp)
                            .background(Color(0x336666ff), shape = RoundedCornerShape(6.dp))
                            .clickable {
                                onSelect(currentPath.value)
                            }
                    ) {
                        Text(if (File(currentPath.value).canWrite()) "Extract Here" else "No Write Access", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
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
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_file_present_24),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(file.removePrefix("fl:"), fontSize = 14.sp)
    }
}

@Composable
fun FolderButton(
    file: String,
    currentPath: MutableState<String>,
    modifier: Modifier
) {
    Row(
        modifier.clickable {
            currentPath.value = "${currentPath.value}/${file}"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_folder_24),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(file, fontSize = 14.sp)
    }
}
