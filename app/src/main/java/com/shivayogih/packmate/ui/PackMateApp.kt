package com.shivayogih.packmate.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.shivayogih.packmate.ui.navigation.AboutRoute
import com.shivayogih.packmate.ui.navigation.AddItemDialogRoute
import com.shivayogih.packmate.ui.navigation.CreateTripRoute
import com.shivayogih.packmate.ui.navigation.DeleteTripDialogRoute
import com.shivayogih.packmate.ui.navigation.Navigator
import com.shivayogih.packmate.ui.navigation.ResetDataDialogRoute
import com.shivayogih.packmate.ui.navigation.SettingsRoute
import com.shivayogih.packmate.ui.navigation.TOP_LEVEL_ROUTES
import com.shivayogih.packmate.ui.navigation.TemplateDetailRoute
import com.shivayogih.packmate.ui.navigation.TemplatesRoute
import com.shivayogih.packmate.ui.navigation.TripDetailRoute
import com.shivayogih.packmate.ui.navigation.TripsRoute
import com.shivayogih.packmate.ui.navigation.rememberNavigationState
import com.shivayogih.packmate.ui.screens.AboutScreen
import com.shivayogih.packmate.ui.screens.AddItemDialogScreen
import com.shivayogih.packmate.ui.screens.CreateTripScreen
import com.shivayogih.packmate.ui.screens.DeleteTripDialogScreen
import com.shivayogih.packmate.ui.screens.ResetDataDialogScreen
import com.shivayogih.packmate.ui.screens.SettingsScreen
import com.shivayogih.packmate.ui.screens.TemplateDetailScreen
import com.shivayogih.packmate.ui.screens.TemplatesScreen
import com.shivayogih.packmate.ui.screens.TripDetailScreen
import com.shivayogih.packmate.ui.screens.TripsScreen

private data class BottomDestination(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(TripsRoute, "Trips", Icons.Default.Home),
    BottomDestination(TemplatesRoute, "Templates", Icons.Default.List),
    BottomDestination(SettingsRoute, "Settings", Icons.Default.Settings),
)

@Composable
fun PackMateApp(viewModel: PackMateViewModel) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val navigationState = rememberNavigationState(
        startRoute = TripsRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val dialogStrategy = remember { DialogSceneStrategy<NavKey>() }
    val currentRoute = navigationState.currentRoute

    val entries = entryProvider {
        entry<TripsRoute> {
            TripsScreen(
                trips = data.trips,
                onTripClick = { navigator.navigate(TripDetailRoute(it)) },
                onCreateTrip = { navigator.navigate(CreateTripRoute()) },
            )
        }
        entry<CreateTripRoute> { route ->
            CreateTripScreen(
                templateId = route.templateId,
                onBack = navigator::goBack,
                onCreate = { draft ->
                    viewModel.createTrip(draft) { tripId ->
                        navigator.openInTopLevel(TripsRoute, TripDetailRoute(tripId))
                    }
                },
            )
        }
        entry<TripDetailRoute> { route ->
            val trip = data.trips.firstOrNull { it.id == route.tripId }
            TripDetailScreen(
                trip = trip,
                onBack = navigator::goBack,
                onAddItem = { navigator.navigate(AddItemDialogRoute(route.tripId)) },
                onTogglePacked = { item, packed ->
                    viewModel.setPacked(route.tripId, item.id, packed)
                },
                onDeleteItem = { item -> viewModel.deleteItem(route.tripId, item.id) },
                onUnpackAll = { viewModel.unpackAll(route.tripId) },
                onDeleteTrip = {
                    navigator.navigate(
                        DeleteTripDialogRoute(
                            tripId = route.tripId,
                            tripName = trip?.name.orEmpty(),
                        ),
                    )
                },
            )
        }
        entry<TemplatesRoute> {
            TemplatesScreen(
                onTemplateClick = { navigator.navigate(TemplateDetailRoute(it)) },
            )
        }
        entry<TemplateDetailRoute> { route ->
            TemplateDetailScreen(
                templateId = route.templateId,
                onBack = navigator::goBack,
                onUseTemplate = { navigator.navigate(CreateTripRoute(route.templateId)) },
            )
        }
        entry<SettingsRoute> {
            SettingsScreen(
                tripCount = data.trips.size,
                onAddDemoTrip = {
                    viewModel.addDemoTrip { tripId ->
                        navigator.openInTopLevel(TripsRoute, TripDetailRoute(tripId))
                    }
                },
                onResetData = { navigator.navigate(ResetDataDialogRoute) },
                onAbout = { navigator.navigate(AboutRoute) },
            )
        }
        entry<AboutRoute> {
            AboutScreen(onBack = navigator::goBack)
        }
        entry<AddItemDialogRoute>(
            metadata = DialogSceneStrategy.dialog(
                DialogProperties(windowTitle = "Add packing item"),
            ),
        ) { route ->
            AddItemDialogScreen(
                onDismiss = navigator::goBack,
                onAdd = { name, category, quantity ->
                    viewModel.addItem(route.tripId, name, category, quantity) {
                        navigator.goBack()
                    }
                },
            )
        }
        entry<DeleteTripDialogRoute>(
            metadata = DialogSceneStrategy.dialog(
                DialogProperties(windowTitle = "Delete trip"),
            ),
        ) { route ->
            DeleteTripDialogScreen(
                tripName = route.tripName,
                onDismiss = navigator::goBack,
                onConfirm = {
                    viewModel.deleteTrip(route.tripId) {
                        navigator.selectTopLevel(TripsRoute)
                    }
                },
            )
        }
        entry<ResetDataDialogRoute>(
            metadata = DialogSceneStrategy.dialog(
                DialogProperties(windowTitle = "Delete local data"),
            ),
        ) {
            ResetDataDialogScreen(
                onDismiss = navigator::goBack,
                onConfirm = { viewModel.resetAll(navigator::goBack) },
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (currentRoute in TOP_LEVEL_ROUTES) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = navigationState.topLevelRoute == destination.route,
                            onClick = { navigator.selectTopLevel(destination.route) },
                            icon = {
                                Icon(destination.icon, contentDescription = destination.label)
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entries),
            onBack = navigator::goBack,
            sceneStrategies = listOf(dialogStrategy),
            modifier = Modifier.padding(padding),
        )
    }
}
