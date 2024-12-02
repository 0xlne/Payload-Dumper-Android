package com.rajmani7584.payloaddumper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rajmani7584.payloaddumper.ui.theme.PayloadDumperAndroidTheme

class MainActivity : ComponentActivity() {

    private var requestCounter by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val hasPermissionFunction = { Utils.hasPermission(this) }
        setContent {
            val dataViewModel: DataViewModel = viewModel()
            var hasPermission by remember { mutableStateOf(false) }
            val dynamicColor by dataViewModel.isDynamicColor.collectAsState()
            val isDarkTheme by dataViewModel.isDarkTheme.collectAsState()
            val trueBlack by dataViewModel.trueBlack.collectAsState()
            val darkTheme = isSystemInDarkTheme()

            LaunchedEffect(requestCounter) {
                hasPermission = hasPermissionFunction()
            }
            LaunchedEffect(dynamicColor) {
                if (dynamicColor) dataViewModel.setDarkTheme(darkTheme)
            }

            PayloadDumperAndroidTheme(darkTheme = isDarkTheme, dynamicColor = dynamicColor, trueBlack = trueBlack) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    )
                    {
                        if (!hasPermission){
                            Button(onClick = {
                                Utils.requestPermission(this@MainActivity)
                            }) {
                                Text("Allow file access")
                            }
                        } else {
                            AppLayout(dataViewModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AppLayout(dataViewModel: DataViewModel) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "home", enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(80)
            )
        }, exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(80)
            )
        }) {
            composable(Screens.HOME) {
                HomeScreen(navController, dataViewModel)
            }
            composable(Screens.SETTING) {
                SettingScreen(navController, dataViewModel)
            }
            composable(
                "${Screens.SELECTOR}/{selectDirectory}",
                arguments = listOf(
                    navArgument("selectDirectory") { type = NavType.BoolType }
                ),
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        tween(80)
                    )
                }, exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        tween(80)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        tween(80)
                    )
                }, popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        tween(80)
                    )
                }
            ) {
                SelectScreen(navController, dataViewModel)
            }
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

class Screens {
    companion object {
        const val HOME = "home"
        const val SETTING = "settings"
        const val SELECTOR = "selector"
    }
}