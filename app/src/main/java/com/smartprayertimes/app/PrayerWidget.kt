package com.smartprayertimes.app

import android.appwidget.AppWidgetManager
import android.content.*
import android.widget.RemoteViews
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first

class PrayerWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { val manager = AppWidgetManager.getInstance(context); val ids = manager.getAppWidgetIds(ComponentName(context, PrayerWidgetReceiver::class.java)); ids.forEach { update(context, manager, it) } }
    private fun update(context: Context, manager: AppWidgetManager, id: Int) { val s = SettingsStore(context); kotlinx.coroutines.MainScope().launch { val settings = s.settings.first(); val p = PrayerCalculator.calculate(settings.lat, settings.lon, settings.zone).firstOrNull { it.time.isAfter(LocalDateTime.now().toLocalTime()) }; val v = RemoteViews(context.packageName, com.smartprayertimes.app.R.layout.widget_loading); v.setTextViewText(com.smartprayertimes.app.R.id.widget_text, "${settings.city}\n${p?.name ?: "—"}  ${p?.time ?: "—"}"); manager.updateAppWidget(id, v) } }
}
