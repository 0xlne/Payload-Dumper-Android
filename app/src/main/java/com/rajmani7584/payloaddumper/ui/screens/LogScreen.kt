package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajmani7584.payloaddumper.R
import com.rajmani7584.payloaddumper.models.DataViewModel

@Composable
fun LogScreen(dataModel: DataViewModel) {
    val scrollState = rememberLazyListState()
    val isDarkTheme by dataModel.isDarkTheme.collectAsState()
    val logs = dataModel.logs
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "LOGS",
                fontFamily = FontFamily(Font(R.font.doto)),
                style = MaterialTheme.typography.headlineMedium
            )
            Column(Modifier.fillMaxSize()) {
                SelectionContainer {
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize().padding(12.dp).border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp)),
                        contentPadding = PaddingValues(
                            start = 10.dp,
                            end = 10.dp,
                            top = 10.dp,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(logs) { log ->
                            Row(
                                Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    log.timestamp,
                                    fontFamily = FontFamily.Monospace,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp,
                                    color = if (log.message.startsWith("Error")) Color.Red else if (log.message.startsWith(
                                            "Success"
                                        )
                                    ) Color.Green.copy(green = 0.8f) else Color(0xffaaaaff)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    log.message.removePrefix("Success:").removePrefix("Error:"),
                                    fontFamily = FontFamily.Monospace,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 12.sp,
                                    color = if (log.message.startsWith("Error")) Color.Red else if (log.message.startsWith(
                                            "Success"
                                        )
                                    ) Color.Green.copy(green = 0.8f) else Color.Blue.copy(blue = 0.6f, red = .2f, green = if (isDarkTheme) .6f else .2f)
                                )
                            }
                        }
                    }
                    LaunchedEffect(logs.size) {
                        scrollState.animateScrollToItem(logs.size)
                    }
                }
            }
        }
    }
}
data class LogEntry(val message: String, val timestamp: String)