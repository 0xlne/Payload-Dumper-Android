package com.rajmani7584.payloaddumper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.rajmani7584.payloaddumper.HomeScreen
import com.rajmani7584.payloaddumper.models.DataViewModel

@Composable
fun Home(dataModel: DataViewModel, navController: NavHostController) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column {
            Text("Home")
            var state by remember { mutableStateOf(false) }
            OutlinedIconToggleButton(state, onCheckedChange = { state = it}) { }
            Button(onClick = {
                navController.navigate("${HomeScreen.SELECTOR}/$state") {
                    popUpTo(HomeScreen.HOME) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }) { Text("Go") }
        }

    }
}