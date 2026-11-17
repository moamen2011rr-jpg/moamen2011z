package com.smartprayertimes.app

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDateTime
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrayerWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, PrayerWidgetReceiver::class.java))
        ids.forEach { update(context, manager, it) }
    }
    private fun update(context: Context, manager: AppWidgetManager, id: Int) {
        val store = SettingsStore(context)
        MainScope().launch {
            val settings = store.settings.first()
            val next = PrayerCalculator.calculate(settings.lat, settings.lon, settings.zone).firstOrNull { it.time.isAfter(LocalDateTime.now().toLocalTime()) }
            val views = RemoteViews(context.packageName, R.layout.widget_loading)
            views.setTextViewText(R.id.widget_text, "${settings.city}\n${next?.name ?: "—"}  ${next?.let { PrayerCalculator.formatTime(it.time, settings.is24) } ?: "—"}")
            manager.updateAppWidget(id, views)
        }
    }
}
