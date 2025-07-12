package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.MainScreen
import com.rajmani7584.payloaddumper.R
import com.rajmani7584.payloaddumper.models.DataViewModel
import com.rajmani7584.payloaddumper.models.Partition
import com.rajmani7584.payloaddumper.models.PartitionState
import com.rajmani7584.payloaddumper.models.Utils
import com.rajmani7584.payloaddumper.ui.customviews.AnimatedProgressBar
import com.rajmani7584.payloaddumper.ui.customviews.MyButton

@Composable
fun ExtractScreen(
    dataModel: DataViewModel,
    navController: NavHostController,
    homeNavController: NavHostController
) {
    val payload by dataModel.payload
    val payloadRaw by dataModel.payloadRaw
    val listView by dataModel.isListView.collectAsState()
    val isDarkTheme by dataModel.isDarkTheme.collectAsState()
    val isExtracting by dataModel.isExtracting
    val isSelecting by dataModel.isSelecting
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Go Back",
                    Modifier.clickable { homeNavController.popBackStack() })
                Spacer(Modifier.weight(1f))

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
                                        stringResource(R.string.extract_screen_select_all),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                dataModel.selectAll(true)
                                    showOpts = false


                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.extract_screen_deselect_all),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                dataModel.selectAll(false)
                                    showOpts = false
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.extract_screen_invert_selections),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                dataModel.invertSelection()
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
                        Modifier.clickable { dataModel.setListView(!listView) }
                    )

                    payloadRaw?.let {
                        Spacer(Modifier.width(16.dp))

                        Icon(ImageVector.vectorResource(R.drawable.round_account_tree_24),
                            contentDescription = "Raw Data",
                            Modifier.clickable { navController.navigate(MainScreen.RAW)})
                    }
                }
            }
            ExtractLayout(dataModel, navController)
        }
        if (isSelecting) MyButton(enabled = !isExtracting, isDarkTheme = isDarkTheme, modifier = Modifier
            .wrapContentWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = if (LocalConfiguration.current.let { it.screenWidthDp > it.screenHeightDp }) 16.dp else 80.dp), onClick = { dataModel.extract() }) { Text("EXTRACT", fontFamily = FontFamily(Font(R.font.doto)), modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp)) }
    }
}

@Composable
fun ExtractLayout(dataModel: DataViewModel, navController: NavHostController) {
    val payload by dataModel.payload
    val isExtracting by dataModel.isExtracting
    val outputDirectory by dataModel.outputDirectory
    val listView by dataModel.isListView.collectAsState()
    val completedPartition by dataModel.completedPayload.collectAsState()

    val headerHeightPx = with(LocalDensity.current) { (MaterialTheme.typography.bodyMedium.fontSize.value.times(if (payload?.incremental == true) 6.5f else 5f)).dp.toPx() }
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
            .padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Payload: ", style = MaterialTheme.typography.titleSmall)
            Text(
                "${payload?.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }

        Column(
            Modifier
                .height(with(LocalDensity.current) { headerHeight.floatValue.toDp() })
                .padding(start = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val directories =
                    outputDirectory.replace(dataModel.externalStorage,
                        stringResource(R.string.extract_screen_internal_storage)
                    )
                        .split("/")
                Text(stringResource(R.string.extract_screen_info_out_dir), style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .weight(.9f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (index in directories.indices) {
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
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable(!isExtracting) {
                                navController.navigate("${MainScreen.SELECTOR}/true")
                            })
                }
            }
            Row {
                Text(
                    stringResource(R.string.extract_screen_info_version),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${payload?.version}",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                Text(
                    stringResource(R.string.security_patch_level),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${payload?.patch}",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            payload?.incremental?.let {
                if (it) Text(
                        "Incremental detected! (Not Supported!)",
                        maxLines = 1,
                        color = Color.Red,
                        style = MaterialTheme.typography.titleSmall
                    )
            }
        }
        Column (Modifier.fillMaxWidth()) {
            val sel = completedPartition.filter { it.value.statusCode != PartitionState.EXTRACTED && it.value.statusCode != PartitionState.FAILED }.size
            val total = completedPartition.size
            if (total != 0) {
                val prog = (total - sel).toFloat() / total
                Text(
                    "${total - sel}/$total",
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFACF5A2))) {
                    Box(Modifier
                        .fillMaxWidth(prog)
                        .fillMaxHeight()
                        .background(Color(0xFF377C33))
                        .clip(
                            RoundedCornerShape(4.dp)
                        ))
                }
            }
        }
        HorizontalDivider()
        if (payload == null) Text(stringResource(R.string.extract_screen_payload_error_check_log), color = Color.Red)

        payload?.let {

            if (listView) {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(
                        top = 15.dp,
                        bottom = 130.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(items = it.partitions, key = { partition -> partition.name }) { partition ->
                        PartitionCard(dataModel, partition, Modifier.fillMaxWidth())
                    }
                }
            } else {
                LazyVerticalGrid(
                    GridCells.Adaptive(it.largest.length.dp * MaterialTheme.typography.bodyMedium.fontSize.value),
                    contentPadding = PaddingValues(
                        top = 15.dp,
                        bottom = 130.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = it.partitions, key = { partition -> partition.name}) { partition ->
                        PartitionCard(dataModel, partition)
                    }
                }
            }
        }
    }
}

