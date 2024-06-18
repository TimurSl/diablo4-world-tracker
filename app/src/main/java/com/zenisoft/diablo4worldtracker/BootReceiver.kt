package com.zenisoft.diablo4worldtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your service here
            val serviceIntent = Intent(context, BackgroundService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}