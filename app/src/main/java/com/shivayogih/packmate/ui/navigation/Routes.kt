package com.shivayogih.packmate.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TripsRoute : NavKey

@Serializable
data class CreateTripRoute(val templateId: String? = null) : NavKey

@Serializable
data class TripDetailRoute(val tripId: Long) : NavKey

@Serializable
data object TemplatesRoute : NavKey

@Serializable
data class TemplateDetailRoute(val templateId: String) : NavKey

@Serializable
data object SettingsRoute : NavKey

@Serializable
data object AboutRoute : NavKey

@Serializable
data class AddItemDialogRoute(val tripId: Long) : NavKey

@Serializable
data class DeleteTripDialogRoute(
    val tripId: Long,
    val tripName: String,
) : NavKey

@Serializable
data object ResetDataDialogRoute : NavKey

val TOP_LEVEL_ROUTES: Set<NavKey> = linkedSetOf(
    TripsRoute,
    TemplatesRoute,
    SettingsRoute,
)
