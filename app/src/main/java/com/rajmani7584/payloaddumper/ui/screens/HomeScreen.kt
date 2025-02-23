package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rajmani7584.payloaddumper.MainActivity
import com.rajmani7584.payloaddumper.MainScreen
import com.rajmani7584.payloaddumper.R
import com.rajmani7584.payloaddumper.models.DataViewModel
import com.rajmani7584.payloaddumper.models.PartitionState
import com.rajmani7584.payloaddumper.ui.customviews.LoadingIndicator

@Composable
fun HomeScreen(
    mainActivity: MainActivity,
    dataModel: DataViewModel,
    navController: NavHostController,
    homeNavController: NavHostController
) {
    val hasPermission by dataModel.hasPermission
    Box(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        if (hasPermission == true) {
            NavHost(homeNavController, HomeScreens.HOME, Modifier.fillMaxSize()) {
                composable(HomeScreens.HOME, enterTransition = {
                    scaleIn()
                }, exitTransition = {
                    scaleOut()
                }) {
                    HomeLayout(dataModel, navController, homeNavController)
                }
                composable(HomeScreens.EXTRACT, enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        tween(120)
                    )
                }, exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(120)
                    )
                }) {
                    ExtractScreen(dataModel, navController, homeNavController)
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = {
                    dataModel.requestPermission(mainActivity)
                }, colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .2f, red = .6f, green = .62f, blue = .63f))) {
                    Text("Allow file access", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
@Composable
fun HomeLayout(
    dataModel: DataViewModel,
    navController: NavHostController,
    homeNavController: NavHostController
) {
    val payload by dataModel.payload
    val payloadPath by dataModel.payloadPath
    val payloadError by dataModel.payloadError
    val isLoading by dataModel.isLoading
    val completedPartition by dataModel.completedPayload.collectAsState()
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "PAYLOAD DUMPER",
            fontFamily = FontFamily(Font(R.font.doto)),
            style = MaterialTheme.typography.headlineMedium
        )
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(ImageVector.vectorResource(R.drawable.rounded_add_circle_24),
                contentDescription = "Select Payload",
                modifier = Modifier
                    .size(
                        minOf(
                            LocalConfiguration.current.screenWidthDp,
                            LocalConfiguration.current.screenHeightDp
                        ).dp / 2f
                    )
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("${MainScreen.SELECTOR}/false") {
                            popUpTo(MainScreen.MainUI) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                LoadingIndicator()
            } else {
                if (payload != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.6f)
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(6.dp))
                            .clickable {
                                homeNavController.navigate(HomeScreens.EXTRACT)
                            }
                            .padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${payload?.name}")

                        Spacer(Modifier.height(6.dp))
                        val sel = completedPartition.filter { it.value.statusCode != PartitionState.EXTRACTED && it.value.statusCode != PartitionState.FAILED }.size
                        val total = completedPartition.size
                        if (total > 0) LinearProgressIndicator((total - sel).toFloat() / total, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                    }
                } else if (payloadError != null) {
                    Text(
                        "$payloadPath\n${payloadError ?: ""}",
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                } else {
                    Text(
                        "Select a payload\nYou can select payload.bin/OTA.zip",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
class HomeScreens {
    companion object {
        const val HOME = "home"
        const val EXTRACT = "extract"
    }
}