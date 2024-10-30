package com.rajmani7584.payloaddumper

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajmani7584.payloaddumper.ui.theme.PayloadDumperAndroidTheme
import java.io.File
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private external fun getPartitionList(path: String): String
    private external fun extractPartition(path: String, partition: String, outputPath: String): String
    val externalStoragePath = Environment.getExternalStorageDirectory().absolutePath
    var requestCounter by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appDirectory = File("$externalStoragePath/PayloadDumperAndroid")
        if (!appDirectory.exists()) appDirectory.mkdir()
        if (!appDirectory.isDirectory) {
            appDirectory.deleteRecursively()
            appDirectory.mkdir()
        }
        setContent {

            var hasPermission by remember { mutableStateOf(hasPermission()) }

            LaunchedEffect(requestCounter) {
                hasPermission = hasPermission()
            }

            PayloadDumperAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column (Modifier.padding(innerPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        if (hasPermission) AppLayout()
                        else Button(onClick = {
                            requestPermission()
                        }) {
                            Text("Allow file access")
                        }
                    }
                }
            }
        }

    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        } else {
            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = android.net.Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } else requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    @Composable
    fun AppLayout() {

        val selectingPayload = remember { mutableStateOf(false) }
        var partitionsList by remember { mutableStateOf(emptyList<Pair<String, Float>>()) }
        val typeDir = remember { mutableStateOf(false) }
        val fileToExtract = remember { mutableStateOf("") }
        val outputDirectory = remember { mutableStateOf("$externalStoragePath/PayloadDumperAndroid") }
        var listOutput by remember { mutableStateOf("") }

        LaunchedEffect(fileToExtract.value) {
            if (fileToExtract.value.isEmpty()) return@LaunchedEffect
            val output = getPartitionList(fileToExtract.value)
            if (output.startsWith("Err:")) {
                listOutput = output
                return@LaunchedEffect
            }

            // Parsing "partitionName|sizeInMB" format
            partitionsList = output.removePrefix("Partitions:").trim().split(",").mapNotNull { partition ->
                val parts = partition.split("|")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val size = parts[1].toFloatOrNull()?.div(1000) ?: 0f
                    name to size
                } else null
            }
        }

        Column(
            Modifier
                .padding(14.dp)
                .fillMaxSize()
        ) {
            Row {
                Box(Modifier.width(0.dp).weight(1f)) {
                    Text(if (fileToExtract.value.isEmpty()) "Select payload to extract" else fileToExtract.value, fontSize = 16.sp)
                }
                Button(onClick = {
                    selectingPayload.value = true
                    typeDir.value = false
                }) { Text("Select Payload") }
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Box(Modifier.width(0.dp).weight(1f)) {
                    Text(outputDirectory.value, fontSize = 16.sp)
                }
                Button(onClick = {
                    selectingPayload.value = true
                    typeDir.value = true
                }) { Text("Select Directory") }
            }

            if (listOutput.startsWith("Err:")) {
                Text(listOutput)
            }
            else {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(Color(0x34898989), shape = RoundedCornerShape(12.dp))
                ) {
                    items(partitionsList.size) { index ->
                        val (partitionName, partitionSize) = partitionsList[index]
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp, start = 14.dp, end = 14.dp).background(Color(
                            0x56C0DDF3
                        ), shape = RoundedCornerShape(12.dp))) {
                            var status by remember { mutableStateOf("status") }
                            Text(partitionName, fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(status)
                            Spacer(Modifier.weight(1f))
                            Text("$partitionSize KB", fontSize = 22.sp)
                            Spacer(Modifier.width(8.dp))
                            Button(onClick = {
                                thread {
                                    status = "Extracting..."
                                    val res = extractPartition(
                                        fileToExtract.value,
                                        partitionName,
                                        "${outputDirectory.value}/$partitionName.img"
                                    )
                                    status = res
                                }
                            }) {
                                Text("Extract")
                            }
                        }
                    }
                }
            }

            val dirPath = externalStoragePath
            val currentPath = remember { mutableStateOf(dirPath) }
            if (selectingPayload.value)
                SelectPayload(
                    typeDir, currentPath, dirPath,
                    outputDirectory,
                    fileToExtract,
                    selectingPayload,
                    Modifier
                        .height((resources.displayMetrics.heightPixels / 2).dp)
                        .width((resources.displayMetrics.widthPixels / 2).dp)
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
        }
    }

    companion object {
        init {
            System.loadLibrary("payload-dumper-rust")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requestCounter++
    }

    override fun onResume() {
        super.onResume()
        requestCounter++
    }
}