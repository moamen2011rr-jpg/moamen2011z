package com.smartprayertimes.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("settings")
data class Settings(val city: String = "القاهرة", val country: String = "مصر", val lat: Double = 30.0444, val lon: Double = 31.2357, val zone: String = "Africa/Cairo", val is24: Boolean = true, val dark: Boolean = true, val adhan: List<Boolean> = listOf(true, false, true, true, true, true))
class SettingsStore(private val context: Context) {
    private object K { val city = stringPreferencesKey("city"); val country = stringPreferencesKey("country"); val lat = doublePreferencesKey("lat"); val lon = doublePreferencesKey("lon"); val zone = stringPreferencesKey("zone"); val is24 = booleanPreferencesKey("is24"); val dark = booleanPreferencesKey("dark"); val adhan = stringPreferencesKey("adhan") }
    val settings: Flow<Settings> = context.store.data.map { p -> Settings(p[K.city] ?: "القاهرة", p[K.country] ?: "مصر", p[K.lat] ?: 30.0444, p[K.lon] ?: 31.2357, p[K.zone] ?: "Africa/Cairo", p[K.is24] ?: true, p[K.dark] ?: true, (p[K.adhan] ?: "101111").map { it == '1' }) }
    suspend fun save(s: Settings) { context.store.edit { it[K.city]=s.city; it[K.country]=s.country; it[K.lat]=s.lat; it[K.lon]=s.lon; it[K.zone]=s.zone; it[K.is24]=s.is24; it[K.dark]=s.dark; it[K.adhan]=s.adhan.joinToString("") { if (it) "1" else "0" } } }
}
