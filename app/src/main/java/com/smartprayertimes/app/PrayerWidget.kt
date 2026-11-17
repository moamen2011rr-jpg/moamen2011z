package com.smartprayertimes.app

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import java.time.LocalDateTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> MainScope().launch {
            val settings = SettingsStore(context).settings.first()
            val next = PrayerCalculator.calculate(settings.lat, settings.lon, settings.zone).firstOrNull { it.time.isAfter(LocalDateTime.now().toLocalTime()) }
            val views = RemoteViews(context.packageName, R.layout.widget_loading)
            views.setTextViewText(R.id.widget_text, "${settings.city}\n${next?.name ?: "—"}  ${next?.let { PrayerCalculator.formatTime(it.time, settings.is24) } ?: "—"}")
            manager.updateAppWidget(id, views)
        } }
    }
}
