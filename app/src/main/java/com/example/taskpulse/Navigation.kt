package com.example.taskpulse

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taskpulse.ui.TaskPulseAppScreen

@Composable
fun MainNavigation(openCheckInOnStart: Boolean = false) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          TaskPulseAppScreen(initialOpenCheckIn = openCheckInOnStart)
        }
      },
  )
}

