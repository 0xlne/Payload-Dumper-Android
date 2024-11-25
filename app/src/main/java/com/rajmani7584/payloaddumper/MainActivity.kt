package com.rajmani7584.payloaddumper

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajmani7584.payloaddumper.ui.theme.PayloadDumperAndroidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    val externalStoragePath = Environment.getExternalStorageDirectory().absolutePath
    private var requestCounter by mutableIntStateOf(0)
    val utils = Utils(this)
    val payload = Payload()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var hasPermission by remember { mutableStateOf(false) }

            LaunchedEffect(requestCounter) {
                hasPermission = utils.hasPermission()
            }

            PayloadDumperAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Text("Incremental OTA not supported!", fontSize = 14.sp, modifier = Modifier.background(
                            MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).fillMaxWidth().padding(start = 10.dp, end = 10.dp), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp, bottom = 6.dp)
                                .background(
                                    color = if (isSystemInDarkTheme()) Color(0xFF283131) else Color(
                                        0xFFe0faff
                                    ), shape = RoundedCornerShape(12.dp)
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            if (hasPermission) AppLayout()
                            else Button(onClick = {
                                utils.requestPermission()
                            }) {
                                    Text("Allow file access")
                                }
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun AppLayout() {
        val payloadFile = remember { mutableStateOf("") }
        val outDir = remember { mutableStateOf(utils.outDir) }
        val currentPath = remember { mutableStateOf(externalStoragePath) }
        var typeDir by remember { mutableStateOf(false) }
        var showSelectPayload by remember { mutableStateOf(false) }
        var partitions by remember { mutableStateOf(listOf<Pair<String, Float>>()) }
        var error by remember { mutableStateOf("") }

        LaunchedEffect(payloadFile.value) {
            if (payloadFile.value.isEmpty()) return@LaunchedEffect
            partitions = emptyList()
            CoroutineScope(Dispatchers.IO).launch {
                payload.getPartitions(payloadFile.value).let {
                    if (it.isSuccess) {
                        partitions = it.getOrElse { emptyList() }
                        error = ""
                    } else {
                        partitions = emptyList()
                        error = it.exceptionOrNull()?.message ?: "Unknown Error"
                    }
                }
            }
        }

        Column (
            Modifier
                .fillMaxSize()
                .padding(8.dp)) {
            Column {
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Payload File: ", color = Color(0xffff66ff), fontSize = 14.sp)
                        Text(payloadFile.value.let { if (it.isEmpty()) "Select a Payload to Extract" else it })
                    }
                    Button(onClick = {
                        typeDir = false
                        showSelectPayload = true
                    }) {
                        Text("Select Payload")
                    }
                }
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Output Directory: ", color = Color(0xffff66ff), fontSize = 14.sp)
                        Text(outDir.value.let { if (it.isEmpty()) "Select an Output Directory" else it })
                    }
                    Button(onClick = {
                        typeDir = true
                        showSelectPayload = true
                    }) {
                        Text("Select Directory")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Column (
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState(0))) {
                if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 18.sp)
                if (partitions.isNotEmpty()) {
                    for (partition in partitions) {
                        Card (
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column (Modifier
                                .background(
                                    Color(if (isSystemInDarkTheme()) 0x23ba98bc else 0xffffffff),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)) {
                                PartitionCard(partition, payloadFile, outDir)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
        if (showSelectPayload) SelectPayload(
            currentPath,
            typeDir,
            Modifier
                .padding(top = 20.dp, bottom = 20.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
            onSelect = {
                showSelectPayload = false
                if (typeDir) {
                    outDir.value = it
                } else {
                    payloadFile.value = it
                }
        }, onDismiss = {
            showSelectPayload = false
        })
    }

    @Composable
    fun PartitionCard(partition: Pair<String, Float>, payloadFile: MutableState<String>, outDir: MutableState<String>) {

        var progress by remember { mutableLongStateOf(0) }
        var warning by remember { mutableStateOf("") }
        var inProgress by remember { mutableStateOf(false) }
        var newPartitionName by remember { mutableStateOf("") }

        LaunchedEffect(outDir.value, payloadFile.value, requestCounter) {
            if (outDir.value.isEmpty()) return@LaunchedEffect
            newPartitionName = utils.setupPartitionName(outDir.value, partition.first, 0)
            warning = ""
            progress = 0
            inProgress = false
            if (newPartitionName != partition.first) {
                warning = "${partition.first}.img already exists\nwill be renamed to $newPartitionName"
            }
        }

        Row {
            Column {
                Text(partition.first, fontSize = 18.sp)
                Text(utils.parseSize(partition.second), fontSize = 18.sp)
            }
            Spacer(Modifier.weight(1f))
            Button(enabled = !inProgress, onClick = {
                inProgress = true
                warning = ""
                progress = 0
                val outDirectory = utils.setupOutDir(outDir.value, 0)
                val output = "$outDirectory/$newPartitionName.img"
                CoroutineScope(Dispatchers.IO).launch {
                    val result = payload.extract(payloadFile.value, partition.first, output) {
                        progress = it
                    }
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) {
                            warning = "success$output"
                        } else {
                            inProgress = false
                            progress = 0
                            warning = result.exceptionOrNull()?.message ?: "Unknown Error"
                            this.cancel()
                            File(newPartitionName).apply { if (exists()) delete() }
                        }
                    }
                }
            }) { Text("Extract") }
        }
        Column {
            if (progress > 0f)
                Column {
                    Row {
                        Spacer(Modifier.weight(1f))
                        Text("$progress%", color = Color(0xffff66ff), fontSize = 14.sp)
                    }
                    @Suppress("DEPRECATION")
                    LinearProgressIndicator(
                        progress.toFloat() / 100,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp),
                        color = Color.Green,
                        trackColor = Color.Red,
                        strokeCap = StrokeCap.Round
                    )
                }
            if (warning.isNotEmpty()) Text(
                if (warning.startsWith("success"))
                    "Done: ${warning.removePrefix("success")}"
                    else warning,
                color = if (warning.startsWith("success")) Color(0xff003388) else MaterialTheme.colorScheme.error)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        requestCounter++
    }

    override fun onResume() {
        super.onResume()
        requestCounter++
    }
}