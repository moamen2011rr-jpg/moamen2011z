package com.smartprayertimes.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result { PrayerWidgetReceiver().onReceive(applicationContext, null); return Result.success() }
}
