package com.zenisoft.diablo4worldtracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.TextView
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Timer

/**
 * Implementation of App Widget functionality.
 */
val WORLDBOSS_ICONS = mapOf(
    "Ashava the Pestilent" to R.drawable.ashava,
    "Wandering Death, Death Given Life" to R.drawable.wanderingdeath,
    "Avarice, the Gold Cursed" to R.drawable.avarice,
)

val DIABLO4LIFE_API_URL = "https://diablo4.life/api/trackers/list"

val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")


class WorldBossTracker : AppWidgetProvider(), HttpGetRequestAsyncTask.OnTaskCompleted {

    var bossName: String = ""
    var bossTime: Long = 0
    var bossIcon: Int = 0

    var context: Context? = null

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WorldBossTracker", "onUpdate called")
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        this.context = context

        // Call HttpGetRequestAsyncTask to fetch data from the API

        val asyncTask = HttpGetRequestAsyncTask(this@WorldBossTracker)
        asyncTask.execute(DIABLO4LIFE_API_URL)
        // Schedule the next update
    }

    override fun onEnabled(context: Context) {
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onTaskCompleted(result: JSONObject?) {
        Log.d("WorldBossTracker", "onTaskCompleted called")
        result?.let {
            val worldBosses = it.getJSONObject("worldBoss")
            val worldBossName = worldBosses.getString("name")
            val worldBossIcon = WORLDBOSS_ICONS[worldBossName] ?: R.drawable.ashava


            bossName = worldBossName
            bossIcon = worldBossIcon
            bossTime = worldBosses.getLong("time")

            updateWidgetViews(context!!)
        }
    }

    private fun updateWidgetViews(context: Context) {
        // Create a RemoteViews instance
        val views = RemoteViews(context.packageName, R.layout.world_boss_tracker)

        // Update views with the fetched data
        views.setTextViewText(R.id.textViewBossName, bossName)
        views.setImageViewResource(R.id.imageViewBossImage, bossIcon)
        val unixTime: Long = System.currentTimeMillis()
        TIME_FORMAT.timeZone = java.util.TimeZone.getTimeZone("UTC")
        views.setTextViewText(R.id.textViewBossTimer, TIME_FORMAT.format(bossTime - unixTime))

        // Get the AppWidgetManager and update the widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, WorldBossTracker::class.java)
        appWidgetManager.updateAppWidget(widgetComponent, views)
    }

    companion object {
        fun scheduleNextUpdate(context: Context) {
            val timer = Timer()
            timer.schedule(object : java.util.TimerTask() {
                override fun run() {
                    val intent = Intent(context, WorldBossTracker::class.java)
                    intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, WorldBossTracker::class.java))
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    context.sendBroadcast(intent)
                }
            }, 0, 1000)


        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.world_boss_tracker)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}


class HttpGetRequestAsyncTask(private val listener: OnTaskCompleted) :
    AsyncTask<String, Void, JSONObject>() {

    interface OnTaskCompleted {
        fun onTaskCompleted(result: JSONObject?)
    }

    override fun doInBackground(vararg params: String?): JSONObject? {
        val urlString = params[0] ?: return null
        var urlConnection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            val inputStream = urlConnection.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val responseString = bufferedReader.use { it.readText() }
            JSONObject(responseString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            urlConnection?.disconnect()
        }
    }

    override fun onPostExecute(result: JSONObject?) {
        super.onPostExecute(result)
        listener.onTaskCompleted(result)
    }
}