package com.smartprayertimes.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object AdhanScheduler {
    fun refresh(context: Context, settings: Settings) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val zone = ZoneId.of(settings.zone)
        for (dayOffset in 0..2) {
            val date = LocalDate.now(zone).plusDays(dayOffset.toLong())
            PrayerCalculator.calculate(settings.lat, settings.lon, settings.zone, date).forEachIndexed { index, prayer ->
                if (settings.adhan.getOrElse(index) { false }) {
                    val at = ZonedDateTime.of(date, prayer.time, zone).toInstant().toEpochMilli()
                    if (at > System.currentTimeMillis()) {
                        val pending = PendingIntent.getBroadcast(context, date.toEpochDay().toInt() * 10 + index, Intent(context, AdhanReceiver::class.java).setAction("adhan.play"), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
                    }
                }
            }
        }
    }
}

class AdhanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "adhan.toggle") {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val store = SettingsStore(context)
                    val old = store.settings.first()
                    val values = old.adhan.toMutableList()
                    val index = intent.getIntExtra("index", -1)
                    if (index in values.indices) {
                        values[index] = !values[index]
                        val updated = old.copy(adhan = values)
                        store.save(updated)
                        AdhanScheduler.refresh(context, updated)
                        val manager = AppWidgetManager.getInstance(context)
                        val ids = manager.getAppWidgetIds(ComponentName(context, DailyWidgetProvider::class.java))
                        context.sendBroadcast(Intent(context, DailyWidgetProvider::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids))
                    }
                } finally { pending.finish() }
            }
            return
        }
        val player = MediaPlayer.create(context, R.raw.adhan) ?: return
        player.setOnCompletionListener { it.release() }
        player.start()
    }
}
