package com.rajmani7584.payloaddumper

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val completed = dataViewModel.partitionStatus.value.filter { it.value.statusCode == 3 }.size

    val headerHeightPx = with(LocalDensity.current) { 105.dp.toPx() }
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
    // Action Bar (top section)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 12.dp, bottom = 4.dp, end = 16.dp)
    ) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Go Back", Modifier.clickable { navController.popBackStack() })
        Spacer(Modifier.weight(1f)) // Spacer to push elements to the right

        // Dropdown menu and list view toggle if payload is available
        if (payload != null) {
            var showOpts by remember { mutableStateOf(false) }

            // Options Menu (Select All, Deselect All, Invert Selections)
            Box {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.rounded_checklist_rtl_24),
                    contentDescription = "",
                    Modifier.clickable(!isExtracting) { showOpts = !showOpts }
                )
                DropdownMenu(
                    expanded = showOpts,
                    onDismissRequest = { showOpts = false },
                    modifier = Modifier
                        .padding(4.dp)
                        .wrapContentWidth()
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Select All",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            dataViewModel.selectAll(true)
                            showOpts = false
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Deselect All",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            dataViewModel.selectAll(false)
                            showOpts = false
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Invert Selections",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            dataViewModel.invertSelection()
                            showOpts = false
                        }
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Toggle between grid and list view
            Icon(
                imageVector = if (listView)
                    ImageVector.vectorResource(R.drawable.rounded_grid_view_24)
                else
                    Icons.AutoMirrored.Default.List,
                contentDescription = "List View",
                Modifier.clickable { dataViewModel.setListView(!listView) }
            )

            Spacer(Modifier.width(16.dp))
        }

        // Settings Icon
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = "Settings",
            Modifier.clickable { navController.navigate(Screens.SETTING) }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior)
            .padding(top = 4.dp)
    ) {

        Row (modifier = Modifier.padding(start = 16.dp).wrapContentSize(), verticalAlignment = Alignment.CenterVertically) {
            Text("Payload: ", style = MaterialTheme.typography.titleMedium)
            Text("${payload?.name}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.horizontalScroll(rememberScrollState()))
        }
        Column(
            Modifier
                .padding(start = 16.dp)
                .height(with(LocalDensity.current) { headerHeight.floatValue.toDp() })
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val directories =
                    outputDirectory.replace(dataViewModel.externalStorage, "Internal Storage")
                        .split("/")
                Text("Output: ", style = MaterialTheme.typography.titleMedium)
                Row (Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).weight(.9f), verticalAlignment = Alignment.CenterVertically) {
                        for (index in 0 until directories.size) {
                            if (index != 0)
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "",
                                    modifier = Modifier.size(20.dp)
                                )
                            Text(
                                directories[index],
                                fontStyle = FontStyle.Italic,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 4.dp).clickable {
                            navController.navigate("${Screens.SELECTOR}/true")
                        })
                }
            }
            Text("Version: ${payload?.version}", maxLines = 1, style = MaterialTheme.typography.bodyMedium)
            Text("Security Patch Level: ${payload?.securityPatch}", maxLines = 1, style = MaterialTheme.typography.bodyMedium)
            Text("Manifest Length: ${payload?.manifestLength}", maxLines = 1, style = MaterialTheme.typography.bodyMedium)
            Text("Signature Length: ${payload?.signatureLength}", maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
        if (isExtracting) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    "$completed/$operations",
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                @Suppress("DEPRECATION")
                LinearProgressIndicator(
                    completed.toFloat().div(operations),
                    modifier = Modifier
                        .height(4.dp)
                        .fillMaxWidth(),
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
                    .background(if (isDynamicColor) MaterialTheme.colorScheme.surface else if (isDarkTheme) Color.Black else Color.White, RoundedCornerShape(8.dp))
            ) {

                if (operations > 0) {
                    MyButton(enabled = !isExtracting, isDarkTheme = isDarkTheme, modifier = Modifier
                        .fillMaxWidth(.4f).wrapContentSize(unbounded = true)
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter)
                        .zIndex(1f),
                        onClick = {
                            dataViewModel.extract()
                        }) {
                        Text(
                            "EXTRACT",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W800,
                            fontSize = 16.sp,
                            maxLines = 1,
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
                        columns = GridCells.Adaptive(150.dp)
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
            else -> when (it.statusCode) {
                1 -> if (isDarkTheme) Color(0xFF48474C) else Color(0xFFD1E3EE) // in progress
                3 -> if (isDarkTheme) Color(0xFF254125) else Color(0xFFD7FDD5) // completed
                4 -> MaterialTheme.colorScheme.errorContainer //failed
                else -> MaterialTheme.colorScheme.surface //default / verifying
            }
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth().wrapContentSize()
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    if (it.isSelected || it.statusCode != null) Color.Transparent else Color.Gray,
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
            if (it.statusCode.let { it == 1 || it == 2 }) {
                Box(Modifier.matchParentSize()) {
                    AnimatedProgressBarWithHighlight(it.progress.toFloat(), isDarkTheme)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (it.isSelected == true) Icons.Default.Check else if (it.statusCode == 4) Icons.Default.Clear else ImageVector.vectorResource(
                        R.drawable.baseline_disc_full_24
                    ),
                    contentDescription = "Drive",
                    Modifier.padding(4.dp)
                )
                Column(
                    Modifier
                        .padding(top = 6.dp, bottom = 12.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        partition.name,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Size: ${Utils.parseSize(partition.size)}",
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium
                    )
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
fun AnimatedProgressBarWithHighlight(
    progress: Float = 100f,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF254125) else Color(0xFFD7FDD5)
    val highlightColor = if (isDarkTheme) Color(0xFF406F40) else Color(0xFFACF5A2)

    val highlightWidthFraction = 0.2f // 20% of the progress bar width
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedOffset = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ), label = ""
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.div(100))
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.div(100))
                .clip(RoundedCornerShape(8.dp))
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val totalWidth = size.width
                val highlightWidth = totalWidth * highlightWidthFraction
                val highlightStart =
                    animatedOffset.value * (totalWidth + highlightWidth) - highlightWidth

                drawRect(
                    color = highlightColor.copy(.5f),
                    topLeft = Offset(highlightStart, 0f),
                    size = Size(highlightWidth, size.height)
                )
            }
        }
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