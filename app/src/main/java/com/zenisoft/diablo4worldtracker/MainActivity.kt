package com.zenisoft.diablo4worldtracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zenisoft.diablo4worldtracker.ui.theme.Diablo4WorldTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Diablo4WorldTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Test",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        updateWidgets()
        WorldBossTracker.scheduleNextUpdate(this)
        startForegroundService()

    }

    private fun updateWidgets() {
        val widgetProvider = ComponentName(this, WorldBossTracker::class.java)
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)
        val widgetProviderIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        widgetProviderIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        sendBroadcast(widgetProviderIntent)
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Diablo4WorldTrackerTheme {
        Greeting("Android")
    }
}