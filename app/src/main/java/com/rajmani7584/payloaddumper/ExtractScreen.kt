package com.rajmani7584.payloaddumper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.Utils.MyButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExtractScreen(navController: NavHostController, dataViewModel: DataViewModel) {
    val payload = dataViewModel.payloadInfo.value
    val listView by dataViewModel.listView.collectAsState()
    val outputDirectory = dataViewModel.outputDirectory.value
    val isDynamicColor by dataViewModel.isDynamicColor.collectAsState()
    val isDarkTheme by dataViewModel.isDarkTheme.collectAsState()
    val isExtracting by dataViewModel.isExtracting
    val isSelecting by dataViewModel.isSelecting
    val operations = dataViewModel.partitionStatus.value.filter { it.value.isSelected }.size
    val completed = dataViewModel.partitionStatus.value.filter { it.value.isCompleted }.size

    val headerHeightPx = with(LocalDensity.current) { 130.dp.toPx() }
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

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior)
    ) {

        Column(
            Modifier
                .padding(start = 16.dp)
                .height(with(LocalDensity.current) { headerHeight.floatValue.toDp() })
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Output: ")
                val scroll = rememberScrollState(0)
                Text(outputDirectory, modifier = Modifier
                    .horizontalScroll(scroll)
                    .clickable(onClick = {
                        navController.navigate("${Screens.SELECTOR}/true") {
                            popUpTo(Screens.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }), textDecoration = TextDecoration.Underline)
                LaunchedEffect(outputDirectory) {
                    scroll.animateScrollTo(scroll.maxValue)
                }
            }
            Text("Version: ${payload?.version}")
            Text("Security Patch Level: ${payload?.securityPatch}")
            Text("Manifest Length: ${payload?.manifestLength}")
            Text("Signature Length: ${payload?.signatureLength}")
        }
        if (isExtracting) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text(
                    "$completed/$operations",
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    completed.toFloat().div(operations),
                    modifier = Modifier.height(4.dp).fillMaxWidth(),
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.LightGray,
                    color = Color(0xFF1D4CAD)
                )
            }
        }
        payload?.let { info ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .background(if (isDynamicColor) MaterialTheme.colorScheme.surface else if (isDarkTheme) Color.Black else Color.White)
            ) {

                if (isSelecting) {
                    MyButton(enabled = !isExtracting, isDarkTheme = isDarkTheme, modifier = Modifier
                        .width(navController.context.resources.displayMetrics.widthPixels.dp / 4.5f)
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter)
                        .zIndex(1f),
                        onClick = {
                            dataViewModel.extract()
                        }) {
                        Text(
                            "EXTRACT",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W800,
                            fontSize = 18.sp,
                            fontFamily = FontFamily(Font(R.font.doto))
                        )
                    }
                }

                if (listView) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 70.dp
                        )
                    ) {
                        items(info.partitions.size) { index ->
                            PartitionCard(dataViewModel, info.partitions[index])
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        contentPadding = PaddingValues(
                            top = 12.dp,
                            start = 8.dp,
                            end = 8.dp,
                            bottom = 70.dp
                        ),
                        columns = GridCells.Adaptive(navController.context.resources.displayMetrics.widthPixels.dp / 6)
                    ) {
                        items(info.partitions.size) { index ->
                            PartitionCard(dataViewModel, info.partitions[index])
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartitionCard(dataViewModel: DataViewModel, partition: Partition) {

    val partitionStatus = dataViewModel.partitionStatus.value[partition.name]
    val isDarkTheme by dataViewModel.isDarkTheme.collectAsState()
    val isExtracting by dataViewModel.isExtracting
    val isSelecting by dataViewModel.isSelecting
    var showInfo by remember { mutableStateOf(false) }

    partitionStatus?.let {

        val backgroundColor = when {
            it.isSelected && isSelecting -> if (isDarkTheme) Color(0xFF48474C) else Color(0xFFD1E3EE)
            it.hasFailed -> MaterialTheme.colorScheme.errorContainer
            it.isCompleted -> if (isDarkTheme) Color(0xFF254125) else Color(0xFFD7FDD5)
            else -> MaterialTheme.colorScheme.surface
        }

        Box (
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (it.isSelected || it.isCompleted || it.hasFailed) Color.Transparent else Color.Gray,
                    RoundedCornerShape(8.dp)
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val partitionStatus =
                                dataViewModel.partitionStatus.value[partition.name]
                            val isSelecting = dataViewModel.isSelecting.value
                            partitionStatus?.let {
                                if (isSelecting && !isExtracting) {
                                    dataViewModel.selectCard(partitionStatus, !it.isSelected)
                                    return@detectTapGestures
                                }
                                showInfo = !showInfo
                            }
                        },
                        onLongPress = {
                            val partitionStatus =
                                dataViewModel.partitionStatus.value[partition.name]
                            val isSelecting = dataViewModel.isSelecting.value
                            if (!isSelecting && !isExtracting) {
                                dataViewModel.setSelecting(true)
                                partitionStatus?.let {
                                    dataViewModel.selectCard(
                                        it,
                                        !it.isSelected
                                    )
                                }
                                return@detectTapGestures
                            }
                            showInfo = !showInfo
                        }
                    )
                }
        ) {
            Box(Modifier.matchParentSize()) { Box(Modifier.fillMaxHeight().fillMaxWidth(it.progress.toFloat().div(100)).background(if (isDarkTheme) Color(0xFF254125) else Color(0xFFD7FDD5), RoundedCornerShape(8.dp))) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (it.isSelected == true) Icons.Default.Check else ImageVector.vectorResource(
                        R.drawable.baseline_disc_full_24
                    ),
                    contentDescription = "Drive",
                    Modifier.padding(8.dp)
                )
                Column(Modifier.padding(start = 4.dp, top = 8.dp, bottom = 16.dp).fillMaxSize()) {
                    val status = when(it.statusCode) {
                        0 -> "Verifying..."
                        1 -> "Complete!"
                        2 -> "Failed!"
                        else -> ""
                    }
                    Row (horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(partition.name, fontFamily = FontFamily.Monospace)
                        Text(status, color = Color(0xFF121144), modifier = Modifier.padding(end = 6.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Size: ${Utils.parseSize(partition.size)}")
                }
            }
        }
        if (showInfo) InfoScreen(
            partition,
            it.message,
            modifier = Modifier.fillMaxWidth()
        ) { showInfo = false }
    }
}

@Composable
fun InfoScreen(
    partition: Partition,
    message: String,
    modifier: Modifier,
    onDismiss: () -> Unit
) {
    val clipManager = LocalClipboardManager.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(26.dp)) {
                Spacer(Modifier.height(12.dp))
                Text("Partition Details:", fontWeight = FontWeight.W600, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text("Name: ${partition.name}")
                Spacer(Modifier.height(6.dp))
                Text("Size: ${Utils.parseSize(partition.size)}")
                Spacer(Modifier.height(12.dp))
                Row {
                    Text("Hash: ")
                    Box(
                        modifier = Modifier.background(
                            Color(0xFF101010),
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        var copied by remember { mutableStateOf(false) }

                        if (copied) Text(
                            "Copied!", fontSize = 12.sp, modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 36.dp),
                            color = Color.White
                        )
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.rounded_content_copy_24),
                            contentDescription = "Copy",
                            Modifier
                                .padding(6.dp)
                                .align(Alignment.TopEnd)
                                .clickable(onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        clipManager.setText(AnnotatedString(partition.hash))
                                        copied = true
                                        delay(1200)
                                        copied = false
                                    }
                                }),
                            colorFilter = ColorFilter.lighting(Color.Black, Color.White)
                        )
                        Text(
                            partition.hash,
                            color = Color.White,
                            modifier = Modifier
                                .padding(top = 28.dp, start = 12.dp, end = 32.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}