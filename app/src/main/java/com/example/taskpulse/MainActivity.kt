package com.example.taskpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.taskpulse.theme.TaskPulseTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val openCheckIn = intent?.getBooleanExtra(com.example.taskpulse.notifications.HabitReminderReceiver.EXTRA_OPEN_CHECKIN, false) ?: false

    enableEdgeToEdge()
    setContent {
      TaskPulseTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainNavigation(openCheckInOnStart = openCheckIn)
        }
      }
    }
  }
}