@Composable
fun PartitionCard(dataModel: DataViewModel, partition: Partition, modifier: Modifier = Modifier) {

    val isDarkTheme by dataModel.isDarkTheme.collectAsState()
    val selectedPartition by dataModel.selectedPartition
    val completedPartition by dataModel.completedPayload.collectAsState()
    val states = completedPartition[partition.name]

    var textColor = MaterialTheme.colorScheme.onBackground
    val color = if (selectedPartition.contains(partition.name)) {
        textColor = MaterialTheme.colorScheme.onPrimaryContainer
        MaterialTheme.colorScheme.primaryContainer
    } else if (completedPartition.contains(partition.name)) {
        textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        when(completedPartition[partition.name]?.statusCode) {
            PartitionState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
            PartitionState.EXTRACTED -> if (isDarkTheme) Color(0xFF244125) else Color(0xFF468D45)
            PartitionState.FAILED -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceContainer.copy(
                alpha = .2f,
                red = .6f,
                green = .62f,
                blue = .63f
            )
        }
    } else MaterialTheme.colorScheme.surfaceContainer.copy(
        alpha = .2f,
        red = .6f,
        green = .62f,
        blue = .63f
    )

    var showInfo by remember { mutableStateOf(false) }
    Box(modifier
        .width(200.dp)
        .background(
            color, RoundedCornerShape(8.dp)
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    val isSelecting by dataModel.isSelecting
                    val isExtracting by dataModel.isExtracting
                    val selectedPartitions by dataModel.selectedPartition
                    selectedPartitions.let {
                        if (isSelecting && !isExtracting) {
                            dataModel.selectPartition(partition, !it.contains(partition.name))
                            return@detectTapGestures
                        }
                        showInfo = !showInfo
                    }
                },
                onLongPress = {
                    val isSelecting by dataModel.isSelecting
                    val isExtracting by dataModel.isExtracting
                    val selectedPartitions by dataModel.selectedPartition
                    selectedPartitions.let {
                        if (!isSelecting && !isExtracting) {
                            dataModel.selectPartition(partition, !it.contains(partition.name))
                            return@detectTapGestures
                        }
                        showInfo = !showInfo
                    }
                }
            )
        }) {
        Row (Modifier
            .padding(8.dp)
            .zIndex(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (states?.statusCode == PartitionState.FAILED) Icons.Default.Clear else if (selectedPartition.contains(partition.name) || states?.statusCode == PartitionState.EXTRACTED) Icons.Default.Check else ImageVector.vectorResource(R.drawable.baseline_disc_full_24), contentDescription = "Disc", tint = textColor)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(partition.name, fontFamily = FontFamily.Monospace, color = textColor)
                Spacer(Modifier.height(4.dp))
                Text(Utils.parseSize(partition.size), color = textColor)
            }
        }
        completedPartition[partition.name]?.let {
            if (it.statusCode == PartitionState.EXTRACTING || it.statusCode == PartitionState.VERIFYING) {
                Box(Modifier
                    .matchParentSize()
                    .zIndex(.5f)) {
                    AnimatedProgressBar(progress = it.progress.toFloat(), backgroundColor = if (isDarkTheme) Color(0xFF244125) else Color(0xFF468D45), highlightColor = if (isDarkTheme) Color(0xFF325832) else Color(0xFFC0F9BB), modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
    if (showInfo) InfoScreen(
        dataModel,
        partition = partition,
        onDismiss = { showInfo = false }
    )
}