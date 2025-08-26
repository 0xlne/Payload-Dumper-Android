package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.rajmani7584.payloaddumper.R
import com.rajmani7584.payloaddumper.models.DataViewModel
import com.rajmani7584.payloaddumper.models.Partition
import com.rajmani7584.payloaddumper.models.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun InfoScreen(
    dataModel: DataViewModel,
    partition: Partition,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val partitions = dataModel.completedPayload.collectAsState()
    val status = partitions.value[partition.name]
    val clipManager = LocalClipboardManager.current

    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(26.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.partition_details_header), fontWeight = FontWeight.W600, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.partition_details_name, partition.name), fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(
                        R.string.partition_details_size,
                        Utils.parseSize(partition.size)
                    ))
                Spacer(Modifier.height(12.dp))
                Row {
                    Text(stringResource(R.string.partition_details_hash))
                    Box(
                        modifier = Modifier.background(
                            Color(0xFF101010),
                            RoundedCornerShape(6.dp)
                        )
                    ) {
                        var copied by remember { mutableStateOf(false) }

                        if (copied) Text(
                            stringResource(R.string.partition_details_hash_copied), fontSize = 12.sp, modifier = Modifier
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
                    status?.message ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}