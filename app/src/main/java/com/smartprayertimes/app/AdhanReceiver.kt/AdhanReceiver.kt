package com.smartprayertimes.app

import android.app.*
import android.content.*
import android.media.MediaPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.*

object AdhanScheduler {
 fun refresh(c: Context, s: Settings) { val a=c.getSystemService(AlarmManager::class.java); val z=ZoneId.of(s.zone); for(d in 0..2){ val date=LocalDate.now(z).plusDays(d.toLong()); PrayerCalculator.calculate(s.lat,s.lon,s.zone,date).forEachIndexed { i,p -> if(s.adhan.getOrElse(i){false}){ val at=ZonedDateTime.of(date,p.time,z).toInstant().toEpochMilli(); if(at>System.currentTimeMillis()){ val inx=Intent(c,AdhanReceiver::class.java).setAction("adhan.play"); val pi=PendingIntent.getBroadcast(c,date.toEpochDay().toInt()*10+i,inx,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE); a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi) } } } } }
}

class AdhanReceiver: BroadcastReceiver(){ override fun onReceive(c: Context, i: Intent?){ if(i?.action=="adhan.toggle"){ val p=goAsync(); CoroutineScope(Dispatchers.IO).launch { val store=SettingsStore(c); val old=store.settings.first(); val v=old.adhan.toMutableList(); val n=i.getIntExtra("index",-1); if(n in v.indices){v[n]=!v[n]; val s=old.copy(adhan=v); store.save(s); AdhanScheduler.refresh(c,s); c.sendBroadcast(Intent(c,DailyWidgetProvider::class.java).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE))}; p.finish() }; return }; val m=MediaPlayer.create(c,R.raw.adhan); m?.setOnCompletionListener{it.release()}; m?.start() } }
