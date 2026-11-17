package com.smartprayertimes.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    override fun onCreate(state: Bundle?) { super.onCreate(state); val store = SettingsStore(applicationContext); locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)); LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location -> if (location != null) kotlinx.coroutines.MainScope().launch { val old = store.settings.first(); store.save(old.copy(lat = location.latitude, lon = location.longitude, zone = java.time.ZoneId.systemDefault().id)) } }; WorkManager.getInstance(this).enqueueUniquePeriodicWork("prayer_refresh", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<RefreshWorker>(6, TimeUnit.HOURS).build()); setContent { PrayerApp() } }
    @Composable private fun PrayerApp() {
        val store = remember { SettingsStore(applicationContext) }; val settings by store.settings.collectAsStateWithLifecycle(Settings()); val scope = rememberCoroutineScope(); var now by remember { mutableStateOf(LocalDateTime.now()) }
        LaunchedEffect(Unit) { while (true) { now = LocalDateTime.now(); kotlinx.coroutines.delay(1000) } }
        val prayers = remember(settings, now.toLocalDate()) { PrayerCalculator.calculate(settings.lat, settings.lon, settings.zone, now.toLocalDate()) }; val next = prayers.firstOrNull { it.time.isAfter(now.toLocalTime()) } ?: prayers.first()
        MaterialTheme(colorScheme = if (settings.dark) darkColorScheme() else lightColorScheme()) { CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) { Surface(Modifier.fillMaxSize()) { Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("مواقيت الصلاة الذكية", style = MaterialTheme.typography.headlineSmall); Text("📍 ${settings.city}، ${settings.country}"); Spacer(Modifier.height(8.dp)); Text(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")), style = MaterialTheme.typography.displaySmall); Text("الصلاة القادمة: ${next.name} — ${next.time}", color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(16.dp)); LazyColumn(Modifier.weight(1f).fillMaxWidth()) { items(prayers.size) { i -> ListItem(headlineContent = { Text(prayers[i].name) }, trailingContent = { Text(prayers[i].time.toString()) }) } }; Button(onClick = { scope.launch { store.save(settings.copy(dark = !settings.dark)) } }) { Text(if (settings.dark) "الوضع النهاري" else "الوضع الليلي") } } } } }
    }
}
