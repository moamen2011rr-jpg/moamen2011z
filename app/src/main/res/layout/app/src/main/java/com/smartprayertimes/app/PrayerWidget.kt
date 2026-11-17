package com.smartprayertimes.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

private suspend fun settingsFor(context: Context) = SettingsStore(context).settings.first()

private fun nextPrayer(s: Settings): Prayer {
    val now = ZonedDateTime.now(ZoneId.of(s.zone))
    val today = PrayerCalculator.calculate(s.lat, s.lon, s.zone, now.toLocalDate())
    return today.firstOrNull { it.time.isAfter(now.toLocalTime()) }
        ?: PrayerCalculator.calculate(s.lat, s.lon, s.zone, now.toLocalDate().plusDays(1)).first()
}

private fun updateEach(context: Context, manager: AppWidgetManager, ids: IntArray, block: (Settings) -> RemoteViews) {
    ids.forEach { id ->
        CoroutineScope(Dispatchers.IO).launch {
            val views = runCatching { block(settingsFor(context)) }
                .getOrElse { RemoteViews(context.packageName, R.layout.widget_loading) }
            manager.updateAppWidget(id, views)
        }
    }
}

class NextPrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = updateEach(context, manager, ids) { s ->
        val next = nextPrayer(s)
        RemoteViews(context.packageName, R.layout.widget_next).apply {
            setTextViewText(R.id.widget_text, "الصلاة القادمة\n${next.name}  ${PrayerCalculator.formatTime(next.time, s.is24)}")
        }
    }
}

class ClockWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = updateEach(context, manager, ids) { s ->
        val now = ZonedDateTime.now(ZoneId.of(s.zone)).toLocalTime()
        val next = nextPrayer(s)
        RemoteViews(context.packageName, R.layout.widget_clock).apply {
            setTextViewText(R.id.widget_time, PrayerCalculator.formatTime(now, s.is24))
            setTextViewText(R.id.widget_city, "${s.city} • القادمة ${next.name} ${PrayerCalculator.formatTime(next.time, s.is24)}")
        }
    }
}

class DailyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = updateEach(context, manager, ids) { s ->
        val text = PrayerCalculator.calculate(s.lat, s.lon, s.zone).joinToString("\n") {
            "${it.name}: ${PrayerCalculator.formatTime(it.time, s.is24)}"
        }
        RemoteViews(context.packageName, R.layout.widget_daily).apply {
            setTextViewText(R.id.widget_daily_text, text)
        }
    }
}
