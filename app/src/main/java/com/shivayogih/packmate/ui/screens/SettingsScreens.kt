package com.shivayogih.packmate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shivayogih.packmate.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    tripCount: Int,
    onAddDemoTrip: () -> Unit,
    onResetData: () -> Unit,
    onAbout: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Text("Private by design", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Your $tripCount ${if (tripCount == 1) "trip is" else "trips are"} stored only on this device. PackMate has no account, ads or analytics.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                ElevatedCard(onClick = onAddDemoTrip, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Add demo trip") },
                        supportingContent = { Text("Useful while exploring the sample") },
                        leadingContent = { Icon(Icons.Default.Science, contentDescription = null) },
                    )
                }
            }
            item {
                ElevatedCard(onClick = onResetData, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Delete all local data") },
                        supportingContent = { Text("Removes every trip and packing item") },
                        leadingContent = { Icon(Icons.Default.DeleteSweep, contentDescription = null) },
                    )
                }
            }
            item {
                ElevatedCard(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("About PackMate") },
                        supportingContent = { Text("Version ${BuildConfig.VERSION_NAME}") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("🎒", style = MaterialTheme.typography.displayLarge)
            Text("PackMate", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "A practical offline packing checklist and a focused Jetpack Compose Navigation 3 learning project.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text("Version ${BuildConfig.VERSION_NAME}")
            Text(
                "Built with Kotlin, Jetpack Compose, Navigation 3 and Preferences DataStore.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
