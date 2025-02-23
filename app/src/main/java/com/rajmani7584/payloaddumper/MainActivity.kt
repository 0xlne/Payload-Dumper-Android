package com.rajmani7584.payloaddumper

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rajmani7584.payloaddumper.models.DataViewModel
import com.rajmani7584.payloaddumper.ui.customviews.LoadingIndicator
import com.rajmani7584.payloaddumper.ui.screens.MainUI
import com.rajmani7584.payloaddumper.ui.screens.RawData
import com.rajmani7584.payloaddumper.ui.screens.Selector
import com.rajmani7584.payloaddumper.ui.theme.PayloadDumperAndroidTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity: ComponentActivity() {
    private var requestCounter by mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(1, 0))
        super.onCreate(savedInstanceState)

        setContent {
            val dataModel: DataViewModel = viewModel()

            val view = LocalView.current
            val hasPermission by dataModel.hasPermission
            val isDarkTheme by dataModel.isDarkTheme.collectAsState()
            val isDynamicColor by dataModel.isDynamicColor.collectAsState()
            val isTrueBlack by dataModel.isTrueBlack.collectAsState()
            val darkTheme = isSystemInDarkTheme()

            LaunchedEffect (isDarkTheme) {
                WindowCompat.getInsetsController((view.context as Activity).window, view).isAppearanceLightStatusBars = !isDarkTheme
            }
            LaunchedEffect (isDynamicColor) {
                if (isDynamicColor) dataModel.setDarkTheme(darkTheme)
            }

            PayloadDumperAndroidTheme(isDarkTheme, isDynamicColor, isTrueBlack) {
                Scaffold { innerPadding ->
                    Column (Modifier.padding(innerPadding)) {
                        Box(Modifier.fillMaxSize()) {
                            if (hasPermission == null) Box(Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxSize()
                                .zIndex(1f)) { LoadingIndicator(Modifier.align(Alignment.Center)) }
                            App(dataModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun App(dataModel: DataViewModel) {
        Box(Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .zIndex(.5f)) {
            val navController = rememberNavController()
            val homeNavController = rememberNavController()
            NavHost(navController, MainScreen.MainUI, Modifier.fillMaxSize()) {
                composable(MainScreen.MainUI, enterTransition = {
                    scaleIn()
                }, exitTransition = {
                    scaleOut()
                }) {
                    MainUI(this@MainActivity, dataModel, navController, homeNavController)
                }
                composable ("${MainScreen.SELECTOR}/{directory}",
                    arguments = listOf(navArgument("directory") { type = NavType.BoolType }),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            tween(120)
                        )
                    }, exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            tween(120)
                        )
                    }) {
                    Selector(dataModel, navController, homeNavController)
                }
                composable (MainScreen.RAW, enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        tween(150)
                    )
                }, exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        tween(150)
                    )
                }) {
                    RawData(dataModel, navController)
                }
            }
        }

        LaunchedEffect (requestCounter) {
            delay(40)
            dataModel.setPermission(this@MainActivity)
            if (dataModel.hasPermission.value != true) dataModel.println("File Permission Denied")
            dataModel.println("App Initialized")
        }
    }

    override fun onResume() {
        super.onResume()
        requestCounter++
    }
}
class MainScreen {
    companion object {
        const val MainUI = "home"
        const val SELECTOR = "selector"
        const val RAW = "raw"
    }
}