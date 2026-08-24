package com.shivayogih.packmate.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.packMateDataStore by preferencesDataStore(name = "packmate_data")

class TripRepository(context: Context) {
    private val appContext = context.applicationContext
    private val dataKey = stringPreferencesKey("packmate_json")
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val data: Flow<PackMateData> = appContext.packMateDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)

    suspend fun createTrip(draft: TripDraft): Long {
        val tripId = System.currentTimeMillis()
        val templateItems = TemplateCatalog.find(draft.templateId)
            ?.items
            .orEmpty()
            .mapIndexed { index, item ->
                PackingItem(
                    id = tripId * 1_000 + index,
                    name = item.name,
                    category = item.category,
                    quantity = item.quantity,
                    position = index,
                )
            }

        mutate { current ->
            current.copy(
                trips = listOf(
                    Trip(
                        id = tripId,
                        name = draft.name.trim(),
                        destination = draft.destination.trim(),
                        startDateMillis = draft.startDateMillis,
                        endDateMillis = draft.endDateMillis,
                        items = templateItems,
                    ),
                ) + current.trips,
            )
        }
        return tripId
    }

    suspend fun addItem(
        tripId: Long,
        name: String,
        category: PackingCategory,
        quantity: Int,
    ) = mutateTrips { trips ->
        trips.map { trip ->
            if (trip.id != tripId) return@map trip
            val nextId = (trip.items.maxOfOrNull(PackingItem::id) ?: (trip.id * 1_000)) + 1
            val nextPosition = (trip.items.maxOfOrNull(PackingItem::position) ?: -1) + 1
            trip.copy(
                items = trip.items + PackingItem(
                    id = nextId,
                    name = name.trim(),
                    category = category,
                    quantity = quantity.coerceAtLeast(1),
                    position = nextPosition,
                ),
            )
        }
    }

    suspend fun setPacked(tripId: Long, itemId: Long, packed: Boolean) = mutateTrips { trips ->
        trips.map { trip ->
            if (trip.id == tripId) {
                trip.copy(items = trip.items.map { item ->
                    if (item.id == itemId) item.copy(isPacked = packed) else item
                })
            } else trip
        }
    }

    suspend fun deleteItem(tripId: Long, itemId: Long) = mutateTrips { trips ->
        trips.map { trip ->
            if (trip.id == tripId) trip.copy(items = trip.items.filterNot { it.id == itemId })
            else trip
        }
    }

    suspend fun unpackAll(tripId: Long) = mutateTrips { trips ->
        trips.map { trip ->
            if (trip.id == tripId) trip.copy(items = trip.items.map { it.copy(isPacked = false) })
            else trip
        }
    }

    suspend fun deleteTrip(tripId: Long) = mutateTrips { trips ->
        trips.filterNot { it.id == tripId }
    }

    suspend fun addDemoTrip(): Long {
        val id = System.currentTimeMillis()
        val demo = Trip(
            id = id,
            name = "Mysuru weekend",
            destination = "Mysuru",
            startDateMillis = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1_000L,
            endDateMillis = System.currentTimeMillis() + 9 * 24 * 60 * 60 * 1_000L,
            items = TemplateCatalog.find("weekend")!!.items.mapIndexed { index, item ->
                PackingItem(
                    id = id * 1_000 + index,
                    name = item.name,
                    category = item.category,
                    quantity = item.quantity,
                    isPacked = index < 2,
                    position = index,
                )
            },
        )
        mutate { current -> current.copy(trips = listOf(demo) + current.trips) }
        return id
    }

    suspend fun resetAll() {
        appContext.packMateDataStore.edit { preferences ->
            preferences.remove(dataKey)
        }
    }

    private suspend fun mutateTrips(transform: (List<Trip>) -> List<Trip>) {
        mutate { current -> current.copy(trips = transform(current.trips)) }
    }

    private suspend fun mutate(transform: (PackMateData) -> PackMateData) {
        appContext.packMateDataStore.edit { preferences ->
            val current = decode(preferences)
            preferences[dataKey] = json.encodeToString(transform(current))
        }
    }

    private fun decode(preferences: Preferences): PackMateData {
        val raw = preferences[dataKey] ?: return PackMateData()
        return try {
            json.decodeFromString<PackMateData>(raw)
        } catch (_: SerializationException) {
            PackMateData()
        }
    }
}
