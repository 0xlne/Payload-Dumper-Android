package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.MainActivity
import com.rajmani7584.payloaddumper.models.DataViewModel
import com.rajmani7584.payloaddumper.ui.customviews.NButton
import kotlinx.coroutines.launch

@Composable
fun MainUI(
    mainActivity: MainActivity,
    dataModel: DataViewModel,
    navController: NavHostController,
    homeNavController: NavHostController
) {
    val horizontal = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val pagerState = rememberPagerState (0) { 3 }
        Row {
            if (horizontal) Column (Modifier.wrapContentWidth()) { Navs(pagerState, homeNavController) }
            HorizontalPager(pagerState, modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)) { current ->
                when (current) {
                    0 -> HomeScreen(mainActivity, dataModel, navController, homeNavController)
                    1 -> LogScreen(dataModel)
                    2 -> SettingScreen(dataModel)
                }
            }
        }
        if (!horizontal) {
            Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).drawBehind {
                val brush = Brush.verticalGradient(
                    colors = listOf(Color(0x55282828), Color.Transparent),
                    startY = size.height,
                    endY = size.height / 8f
                )
                drawRect(brush)
            }, contentAlignment = Alignment.Center) {
                Row(
                    Modifier.wrapContentWidth().padding(bottom = 12.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .95f), CircleShape),
                    horizontalArrangement = Arrangement.SpaceAround
                ) { Navs(pagerState, homeNavController) }
            }
        }
    }
}
@Composable
fun Navs(pagerState: PagerState, homeNavController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    NButton(onClick = {
        if (pagerState.currentPage == 0 && !homeNavController.currentDestination?.route.equals(HomeScreens.HOME)) homeNavController.popBackStack()
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }, isActive = pagerState.currentPage == 0, page = 0)
    NButton(onClick = {
        coroutineScope.launch {
            pagerState.animateScrollToPage(1)
        }
    }, isActive = pagerState.currentPage == 1, page = 1)
    NButton(onClick = {
        coroutineScope.launch {
            pagerState.animateScrollToPage(2)
        }
    }, isActive = pagerState.currentPage == 2, page = 2)
}