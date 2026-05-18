package com.bahri.lovelypos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bahri.lovelypos.ui.screen.HistoryScreen
import com.bahri.lovelypos.ui.screen.MenuScreen
import com.bahri.lovelypos.ui.screen.POSScreen
import com.bahri.lovelypos.ui.screen.SummaryScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "pos"
    ) {
        composable("pos") { POSScreen() }
        composable("menu") { MenuScreen() }
        composable("history") { HistoryScreen() }
        composable("summary") { SummaryScreen() }
    }
}