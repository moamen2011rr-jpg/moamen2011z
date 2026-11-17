package com.smartprayertimes.app

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.*

data class Prayer(val name: String, val time: LocalTime)

object PrayerCalculator {
    fun calculate(lat: Double, lon: Double, zone: String, date: LocalDate = LocalDate.now()): List<Prayer> {
        val n = date.dayOfYear.toDouble(); val decl = Math.toRadians(23.44 * sin(Math.toRadians(360.0 / 365.0 * (n - 81))))
        val latitude = Math.toRadians(lat); val tz = java.time.ZoneId.of(zone).rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0
        val noon = 12 - lon / 15 + tz
        fun angle(a: Double): Double { val x = (sin(Math.toRadians(a)) - sin(latitude) * sin(decl)) / (cos(latitude) * cos(decl)); return Math.toDegrees(acos(x.coerceIn(-1.0, 1.0))) / 15 }
        fun time(v: Double): LocalTime { val m = ((v * 60).roundToInt() % 1440 + 1440) % 1440; return LocalTime.of(m / 60, m % 60) }
        val asr = Math.toDegrees(atan(1 / (1 + tan(abs(latitude - decl)))))
        return listOf(Prayer("الفجر", time(noon - angle(-18.0))), Prayer("الشروق", time(noon - angle(-0.833))), Prayer("الظهر", time(noon)), Prayer("العصر", time(noon + angle(asr))), Prayer("المغرب", time(noon + angle(-0.833))), Prayer("العشاء", time(noon + angle(-18.0))))
    }
}
