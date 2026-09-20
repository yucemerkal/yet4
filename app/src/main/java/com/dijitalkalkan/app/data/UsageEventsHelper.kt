package com.dijitalkalkan.app.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AppEvent(
    val packageName: String,
    val appLabel: String,
    val opened: Boolean, // true = açıldı, false = kapandı
    val timeMillis: Long
) {
    fun timeText(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(timeMillis)
}

/**
 * Bugün hangi uygulamanın saat kaçta açılıp kaçta kapandığını (ön plana
 * geçme / ön plandan çıkma) listeler. Sadece Ebeveyn Paneli'nde gösterilir.
 */
object UsageEventsHelper {

    fun getTodayEvents(context: Context): List<AppEvent> {
        if (!UsageStatsHelper.hasUsagePermission(context)) return emptyList()

        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.packageManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val events = usm.queryEvents(startTime, endTime)
        val result = mutableListOf<AppEvent>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val isResume = event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            val isPause = event.eventType == UsageEvents.Event.ACTIVITY_PAUSED
            if (!isResume && !isPause) continue
            if (event.packageName == context.packageName) continue

            val label = try {
                val appInfo = pm.getApplicationInfo(event.packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                event.packageName
            }

            result.add(AppEvent(event.packageName, label, isResume, event.timeStamp))
        }

        return result.sortedByDescending { it.timeMillis }
    }
}
